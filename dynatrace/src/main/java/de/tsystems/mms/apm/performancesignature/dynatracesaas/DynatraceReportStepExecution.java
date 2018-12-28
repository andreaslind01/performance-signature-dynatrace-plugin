/*
 * Copyright (c) 2014-2018 T-Systems Multimedia Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tsystems.mms.apm.performancesignature.dynatracesaas;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.tsystems.mms.apm.performancesignature.dynatrace.model.*;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.model.DynatraceServerConfiguration;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.model.Specification;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.model.SpecificationTM;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.rest.DynatraceServerConnection;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.rest.model.AggregationTypeEnum;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.rest.model.TimeseriesDataPointQueryResult;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.rest.model.TimeseriesDefinition;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.rest.model.UnitEnum;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.util.ConversionHelper;
import de.tsystems.mms.apm.performancesignature.dynatracesaas.util.DynatraceUtils;
import de.tsystems.mms.apm.performancesignature.ui.PerfSigBuildAction;
import de.tsystems.mms.apm.performancesignature.ui.util.PerfSigUIUtils;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.MissingContextVariableException;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DynatraceReportStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DynatraceReportStepExecution.class.getName());
    private static final String DEFAULT_COLOR = "#006bba";
    private final transient DynatraceReportStep step;
    private FilePath ws;

    public DynatraceReportStepExecution(DynatraceReportStep dynatraceReportStep, StepContext context) {
        super(context);
        this.step = dynatraceReportStep;
    }

    static void convertUnitOfDataPoints(Map<AggregationTypeEnum, TimeseriesDataPointQueryResult> aggregations) {
        for (TimeseriesDataPointQueryResult queryResult : aggregations.values()) {
            Map<String, Map<Long, Double>> dataPoints = queryResult.getDataPoints();
            if (MapUtils.isEmpty(dataPoints)) continue;

            //get the max value from each entity
            OptionalDouble maxValue2 = dataPoints.entrySet().stream()
                    .flatMapToDouble(stringMapEntry -> stringMapEntry.getValue().entrySet().stream()
                            .filter(longDoubleEntry -> longDoubleEntry.getValue() != null)
                            .mapToDouble(Map.Entry::getValue)).max();
            UnitEnum calculatedUnit = calculateUnitEnum(aggregations.entrySet().iterator().next().getValue(), maxValue2.isPresent() ? maxValue2.getAsDouble() : 0);

            Map<String, Map<Long, Double>> convertedDataPoints = new LinkedHashMap<>();
            for (Map.Entry<String, Map<Long, Double>> entry : dataPoints.entrySet()) {
                String key = entry.getKey();
                Map<Long, Double> value = entry.getValue();
                Map<Long, Double> convertedValuesPerEntity = new LinkedHashMap<>();
                for (Map.Entry<Long, Double> mapEntry : value.entrySet()) {
                    convertedValuesPerEntity.put(mapEntry.getKey(), ConversionHelper.convertUnit(mapEntry.getValue(), queryResult.getUnit(), calculatedUnit));
                }
                convertedDataPoints.put(key, convertedValuesPerEntity);
            }
            queryResult.setUnit(calculatedUnit);
            queryResult.getDataPoints().clear();
            queryResult.getDataPoints().putAll(convertedDataPoints);
        }
    }

    private static double getMeasurementValue(Map<AggregationTypeEnum, Map<Long, Double>> scalarValues, long key, AggregationTypeEnum aggregation) {
        Map<Long, Double> aggregationValues = scalarValues.get(aggregation);
        if (MapUtils.isNotEmpty(aggregationValues)) {
            return PerfSigUIUtils.roundAsDouble(aggregationValues.get(key));
        }
        return 0;
    }

    private static String handleEntityIdString(Map<String, String> entities, String entityId) {
        if (StringUtils.isBlank(entityId) || MapUtils.isEmpty(entities)) return null;

        String cleanedEntityId = entityId.split(",")[0];
        return entities.get(cleanedEntityId);
    }

    private static Map<AggregationTypeEnum, Map<Long, Double>> getScalarValues(Map<AggregationTypeEnum, TimeseriesDataPointQueryResult> dataPointQueryResultMap) {
        Map<AggregationTypeEnum, Map<Long, Double>> hashMap = new LinkedHashMap<>();

        dataPointQueryResultMap.forEach((key, value) -> {
            Map<String, Map<Long, Double>> dataPoints = value.getDataPoints();
            if (dataPoints != null) {
                Map<Long, Double> aggregatedValues = dataPoints.values().stream()
                        .flatMap(test -> test.entrySet().stream())
                        .filter(longDoubleEntry -> longDoubleEntry.getValue() != null)
                        .collect(
                                Collectors.groupingBy(
                                        Map.Entry::getKey,
                                        Collectors.averagingDouble(Map.Entry::getValue)
                                )
                        );
                hashMap.put(key, aggregatedValues);
            }
        });
        return hashMap;
    }

    private static double getScalarValue(TimeseriesDataPointQueryResult dataPointQueryResult) {
        if (dataPointQueryResult != null && MapUtils.isNotEmpty(dataPointQueryResult.getDataPoints())) {
            OptionalDouble average = dataPointQueryResult.getDataPoints().values().stream()
                    .flatMap(values -> values.values().stream())
                    .mapToDouble(a -> a).average();
            if (average.isPresent()) return PerfSigUIUtils.roundAsDouble(average.getAsDouble());
        }
        return 0;
    }

    private static String translateAggregation(AggregationTypeEnum aggregation) {
        switch (aggregation) {
            case MIN:
                return "Minimum";
            case MAX:
                return "Maximum";
            case AVG:
                return "Average";
            default:
                return StringUtils.capitalize(aggregation.getValue().toLowerCase());
        }
    }

    private static List<Alert> evaluateSpecification(double globalLowerBound, double globalUpperBound, SpecificationTM specTM, Map<AggregationTypeEnum,
            TimeseriesDataPointQueryResult> aggregations, Map<String, TimeseriesDefinition> timeseries) {

        List<Alert> alerts = new ArrayList<>();
        double lowerbound = Optional.ofNullable(specTM.getLowerLimit()).orElse(globalLowerBound);
        double upperBound = Optional.ofNullable(specTM.getUpperLimit()).orElse(globalUpperBound);
        TimeseriesDataPointQueryResult result = aggregations.get(specTM.getAggregation());
        if (specTM.getAggregation() == null || result == null) return alerts;

        for (Map.Entry<String, Map<Long, Double>> entry : result.getDataPoints().entrySet()) {
            String entity = handleEntityIdString(result.getEntities(), entry.getKey());

            for (Map.Entry<Long, Double> e : entry.getValue().entrySet()) {
                Long timestamp = e.getKey();
                Double value = e.getValue();
                if (value != null) {
                    if (lowerbound < upperBound) {
                        if (value > lowerbound && value < upperBound) {
                            String rule = timeseries.get(result.getTimeseriesId()).getDetailedSource() + " - " + timeseries.get(result.getTimeseriesId()).getDisplayName();
                            alerts.add(new Alert(Alert.SeverityEnum.WARNING,
                                    String.format("SpecFile threshold violation: %s lower bound exceeded", rule),
                                    String.format("%s: Measured peak value: %.2f %s on Entity: %s, Lower Bound: %.2f %s",
                                            rule, value, result.getUnit(), entity, lowerbound, result.getUnit()),
                                    timestamp, rule));
                        } else if (value > upperBound) {
                            String rule = timeseries.get(result.getTimeseriesId()).getDetailedSource() + " - " + timeseries.get(result.getTimeseriesId()).getDisplayName();
                            alerts.add(new Alert(Alert.SeverityEnum.SEVERE,
                                    String.format("SpecFile threshold violation: %s upper bound exceeded", rule),
                                    String.format("%s: Measured peak value: %.2f %s on Entity: %s, Upper Bound: %.2f %s",
                                            rule, value, result.getUnit(), entity, upperBound, result.getUnit()),
                                    timestamp, rule));
                        }
                    } else {
                        if (value < lowerbound && value > upperBound) {
                            String rule = timeseries.get(result.getTimeseriesId()).getDetailedSource() + " - " + timeseries.get(result.getTimeseriesId()).getDisplayName();
                            alerts.add(new Alert(Alert.SeverityEnum.WARNING,
                                    String.format("SpecFile threshold violation: %s lower bound exceeded", rule),
                                    String.format("%s: Measured peak value: %.2f %s on Entity: %s, Lower Bound: %.2f %s",
                                            rule, value, result.getUnit(), entity, lowerbound, result.getUnit()),
                                    timestamp, rule));
                        } else {
                            if (value < upperBound) {
                                String rule = timeseries.get(result.getTimeseriesId()).getDetailedSource() + " - " + timeseries.get(result.getTimeseriesId()).getDisplayName();
                                alerts.add(new Alert(Alert.SeverityEnum.SEVERE,
                                        String.format("SpecFile threshold violation: %s upper bound exceeded", rule),
                                        String.format("%s: Measured peak value: %.2f %s on Entity: %s, Upper Bound: %.2f %s",
                                                rule, value, result.getUnit(), entity, upperBound, result.getUnit()),
                                        timestamp, rule));
                            }
                        }
                    }
                }
            }
        }
        return alerts;
    }

    private static UnitEnum calculateUnitEnum(TimeseriesDataPointQueryResult baseResult, double maxValue) {
        System.out.println(baseResult);
        UnitEnum unit = baseResult.getUnit();
        if (ConversionHelper.TIME_UNITS.contains(unit)) {
            return UnitEnum.MILLISECOND;
        } else {
            return ConversionHelper.convertByteUnitEnum(maxValue, unit);
        }
    }

    private static Number getAggregationValue(TimeseriesDataPointQueryResult value, String key, Long timestamp) {
        if (value == null) return 0;
        return value.getDataPoints().get(key).get(timestamp);
    }

    public DynatraceReportStep getStep() {
        return step;
    }

    @Override
    protected Void run() throws Exception {
        Run<?, ?> run = getContext().get(Run.class);
        TaskListener listener = getContext().get(TaskListener.class);

        ws = getContext().get(FilePath.class);

        if (run == null || listener == null) {
            throw new IllegalStateException("pipeline step was called without run or task listener in context");
        }
        if (StringUtils.isNotBlank(step.getSpecFile()) && CollectionUtils.isNotEmpty(step.getMetrics())) {
            throw new IllegalArgumentException("At most one of file or text must be provided to " + step.getDescriptor().getFunctionName());
        }
        if (ws == null && StringUtils.isNotBlank(step.getSpecFile())) {
            throw new MissingContextVariableException(FilePath.class);
        }
        DynatraceServerConnection serverConnection = DynatraceUtils.createDynatraceServerConnection(step.getEnvId(), true);
        println("getting metric data from Dynatrace Server");

        Map<String, TimeseriesDefinition> timeseries = serverConnection.getTimeseries()
                .parallelStream().collect(Collectors.toMap(TimeseriesDefinition::getTimeseriesId, item -> item));

        final List<DynatraceEnvInvisAction> envInvisActions = run.getActions(DynatraceEnvInvisAction.class);
        final List<DashboardReport> dashboardReports = new ArrayList<>();
        Specification spec = getSpecifications();

        try {
            for (DynatraceEnvInvisAction dynatraceAction : envInvisActions) {
                Long start = dynatraceAction.getTimeframeStart();
                Long end = dynatraceAction.getTimeframeStop();
                DashboardReport dashboardReport = new DashboardReport(dynatraceAction.getTestCase());

                //set url for Dynatrace dashboard
                DynatraceServerConfiguration configuration = serverConnection.getConfiguration();
                dashboardReport.setClientUrl(String.format("%s/#dashboard;gtf=c_%d_%d", configuration.getServerUrl(), start, end));

                //iterate over specified timeseries ids
                spec.getTimeseries().forEach(specTM -> {
                    TimeseriesDefinition tm = timeseries.get(specTM.getTimeseriesId());
                    //get data points for every possible aggregation
                    Map<AggregationTypeEnum, TimeseriesDataPointQueryResult> aggregations = tm.getAggregationTypes().parallelStream()
                            .collect(Collectors.toMap(
                                    Function.identity(),
                                    aggregation -> serverConnection.getTimeseriesData(specTM.getTimeseriesId(), start, end, aggregation, specTM.getPercentile(), specTM.getEntityIds(), specTM.getTags()),
                                    (a, b) -> b, LinkedHashMap::new)
                            );
                    convertUnitOfDataPoints(aggregations);

                    TimeseriesDataPointQueryResult baseResult = aggregations.get(AggregationTypeEnum.AVG);
                    if (baseResult != null && MapUtils.isNotEmpty(baseResult.getDataPoints())) {
                        //get a scalar value for every possible aggregation
                        Map<AggregationTypeEnum, TimeseriesDataPointQueryResult> totalValues = tm.getAggregationTypes().parallelStream()
                                .collect(Collectors.toMap(Function.identity(),
                                        aggregation -> serverConnection.getTotalTimeseriesData(specTM.getTimeseriesId(), start, end, aggregation, specTM.getPercentile(), specTM.getEntityIds(), specTM.getTags()),
                                        (a, b) -> b, LinkedHashMap::new));
                        convertUnitOfDataPoints(totalValues);

                        //evaluate possible incidents
                        dashboardReport.getIncidents().addAll(evaluateSpecification(spec.getLowerLimit(), spec.getUpperLimit(),
                                specTM, aggregations, timeseries));

                        ChartDashlet chartDashlet = new ChartDashlet();
                        chartDashlet.setName(tm.getDetailedSource() + " - " + tm.getDisplayName());

                        //create aggregated overall measure
                        Measure overallMeasure = new Measure("overall");
                        overallMeasure.setAggregation(translateAggregation(specTM.getAggregation()));
                        overallMeasure.setColor(DEFAULT_COLOR);

                        UnitEnum calculatedUnit = calculateUnitEnum(baseResult, getScalarValue(totalValues.get(AggregationTypeEnum.MAX)));

                        overallMeasure.setUnit(calculatedUnit.getValue());

                        //calculate aggregated values from totalValues
                        overallMeasure.setAvg(getScalarValue(totalValues.get(AggregationTypeEnum.AVG)));
                        overallMeasure.setMin(getScalarValue(totalValues.get(AggregationTypeEnum.MIN)));
                        overallMeasure.setMax(getScalarValue(totalValues.get(AggregationTypeEnum.MAX)));
                        overallMeasure.setSum(getScalarValue(totalValues.get(AggregationTypeEnum.SUM)));
                        overallMeasure.setCount(getScalarValue(totalValues.get(AggregationTypeEnum.COUNT)));

                        //calculate aggregated values from seriesValues
                        Map<AggregationTypeEnum, Map<Long, Double>> scalarValues = getScalarValues(aggregations);
                        scalarValues.entrySet().iterator().next().getValue().keySet().forEach(entry -> {
                            Measurement m = new Measurement(entry,
                                    getMeasurementValue(scalarValues, entry, AggregationTypeEnum.AVG),
                                    getMeasurementValue(scalarValues, entry, AggregationTypeEnum.MIN),
                                    getMeasurementValue(scalarValues, entry, AggregationTypeEnum.MAX),
                                    getMeasurementValue(scalarValues, entry, AggregationTypeEnum.SUM),
                                    getMeasurementValue(scalarValues, entry, AggregationTypeEnum.COUNT)
                            );
                            overallMeasure.getMeasurements().add(m);
                        });
                        chartDashlet.getMeasures().add(overallMeasure);

                        //iterate over every entityId
                        baseResult.getDataPoints().forEach((key, value) -> {
                            Map<AggregationTypeEnum, Double> totalValuesPerDataPoint = tm.getAggregationTypes().stream()
                                    .collect(Collectors.toMap(Function.identity(),
                                            aggregation -> totalValues.get(aggregation).getDataPoints().get(key).entrySet().iterator().next().getValue(),
                                            (a, b) -> b, LinkedHashMap::new));

                            Measure measure = new Measure(handleEntityIdString(baseResult.getEntities(), key));
                            measure.setAggregation(translateAggregation(specTM.getAggregation()));
                            measure.setUnit(calculatedUnit.getValue());
                            measure.setColor(DEFAULT_COLOR);

                            measure.setAvg(totalValuesPerDataPoint.getOrDefault(AggregationTypeEnum.AVG, 0D));
                            measure.setMin(totalValuesPerDataPoint.getOrDefault(AggregationTypeEnum.MIN, 0D));
                            measure.setMax(totalValuesPerDataPoint.getOrDefault(AggregationTypeEnum.MAX, 0D));
                            measure.setSum(totalValuesPerDataPoint.getOrDefault(AggregationTypeEnum.SUM, 0D));
                            measure.setCount(totalValuesPerDataPoint.getOrDefault(AggregationTypeEnum.COUNT, 0D));

                            value.entrySet().stream()
                                    .filter(entry -> entry.getValue() != null)
                                    .forEach(entry -> {
                                        Measurement m = new Measurement(entry.getKey(),
                                                getAggregationValue(aggregations.get(AggregationTypeEnum.AVG), key, entry.getKey()),
                                                getAggregationValue(aggregations.get(AggregationTypeEnum.MIN), key, entry.getKey()),
                                                getAggregationValue(aggregations.get(AggregationTypeEnum.MAX), key, entry.getKey()),
                                                getAggregationValue(aggregations.get(AggregationTypeEnum.SUM), key, entry.getKey()),
                                                getAggregationValue(aggregations.get(AggregationTypeEnum.COUNT), key, entry.getKey())
                                        );
                                        measure.getMeasurements().add(m);
                                    });
                            chartDashlet.getMeasures().add(measure);
                        });
                        dashboardReport.addChartDashlet(chartDashlet);
                    } else {
                        println(String.format("Timeseries %s has no data points", tm.getTimeseriesId()));
                    }
                });
                dashboardReports.add(dashboardReport);

                PrintStream stream = Optional.ofNullable(DynatraceUtils.getTaskListener(getContext())).map(TaskListener::getLogger).orElseGet(() -> new PrintStream(System.out));
                PerfSigUIUtils.handleIncidents(run, dashboardReport.getIncidents(), PerfSigUIUtils.createLogger(stream), step.getNonFunctionalFailure());
            }
        } finally {
            println("created " + dashboardReports.size() + " DashboardReports");

            PerfSigBuildAction action = new PerfSigBuildAction(dashboardReports);
            run.addAction(action);
        }
        return null;
    }

    private Specification getSpecifications() throws IOException, InterruptedException {
        Specification specification = new Specification();
        if (ws != null && StringUtils.isNotBlank(step.getSpecFile())) {
            FilePath f = ws.child(step.getSpecFile());
            if (f.exists() && !f.isDirectory()) {
                try (InputStream is = f.read()) {
                    Type type = new TypeToken<Specification>() {
                    }.getType();
                    return new Gson().fromJson(IOUtils.toString(is, StandardCharsets.UTF_8), type);
                }
            } else if (f.isDirectory()) {
                throw new IllegalArgumentException(f.getRemote() + "  is a directory ...");
            } else if (!f.exists()) {
                throw new FileNotFoundException(f.getRemote() + " does not exist ...");
            }
            return specification;
        } else {
            specification.setTimeseries(step.getMetrics().stream().map(metric -> new SpecificationTM(metric.getMetricId())).collect(Collectors.toList()));
            return specification;
        }
    }

    private void println(String message) {
        TaskListener listener = DynatraceUtils.getTaskListener(getContext());
        if (listener == null) {
            LOGGER.log(Level.FINE, "failed to print message {0} due to null TaskListener", message);
        } else {
            PerfSigUIUtils.createLogger(listener.getLogger()).log(message);
        }
    }
}
