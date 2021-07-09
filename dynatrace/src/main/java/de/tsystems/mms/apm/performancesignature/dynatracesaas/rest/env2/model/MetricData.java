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
 * A list of metrics and their data points.
 */
@ApiModel(description = "A list of metrics and their data points.")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class MetricData {
    public static final String SERIALIZED_NAME_NEXT_PAGE_KEY = "nextPageKey";
    public static final String SERIALIZED_NAME_TOTAL_COUNT = "totalCount";
    public static final String SERIALIZED_NAME_RESULT = "result";
    public static final String SERIALIZED_NAME_WARNINGS = "warnings";
    @SerializedName(SERIALIZED_NAME_NEXT_PAGE_KEY)
    private String nextPageKey;
    @SerializedName(SERIALIZED_NAME_TOTAL_COUNT)
    private Long totalCount;
    @SerializedName(SERIALIZED_NAME_RESULT)
    private List<MetricSeriesCollection> result = null;
    @SerializedName(SERIALIZED_NAME_WARNINGS)
    private List<String> warnings = null;

    public MetricData nextPageKey(String nextPageKey) {

        this.nextPageKey = nextPageKey;
        return this;
    }

    /**
     * The cursor for the next page of results. Has the value of &#x60;null&#x60; on the last page.   Use it in the **nextPageKey** query parameter to obtain subsequent pages of the result.
     *
     * @return nextPageKey
     **/
    @ApiModelProperty(required = true, value = "The cursor for the next page of results. Has the value of `null` on the last page.   Use it in the **nextPageKey** query parameter to obtain subsequent pages of the result.")

    public String getNextPageKey() {
        return nextPageKey;
    }

    public void setNextPageKey(String nextPageKey) {
        this.nextPageKey = nextPageKey;
    }

    public MetricData totalCount(Long totalCount) {

        this.totalCount = totalCount;
        return this;
    }

    /**
     * The total number of primary entities in the result.   Has the &#x60;0&#x60; value if none of the requested metrics is suitable for pagination.
     *
     * @return totalCount
     **/
    @ApiModelProperty(required = true, value = "The total number of primary entities in the result.   Has the `0` value if none of the requested metrics is suitable for pagination.")

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public MetricData result(List<MetricSeriesCollection> result) {

        this.result = result;
        return this;
    }

    public MetricData addResultItem(MetricSeriesCollection resultItem) {
        if (this.result == null) {
            this.result = new ArrayList<>();
        }
        this.result.add(resultItem);
        return this;
    }

    /**
     * A list of metrics and their data points.
     *
     * @return result
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A list of metrics and their data points.")

    public List<MetricSeriesCollection> getResult() {
        return result;
    }

    public void setResult(List<MetricSeriesCollection> result) {
        this.result = result;
    }

    public MetricData warnings(List<String> warnings) {

        this.warnings = warnings;
        return this;
    }

    public MetricData addWarningsItem(String warningsItem) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warningsItem);
        return this;
    }

    /**
     * A list of warnings
     *
     * @return warnings
     **/
    @javax.annotation.Nullable
    @ApiModelProperty(value = "A list of warnings")

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
        MetricData metricData = (MetricData) o;
        return Objects.equals(this.nextPageKey, metricData.nextPageKey) &&
                Objects.equals(this.totalCount, metricData.totalCount) &&
                Objects.equals(this.result, metricData.result) &&
                Objects.equals(this.warnings, metricData.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nextPageKey, totalCount, result, warnings);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetricData {\n");
        sb.append("    nextPageKey: ").append(toIndentedString(nextPageKey)).append("\n");
        sb.append("    totalCount: ").append(toIndentedString(totalCount)).append("\n");
        sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

