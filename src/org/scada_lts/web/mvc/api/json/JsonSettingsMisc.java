package org.scada_lts.web.mvc.api.json;

import java.io.Serializable;

public class JsonSettingsMisc implements Serializable {

    private boolean groveLogging;
    private int eventPurgePeriodType;
    private int eventPurgePeriods;
    private int reportPurgePeriodType;
    private int reportPurgePeriods;
    private int uiPerformance;
    private int futureDateLimitPeriodType;
    private int futureDateLimitPeriods;

    public JsonSettingsMisc() {}

    public JsonSettingsMisc(boolean groveLogging, int eventPurgePeriodType, int eventPurgePeriods, int reportPurgePeriodType, int reportPurgePeriods, int uiPerformance, int futureDateLimitPeriodType, int futureDateLimitPeriods) {
        this.groveLogging = groveLogging;
        this.eventPurgePeriodType = eventPurgePeriodType;
        this.eventPurgePeriods = eventPurgePeriods;
        this.reportPurgePeriodType = reportPurgePeriodType;
        this.reportPurgePeriods = reportPurgePeriods;
        this.uiPerformance = uiPerformance;
        this.futureDateLimitPeriodType = futureDateLimitPeriodType;
        this.futureDateLimitPeriods = futureDateLimitPeriods;
    }

    public boolean isGroveLogging() {
        return groveLogging;
    }

    public void setGroveLogging(boolean groveLogging) {
        this.groveLogging = groveLogging;
    }

    public int getEventPurgePeriodType() {
        return eventPurgePeriodType;
    }

    public void setEventPurgePeriodType(int eventPurgePeriodType) {
        this.eventPurgePeriodType = eventPurgePeriodType;
    }

    public int getEventPurgePeriods() {
        return eventPurgePeriods;
    }

    public void setEventPurgePeriods(int eventPurgePeriods) {
        this.eventPurgePeriods = eventPurgePeriods;
    }

    public int getReportPurgePeriodType() {
        return reportPurgePeriodType;
    }

    public void setReportPurgePeriodType(int reportPurgePeriodType) {
        this.reportPurgePeriodType = reportPurgePeriodType;
    }

    public int getReportPurgePeriods() {
        return reportPurgePeriods;
    }

    public void setReportPurgePeriods(int reportPurgePeriods) {
        this.reportPurgePeriods = reportPurgePeriods;
    }

    public int getUiPerformance() {
        return uiPerformance;
    }

    public void setUiPerformance(int uiPerformance) {
        this.uiPerformance = uiPerformance;
    }

    public int getFutureDateLimitPeriodType() {
        return futureDateLimitPeriodType;
    }

    public void setFutureDateLimitPeriodType(int futureDateLimitPeriodType) {
        this.futureDateLimitPeriodType = futureDateLimitPeriodType;
    }

    public int getFutureDateLimitPeriods() {
        return futureDateLimitPeriods;
    }

    public void setFutureDateLimitPeriods(int futureDateLimitPeriods) {
        this.futureDateLimitPeriods = futureDateLimitPeriods;
    }
}
