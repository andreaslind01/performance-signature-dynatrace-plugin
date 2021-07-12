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

package de.tsystems.mms.apm.performancesignature.dynatrace.model;

import de.tsystems.mms.apm.performancesignature.dynatrace.model.Alert.SeverityEnum;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dashboardreport")
@ExportedBean
public class DashboardReport {

    @XmlElementWrapper(name = "data")
    @XmlElement(name = "chartdashlet")
    private final List<ChartDashlet> chartDashlets;
    @XmlElement(name = "incident")
    private final List<Alert> incidents;
    private String name;
    private String clientUrl;

    public DashboardReport(final String testCaseName) {
        this.name = testCaseName;
        this.chartDashlets = new ArrayList<>();
        this.incidents = new ArrayList<>();
    }

    public DashboardReport() {
        this(null);
    }

    @Exported
    public List<Alert> getIncidents() {
        return incidents;
    }

    public Map<SeverityEnum, Map<String, List<Alert>>> getIncidentMap() {
        return incidents.stream()
                .collect(groupingBy(Alert::getSeverity,
                        groupingBy(alert -> String.format("%s Incident: %s", alert.getSeverity(), alert.getMessage()), TreeMap::new, Collectors.toList())));
    }

    public void addIncident(final Alert incident) {
        this.incidents.add(incident);
    }

    /**
     * Gets the value of the chartdashlet property.
     * <p>
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the chartdashlet property.
     * <p>
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChartdashlet().add(newItem);
     * </pre>
     * <p>
     * <p>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChartDashlet }
     */
    @Exported
    public List<ChartDashlet> getChartDashlets() {
        return this.chartDashlets;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    @Exported
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    public void addChartDashlet(final ChartDashlet chartDashlet) {
        this.chartDashlets.add(chartDashlet);
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public Measure getMeasure(final String chartDashlet, final String measure) {
        return chartDashlets.stream()
                .filter(cd -> cd.getName().equalsIgnoreCase(chartDashlet) && cd.getMeasures() != null)
                .flatMap(cd -> cd.getMeasures().stream()).filter(m -> m.getName().equalsIgnoreCase(measure))
                .findFirst().orElse(null);
    }
}
