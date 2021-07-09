/*
 * Dynatrace Environment API
 *  Documentation of the Dynatrace Environment API v2. Resources here generally supersede those in v1. Migration of resources from v1 is in progress.   If you miss a resource, consider using the Dynatrace Environment API v1. To read about use cases and examples, see [Dynatrace Documentation](https://dt-url.net/2u23k1k) .  Notes about compatibility: * Operations marked as early adopter or preview may be changed in non-compatible ways, although we try to avoid this. * We may add new enum constants without incrementing the API version; thus, clients need to handle unknown enum constants gracefully.
 *
 * The version of the OpenAPI document: 2.0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package de.tsystems.mms.apm.performancesignature.dynatracesaas.rest.env2.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data points of a metric.
 */
@ApiModel(description = "Data points of a metric.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class MetricSeriesCollection {
    public static final String SERIALIZED_NAME_METRIC_ID = "metricId";
    public static final String SERIALIZED_NAME_DATA = "data";
    public static final String SERIALIZED_NAME_WARNINGS = "warnings";
    @SerializedName(SERIALIZED_NAME_METRIC_ID)
    private String metricId;
    @SerializedName(SERIALIZED_NAME_DATA)
    private List<MetricSeries> data = null;
    @SerializedName(SERIALIZED_NAME_WARNINGS)
    private List<String> warnings = null;

    public MetricSeriesCollection metricId(String metricId) {

        this.metricId = metricId;
        return this;
    }

    /**
     * The key of the metric.   If any transformation is applied, it is included here.
     *
     * @return metricId
     **/
    @ApiModelProperty(required = true, value = "The key of the metric.   If any transformation is applied, it is included here.")

    public String getMetricId() {
        return metricId;
    }

    public void setMetricId(String metricId) {
        this.metricId = metricId;
    }

    public MetricSeriesCollection data(List<MetricSeries> data) {

        this.data = data;
        return this;
    }

    public MetricSeriesCollection addDataItem(MetricSeries dataItem) {
        if (this.data == null) {
            this.data = new ArrayList<>();
        }
        this.data.add(dataItem);
        return this;
    }

    /**
     * Data points of the metric.
     *
     * @return data
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "Data points of the metric.")

    public List<MetricSeries> getData() {
        return data;
    }

    public void setData(List<MetricSeries> data) {
        this.data = data;
    }

    public MetricSeriesCollection warnings(List<String> warnings) {

        this.warnings = warnings;
        return this;
    }

    public MetricSeriesCollection addWarningsItem(String warningsItem) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warningsItem);
        return this;
    }

    /**
     * A list of potential warnings that affect this ID. For example deprecated feature usage etc.
     *
     * @return warnings
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A list of potential warnings that affect this ID. For example deprecated feature usage etc.")

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetricSeriesCollection metricSeriesCollection = (MetricSeriesCollection) o;
        return Objects.equals(this.metricId, metricSeriesCollection.metricId) &&
                Objects.equals(this.data, metricSeriesCollection.data) &&
                Objects.equals(this.warnings, metricSeriesCollection.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricId, data, warnings);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetricSeriesCollection {\n");
        sb.append("    metricId: ").append(toIndentedString(metricId)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}

