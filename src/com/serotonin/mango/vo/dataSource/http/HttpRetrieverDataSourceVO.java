/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.vo.dataSource.http;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.serotonin.db.KeyValuePair;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.http.HttpRetrieverDataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import org.scada_lts.ds.model.ReactivationDs;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class HttpRetrieverDataSourceVO extends DataSourceVO<HttpRetrieverDataSourceVO> implements ICheckReactivation{
    public static final Type TYPE = Type.HTTP_RETRIEVER;

    @Override
    protected void addEventTypes(List<EventTypeVO> ets) {
        ets.add(createEventType(HttpRetrieverDataSourceRT.DATA_RETRIEVAL_FAILURE_EVENT, new LocalizableMessage(
                "event.ds.dataRetrieval")));
        ets.add(createEventType(HttpRetrieverDataSourceRT.PARSE_EXCEPTION_EVENT, new LocalizableMessage(
                "event.ds.dataParse")));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        EVENT_CODES.addElement(HttpRetrieverDataSourceRT.DATA_RETRIEVAL_FAILURE_EVENT, "DATA_RETRIEVAL_FAILURE");
        EVENT_CODES.addElement(HttpRetrieverDataSourceRT.PARSE_EXCEPTION_EVENT, "PARSE_EXCEPTION");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public LocalizableMessage getConnectionDescription() {
        return new LocalizableMessage("common.default", StringUtils.truncate(url, 30, " ..."));
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public DataSourceRT createDataSourceRT() {
        return new HttpRetrieverDataSourceRT(this);
    }

    @Override
    public HttpRetrieverPointLocatorVO createPointLocator() {
        return new HttpRetrieverPointLocatorVO();
    }

    @JsonRemoteProperty
    private String url;
    private int updatePeriodType = Common.TimePeriods.MINUTES;
    @JsonRemoteProperty
    private int updatePeriods = 5;
    @JsonRemoteProperty
    private int timeoutSeconds = 30;
    @JsonRemoteProperty
    private int retries = 2;
    @JsonRemoteProperty(innerType = KeyValuePair.class)
    private List<KeyValuePair> staticHeaders = new ArrayList<KeyValuePair>();

    @JsonRemoteProperty
    private boolean stop = false;

    @JsonRemoteProperty
    private ReactivationDs reactivation = new ReactivationDs();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUpdatePeriodType() {
        return updatePeriodType;
    }

    public void setUpdatePeriodType(int updatePeriodType) {
        this.updatePeriodType = updatePeriodType;
    }

    public int getUpdatePeriods() {
        return updatePeriods;
    }

    public void setUpdatePeriods(int updatePeriods) {
        this.updatePeriods = updatePeriods;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public ReactivationDs getReactivation() {
        return reactivation;
    }

    public void setReactivation(ReactivationDs reactivation) {
        this.reactivation = reactivation;
    }

    public List<KeyValuePair> getStaticHeaders() {
        return staticHeaders;
    }

    public void setStaticHeaders(List<KeyValuePair> staticHeaders) {
        this.staticHeaders = staticHeaders;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);
        if (StringUtils.isEmpty(url))
            response.addContextualMessage("url", "validate.required");
        if (!Common.TIME_PERIOD_CODES.isValidId(updatePeriodType))
            response.addContextualMessage("updatePeriodType", "validate.invalidValue");
        if (updatePeriods <= 0)
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        if (timeoutSeconds <= 0)
            response.addContextualMessage("updatePeriods", "validate.greaterThanZero");
        if (retries < 0)
            response.addContextualMessage("retries", "validate.cannotBeNegative");
    }

    @Override
    protected void addPropertiesImpl(List<LocalizableMessage> list) {
        AuditEventType.addPeriodMessage(list, "dsEdit.updatePeriod", updatePeriodType, updatePeriods);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.url", url);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.timeout", timeoutSeconds);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.retries", retries);
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.stop", stop );
        AuditEventType.addPropertyMessage(list, "dsEdit.httpRetriever.reactivation", reactivation );
    }

    @Override
    protected void addPropertyChangesImpl(List<LocalizableMessage> list, HttpRetrieverDataSourceVO from) {
        AuditEventType.maybeAddPeriodChangeMessage(list, "dsEdit.updatePeriod", from.updatePeriodType,
                from.updatePeriods, updatePeriodType, updatePeriods);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.url", from.url, url);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.timeout", from.timeoutSeconds,
                timeoutSeconds);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.retries", from.retries, retries);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.stop", from.stop, stop);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.httpRetriever.reactivation", from.stop, stop);
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, url);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeInt(timeoutSeconds);
        out.writeInt(retries);
        out.writeBoolean(stop);
        out.writeObject(reactivation);
        out.writeObject(staticHeaders);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            url = SerializationHelper.readSafeUTF(in);
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            timeoutSeconds = in.readInt();

            retries = in.readInt();
            try {
                stop = in.readBoolean();
            } catch (Exception e) {
                stop = false;
            }
            try {
                reactivation = (ReactivationDs) in.readObject();
            } catch (Exception e) {
                reactivation = new ReactivationDs(false, (short) 1,(short) 1);
            }
        } else if (ver == 2) {
            url = SerializationHelper.readSafeUTF(in);
            updatePeriodType = in.readInt();
            updatePeriods = in.readInt();
            timeoutSeconds = in.readInt();

            retries = in.readInt();
            try {
                stop = in.readBoolean();
            } catch (Exception e) {
                stop = false;
            }
            try {
                reactivation = (ReactivationDs) in.readObject();
            } catch (Exception e) {
                reactivation = new ReactivationDs(false, (short) 1,(short) 1);
            }

            staticHeaders = (List<KeyValuePair>) in.readObject();
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        Integer value = deserializeUpdatePeriodType(json);
        if (value != null)
            updatePeriodType = value;
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        serializeUpdatePeriodType(map, updatePeriodType);
    }

    public boolean checkToTrayEnable() {
        return isEnabled() || isStop() || reactivation.isSleep();
    }
}
