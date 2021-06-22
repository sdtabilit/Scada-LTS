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
package com.serotonin.mango.vo.publish.httpSender;

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
import com.serotonin.mango.rt.event.AlarmLevels;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.publish.PublisherRT;
import com.serotonin.mango.rt.publish.httpSender.HttpSenderRT;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
public class HttpSenderVO extends PublisherVO<HttpPointVO> {
    @Override
    protected void getEventTypesImpl(List<EventTypeVO> eventTypes) {
        eventTypes.add(new EventTypeVO(EventType.EventSources.PUBLISHER, getId(), HttpSenderRT.SEND_EXCEPTION_EVENT,
                new LocalizableMessage("event.pb.httpSend"), AlarmLevels.URGENT));
        eventTypes.add(new EventTypeVO(EventType.EventSources.PUBLISHER, getId(), HttpSenderRT.RESULT_WARNINGS_EVENT,
                new LocalizableMessage("event.pb.resultWarnings"), AlarmLevels.INFORMATION));
    }

    private static final ExportCodes EVENT_CODES = new ExportCodes();
    static {
        PublisherVO.addDefaultEventCodes(EVENT_CODES);
        EVENT_CODES.addElement(HttpSenderRT.SEND_EXCEPTION_EVENT, "SEND_EXCEPTION_EVENT");
        EVENT_CODES.addElement(HttpSenderRT.RESULT_WARNINGS_EVENT, "RESULT_WARNINGS_EVENT");
    }

    public static final int DATE_FORMAT_BASIC = 1;
    public static final int DATE_FORMAT_TZ = 2;
    public static final int DATE_FORMAT_UTC = 3;

    private static ExportCodes DATE_FORMAT_CODES = new ExportCodes();
    static {
        DATE_FORMAT_CODES.addElement(DATE_FORMAT_BASIC, "DATE_FORMAT_BASIC",
                "publisherEdit.httpSender.dateFormat.basic");
        DATE_FORMAT_CODES.addElement(DATE_FORMAT_TZ, "DATE_FORMAT_TZ", "publisherEdit.httpSender.dateFormat.tz");
        DATE_FORMAT_CODES.addElement(DATE_FORMAT_UTC, "DATE_FORMAT_UTC", "publisherEdit.httpSender.dateFormat.utc");
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public LocalizableMessage getConfigDescription() {
        return new LocalizableMessage("common.default", url);
    }

    @Override
    public Type getType() {
        return PublisherVO.Type.HTTP_SENDER;
    }

    @Override
    public PublisherRT<HttpPointVO> createPublisherRT() {
        return new HttpSenderRT(this);
    }

    @Override
    protected HttpPointVO createPublishedPointInstance() {
        return new HttpPointVO();
    }

    @JsonRemoteProperty
    private String url;
    @JsonRemoteProperty
    private boolean usePost;
    @JsonRemoteProperty(innerType = KeyValuePair.class)
    private List<KeyValuePair> staticHeaders = new ArrayList<KeyValuePair>();
    @JsonRemoteProperty(innerType = KeyValuePair.class)
    private List<KeyValuePair> staticParameters = new ArrayList<KeyValuePair>();
    @JsonRemoteProperty
    private boolean raiseResultWarning = true;
    private int dateFormat = DATE_FORMAT_BASIC;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isUsePost() {
        return usePost;
    }

    public void setUsePost(boolean usePost) {
        this.usePost = usePost;
    }

    public List<KeyValuePair> getStaticHeaders() {
        return staticHeaders;
    }

    public void setStaticHeaders(List<KeyValuePair> staticHeaders) {
        this.staticHeaders = staticHeaders;
    }

    public List<KeyValuePair> getStaticParameters() {
        return staticParameters;
    }

    public void setStaticParameters(List<KeyValuePair> staticParameters) {
        this.staticParameters = staticParameters;
    }

    public boolean isRaiseResultWarning() {
        return raiseResultWarning;
    }

    public void setRaiseResultWarning(boolean raiseResultWarning) {
        this.raiseResultWarning = raiseResultWarning;
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);

        if (StringUtils.isEmpty(url))
            response.addContextualMessage("url", "validate.required");

        for (HttpPointVO point : points) {
            if (StringUtils.isEmpty(point.getParameterName())) {
                response.addContextualMessage("points", "validate.parameterRequired");
                break;
            }
        }

        if (!DATE_FORMAT_CODES.isValidId(dateFormat))
            response.addContextualMessage("dateFormat", "validate.invalidValue");
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, url);
        out.writeBoolean(usePost);
        out.writeObject(staticHeaders);
        out.writeObject(staticParameters);
        out.writeBoolean(raiseResultWarning);
        out.writeInt(dateFormat);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            url = SerializationHelper.readSafeUTF(in);
            usePost = in.readBoolean();
            staticHeaders = new ArrayList<KeyValuePair>();
            staticParameters = (List<KeyValuePair>) in.readObject();
            raiseResultWarning = in.readBoolean();
            dateFormat = DATE_FORMAT_BASIC;
        }
        else if (ver == 2) {
            url = SerializationHelper.readSafeUTF(in);
            usePost = in.readBoolean();
            staticHeaders = (List<KeyValuePair>) in.readObject();
            staticParameters = (List<KeyValuePair>) in.readObject();
            raiseResultWarning = in.readBoolean();
            dateFormat = DATE_FORMAT_BASIC;
        }
        else if (ver == 3) {
            url = SerializationHelper.readSafeUTF(in);
            usePost = in.readBoolean();
            staticHeaders = (List<KeyValuePair>) in.readObject();
            staticParameters = (List<KeyValuePair>) in.readObject();
            raiseResultWarning = in.readBoolean();
            dateFormat = in.readInt();
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("dateFormat", DATE_FORMAT_CODES.getCode(dateFormat));
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException, LocalizableJsonException {
        super.jsonDeserialize(reader, json);

        String text = json.getString("dateFormat");
        if (text != null) {
            dateFormat = DATE_FORMAT_CODES.getId(text);
            if (dateFormat == -1)
                throw new LocalizableJsonException("emport.error.invalid", "dateFormat", text,
                        DATE_FORMAT_CODES.getCodeList());
        }
    }

    public boolean isUseJSON() {
        return staticHeaders.stream().anyMatch(o -> (o.getKey().equals("Content-Type") && o.getValue().equals("application/json")));
    }
}
