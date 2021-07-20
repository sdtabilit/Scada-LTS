package org.scada_lts.web.mvc.api;

import org.scada_lts.utils.AggregateUtils;

public class AggregateSettings {

    private Boolean enabled;
    private Integer valuesLimit;
    private Double limitFactor;

    public AggregateSettings() {}

    public AggregateSettings(Boolean enabled, Integer valuesLimit, Double limitFactor) {
        this.enabled = enabled;
        this.valuesLimit = valuesLimit;
        this.limitFactor = limitFactor;
    }

    public static AggregateSettings fromEnvProperties() {
        return new AggregateSettings(AggregateUtils.isEnabled(), AggregateUtils.getValuesLimit(), AggregateUtils.getLimitFactor());
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Integer getValuesLimit() {
        return valuesLimit;
    }

    public Double getLimitFactor() {
        return limitFactor;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setValuesLimit(Integer valuesLimit) {
        this.valuesLimit = valuesLimit;
    }

    public void setLimitFactor(Double limitFactor) {
        this.limitFactor = limitFactor;
    }
}
