package org.scada_lts.mango.service;

import br.org.scadabr.db.configuration.ConfigurationDB;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.beans.IntegerPair;
import org.scada_lts.dao.SystemSettingsDAO;
import org.scada_lts.web.mvc.api.json.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on the WatchListService created by Grzegorz Bylica
 * @author Radoslaw Jajko
 */
@Service
public class SystemSettingsService {

    private SystemSettingsDAO systemSettingsDAO;

    public SystemSettingsService() {
        systemSettingsDAO = new SystemSettingsDAO();
    }

    public Map<String, Object> getSettings() {
        Map<String, Object> settings = new HashMap<>();

        settings.put("emailSettings", this.getEmailSettings());
        settings.put("httpSettings", this.getHttpSettings());
        settings.put("miscSettings", this.getMiscSettings());
        settings.put("systemInfoSettings", this.getSystemInfoSettings());
        settings.put("systemEventTypes", this.getSystemEventAlarmLevels());
        settings.put("auditEventTypes", this.getAuditEventAlarmLevels());
        settings.put("databaseType", this.getDatabaseType());

        return settings;
    }

    public JsonSettingsEmail getEmailSettings() {
        JsonSettingsEmail json = new JsonSettingsEmail();
        json.setAuth(SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.EMAIL_AUTHORIZATION));
        json.setTls(SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.EMAIL_TLS));
        json.setContentType(SystemSettingsDAO.getIntValue(SystemSettingsDAO.EMAIL_CONTENT_TYPE));
        json.setPort(SystemSettingsDAO.getIntValue(SystemSettingsDAO.EMAIL_SMTP_PORT));
        json.setFrom(SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_FROM_ADDRESS));
        json.setHost(SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_SMTP_HOST));
        json.setName(SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_FROM_NAME));
        json.setUsername(SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_SMTP_USERNAME));
        json.setPassword(SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_SMTP_PASSWORD));
        return json;
    }

    public void saveEmailSettings(JsonSettingsEmail json) {
        systemSettingsDAO.setBooleanValue(SystemSettingsDAO.EMAIL_AUTHORIZATION, json.isAuth());
        systemSettingsDAO.setBooleanValue(SystemSettingsDAO.EMAIL_TLS, json.isTls());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.EMAIL_CONTENT_TYPE, json.getContentType());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.EMAIL_SMTP_PORT, json.getPort());
        systemSettingsDAO.setValue(SystemSettingsDAO.EMAIL_SMTP_HOST, json.getHost());
        systemSettingsDAO.setValue(SystemSettingsDAO.EMAIL_SMTP_USERNAME, json.getUsername());
        systemSettingsDAO.setValue(SystemSettingsDAO.EMAIL_SMTP_PASSWORD, json.getPassword());
        systemSettingsDAO.setValue(SystemSettingsDAO.EMAIL_FROM_ADDRESS, json.getFrom());
        systemSettingsDAO.setValue(SystemSettingsDAO.EMAIL_FROM_NAME, json.getName());
    }

    public JsonSettingsHttp getHttpSettings() {
        JsonSettingsHttp json = new JsonSettingsHttp();
        json.setUseProxy(SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.HTTP_CLIENT_USE_PROXY));
        json.setPort(SystemSettingsDAO.getIntValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_PORT));
        json.setHost(SystemSettingsDAO.getValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_SERVER));
        json.setUsername(SystemSettingsDAO.getValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_USERNAME));
        json.setPassword(SystemSettingsDAO.getValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_PASSWORD));
        return json;
    }

    public void saveHttpSettings(JsonSettingsHttp json) {
        systemSettingsDAO.setBooleanValue(SystemSettingsDAO.HTTP_CLIENT_USE_PROXY, json.isUseProxy());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_PORT, json.getPort());
        systemSettingsDAO.setValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_SERVER, json.getHost());
        systemSettingsDAO.setValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_USERNAME, json.getUsername());
        systemSettingsDAO.setValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_PASSWORD, json.getPassword());
    }

    public JsonSettingsMisc getMiscSettings() {
        JsonSettingsMisc json = new JsonSettingsMisc();
        json.setGroveLogging(SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.GROVE_LOGGING));
        json.setEventPurgePeriodType(SystemSettingsDAO.getIntValue(SystemSettingsDAO.EVENT_PURGE_PERIOD_TYPE));
        json.setEventPurgePeriods(SystemSettingsDAO.getIntValue(SystemSettingsDAO.EVENT_PURGE_PERIODS));
        json.setReportPurgePeriodType(SystemSettingsDAO.getIntValue(SystemSettingsDAO.REPORT_PURGE_PERIOD_TYPE));
        json.setReportPurgePeriods(SystemSettingsDAO.getIntValue(SystemSettingsDAO.REPORT_PURGE_PERIODS));
        json.setFutureDateLimitPeriodType(SystemSettingsDAO.getIntValue(SystemSettingsDAO.FUTURE_DATE_LIMIT_PERIOD_TYPE));
        json.setFutureDateLimitPeriods(SystemSettingsDAO.getIntValue(SystemSettingsDAO.FUTURE_DATE_LIMIT_PERIODS));
        json.setUiPerformance(SystemSettingsDAO.getIntValue(SystemSettingsDAO.UI_PERFORMANCE));
        return json;
    }

    public  void saveMiscSettings(JsonSettingsMisc json) {
        systemSettingsDAO.setBooleanValue(SystemSettingsDAO.GROVE_LOGGING, json.isGroveLogging());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.EVENT_PURGE_PERIOD_TYPE, json.getEventPurgePeriodType());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.EVENT_PURGE_PERIODS, json.getEventPurgePeriods());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.REPORT_PURGE_PERIOD_TYPE, json.getReportPurgePeriodType());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.REPORT_PURGE_PERIODS, json.getReportPurgePeriods());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.FUTURE_DATE_LIMIT_PERIOD_TYPE, json.getFutureDateLimitPeriodType());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.FUTURE_DATE_LIMIT_PERIODS, json.getFutureDateLimitPeriods());
        systemSettingsDAO.setIntValue(SystemSettingsDAO.UI_PERFORMANCE, json.getUiPerformance());
    }

    public List<JsonSettingsEventLevels> getAuditEventAlarmLevels() {
        List<EventTypeVO> auditTypeVoList = AuditEventType.getAuditEventTypes();
        List<JsonSettingsEventLevels> json =  new ArrayList<JsonSettingsEventLevels>();
        for (EventTypeVO eventTypeVO : auditTypeVoList) {
            JsonSettingsEventLevels ip = new JsonSettingsEventLevels();
            ip.setI1(eventTypeVO.getTypeRef1());
            ip.setI2(eventTypeVO.getAlarmLevel());
            ip.setTranslation(eventTypeVO.getDescription().getKey());
            json.add(ip);
        }
        return json;
    }

    public void saveAuditEventAlarmLevels(List<JsonSettingsEventLevels> eventAlarmLevels) {
        for (JsonSettingsEventLevels eventAlarmLevel : eventAlarmLevels) {
            AuditEventType.setEventTypeAlarmLevel(eventAlarmLevel.getI1(), eventAlarmLevel.getI2());
        }
    }

    public List<JsonSettingsEventLevels> getSystemEventAlarmLevels() {
        List<EventTypeVO> eventTypeVoList = SystemEventType.getSystemEventTypes();
        List<JsonSettingsEventLevels> json =  new ArrayList<JsonSettingsEventLevels>();
        for (EventTypeVO eventTypeVO : eventTypeVoList) {
            JsonSettingsEventLevels ip = new JsonSettingsEventLevels();
            ip.setI1(eventTypeVO.getTypeRef1());
            ip.setI2(eventTypeVO.getAlarmLevel());
            ip.setTranslation(eventTypeVO.getDescription().getKey());
            json.add(ip);
        }
        return json;
    }

    public void saveSystemEventAlarmLevels(List<JsonSettingsEventLevels> eventAlarmLevels) {
        for (JsonSettingsEventLevels eventAlarmLevel : eventAlarmLevels) {
            SystemEventType.setEventTypeAlarmLevel(eventAlarmLevel.getI1(), eventAlarmLevel.getI2());
        }
    }

    public JsonSettingsSystemInfo getSystemInfoSettings() {
        JsonSettingsSystemInfo json = new JsonSettingsSystemInfo();
        json.setNewVersionNotificationLevel(SystemSettingsDAO.getValue(SystemSettingsDAO.NEW_VERSION_NOTIFICATION_LEVEL));
        json.setInstanceDescription(SystemSettingsDAO.getValue(SystemSettingsDAO.INSTANCE_DESCRIPTION));
        json.setLanguage(SystemSettingsDAO.getValue(SystemSettingsDAO.LANGUAGE));
        return json;
    }

    public void saveSystemInfoSettings(JsonSettingsSystemInfo json) {
        systemSettingsDAO.setValue(SystemSettingsDAO.NEW_VERSION_NOTIFICATION_LEVEL, json.getNewVersionNotificationLevel());
        systemSettingsDAO.setValue(SystemSettingsDAO.INSTANCE_DESCRIPTION, json.getInstanceDescription());
        systemSettingsDAO.setValue(SystemSettingsDAO.LANGUAGE, json.getLanguage());
    }

    public String getDatabaseType() {
        return Common.getEnvironmentProfile().getString("db.type", "derby");
    }

    public void setDatabaseType(String databaseType) {
        if (databaseType.equalsIgnoreCase("mysql")) {
            ConfigurationDB.useMysqlDB();
        } else if (databaseType.equalsIgnoreCase("mssql")) {
            ConfigurationDB.useMssqlDB();
        } else if (databaseType.equalsIgnoreCase("oracle11g")) {
            ConfigurationDB.useOracle11gDB();
        } else {
            ConfigurationDB.useDerbyDB();
        }
    }

}