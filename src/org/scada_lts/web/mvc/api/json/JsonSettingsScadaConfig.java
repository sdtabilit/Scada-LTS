package org.scada_lts.web.mvc.api.json;

import java.io.Serializable;

public class JsonSettingsScadaConfig implements Serializable {

    boolean apiReplaceAlertOnView;

    boolean cacheEnable;
    int startUpdateUnsilencedAlarmLevel;
    int startUpdateEventDetectors;
    int startUpdatePendingEvents;
    int millisSecondsPeriodUpdateUnsilencedAlarmLevel;
    int millisSecondsPeriodUpdateEventDetectors;
    int millisSecondsPeriodUpdatePendingEvents;
    String croneUpdateCachePointHierarchy;

    String croneUpdateDataSourcesPoints;
    boolean useCacheDataSourcePointsWhenSystemIsReady;

    boolean useAcl;
    String aclServer;

    boolean doNotCreateEventsForEmailError;
    boolean httpRetriverSleepCheckToReactivationWhenStart;
    boolean httpRetriverDoNotAllowEnableReactivation;

    public JsonSettingsScadaConfig() {
    }

    public JsonSettingsScadaConfig(boolean cacheEnable, boolean apiReplaceAlertOnView, int startUpdateUnsilencedAlarmLevel, int startUpdateEventDetectors, int startUpdatePendingEvents, int millisSecondsPeriodUpdateUnsilencedAlarmLevel, int millisSecondsPeriodUpdateEventDetectors, int millisSecondsPeriodUpdatePendingEvents, String croneUpdateCachePointHierarchy, String croneUpdateDataSourcesPoints, boolean useCacheDataSourcePointsWhenSystemIsReady, boolean useAcl, String aclServer, boolean doNotCreateEventsForEmailError, boolean httpRetriverSleepCheckToReactivationWhenStart, boolean httpRetriverDoNotAllowEnableReactivation) {
        this.cacheEnable = cacheEnable;
        this.apiReplaceAlertOnView = apiReplaceAlertOnView;
        this.startUpdateUnsilencedAlarmLevel = startUpdateUnsilencedAlarmLevel;
        this.startUpdateEventDetectors = startUpdateEventDetectors;
        this.startUpdatePendingEvents = startUpdatePendingEvents;
        this.millisSecondsPeriodUpdateUnsilencedAlarmLevel = millisSecondsPeriodUpdateUnsilencedAlarmLevel;
        this.millisSecondsPeriodUpdateEventDetectors = millisSecondsPeriodUpdateEventDetectors;
        this.millisSecondsPeriodUpdatePendingEvents = millisSecondsPeriodUpdatePendingEvents;
        this.croneUpdateCachePointHierarchy = croneUpdateCachePointHierarchy;
        this.croneUpdateDataSourcesPoints = croneUpdateDataSourcesPoints;
        this.useCacheDataSourcePointsWhenSystemIsReady = useCacheDataSourcePointsWhenSystemIsReady;
        this.useAcl = useAcl;
        this.aclServer = aclServer;
        this.doNotCreateEventsForEmailError = doNotCreateEventsForEmailError;
        this.httpRetriverSleepCheckToReactivationWhenStart = httpRetriverSleepCheckToReactivationWhenStart;
        this.httpRetriverDoNotAllowEnableReactivation = httpRetriverDoNotAllowEnableReactivation;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public boolean isApiReplaceAlertOnView() {
        return apiReplaceAlertOnView;
    }

    public void setApiReplaceAlertOnView(boolean apiReplaceAlertOnView) {
        this.apiReplaceAlertOnView = apiReplaceAlertOnView;
    }

    public int getStartUpdateUnsilencedAlarmLevel() {
        return startUpdateUnsilencedAlarmLevel;
    }

    public void setStartUpdateUnsilencedAlarmLevel(int startUpdateUnsilencedAlarmLevel) {
        this.startUpdateUnsilencedAlarmLevel = startUpdateUnsilencedAlarmLevel;
    }

    public int getStartUpdateEventDetectors() {
        return startUpdateEventDetectors;
    }

    public void setStartUpdateEventDetectors(int startUpdateEventDetectors) {
        this.startUpdateEventDetectors = startUpdateEventDetectors;
    }

    public int getStartUpdatePendingEvents() {
        return startUpdatePendingEvents;
    }

    public void setStartUpdatePendingEvents(int startUpdatePendingEvents) {
        this.startUpdatePendingEvents = startUpdatePendingEvents;
    }

    public int getMillisSecondsPeriodUpdateUnsilencedAlarmLevel() {
        return millisSecondsPeriodUpdateUnsilencedAlarmLevel;
    }

    public void setMillisSecondsPeriodUpdateUnsilencedAlarmLevel(int millisSecondsPeriodUpdateUnsilencedAlarmLevel) {
        this.millisSecondsPeriodUpdateUnsilencedAlarmLevel = millisSecondsPeriodUpdateUnsilencedAlarmLevel;
    }

    public int getMillisSecondsPeriodUpdateEventDetectors() {
        return millisSecondsPeriodUpdateEventDetectors;
    }

    public void setMillisSecondsPeriodUpdateEventDetectors(int millisSecondsPeriodUpdateEventDetectors) {
        this.millisSecondsPeriodUpdateEventDetectors = millisSecondsPeriodUpdateEventDetectors;
    }

    public int getMillisSecondsPeriodUpdatePendingEvents() {
        return millisSecondsPeriodUpdatePendingEvents;
    }

    public void setMillisSecondsPeriodUpdatePendingEvents(int millisSecondsPeriodUpdatePendingEvents) {
        this.millisSecondsPeriodUpdatePendingEvents = millisSecondsPeriodUpdatePendingEvents;
    }

    public String getCroneUpdateCachePointHierarchy() {
        return croneUpdateCachePointHierarchy;
    }

    public void setCroneUpdateCachePointHierarchy(String croneUpdateCachePointHierarchy) {
        this.croneUpdateCachePointHierarchy = croneUpdateCachePointHierarchy;
    }

    public String getCroneUpdateDataSourcesPoints() {
        return croneUpdateDataSourcesPoints;
    }

    public void setCroneUpdateDataSourcesPoints(String croneUpdateDataSourcesPoints) {
        this.croneUpdateDataSourcesPoints = croneUpdateDataSourcesPoints;
    }

    public boolean isUseCacheDataSourcePointsWhenSystemIsReady() {
        return useCacheDataSourcePointsWhenSystemIsReady;
    }

    public void setUseCacheDataSourcePointsWhenSystemIsReady(boolean useCacheDataSourcePointsWhenSystemIsReady) {
        this.useCacheDataSourcePointsWhenSystemIsReady = useCacheDataSourcePointsWhenSystemIsReady;
    }

    public boolean isUseAcl() {
        return useAcl;
    }

    public void setUseAcl(boolean useAcl) {
        this.useAcl = useAcl;
    }

    public String getAclServer() {
        return aclServer;
    }

    public void setAclServer(String aclServer) {
        this.aclServer = aclServer;
    }

    public boolean isDoNotCreateEventsForEmailError() {
        return doNotCreateEventsForEmailError;
    }

    public void setDoNotCreateEventsForEmailError(boolean doNotCreateEventsForEmailError) {
        this.doNotCreateEventsForEmailError = doNotCreateEventsForEmailError;
    }

    public boolean isHttpRetriverSleepCheckToReactivationWhenStart() {
        return httpRetriverSleepCheckToReactivationWhenStart;
    }

    public void setHttpRetriverSleepCheckToReactivationWhenStart(boolean httpRetriverSleepCheckToReactivationWhenStart) {
        this.httpRetriverSleepCheckToReactivationWhenStart = httpRetriverSleepCheckToReactivationWhenStart;
    }

    public boolean isHttpRetriverDoNotAllowEnableReactivation() {
        return httpRetriverDoNotAllowEnableReactivation;
    }

    public void setHttpRetriverDoNotAllowEnableReactivation(boolean httpRetriverDoNotAllowEnableReactivation) {
        this.httpRetriverDoNotAllowEnableReactivation = httpRetriverDoNotAllowEnableReactivation;
    }
}
