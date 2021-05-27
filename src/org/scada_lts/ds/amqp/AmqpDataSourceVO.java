package org.scada_lts.ds.amqp;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.event.EventTypeVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class AmqpDataSourceVO extends DataSourceVO<AmqpDataSourceVO> {

    public static final Type TYPE = Type.AMQP;

    private static final ExportCodes EVENT_CODES = new ExportCodes();

    static {
        EVENT_CODES.addElement(AmqpDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, "DATA_SOURCE_EXCEPTION");
        EVENT_CODES.addElement(AmqpDataSourceRT.DATA_POINT_EXCEPTION_EVENT, "DATA_POINT_EXCEPTION");
    }

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5672";
    private static final String DEFAULT_NOT_SET = "";

    private int updatePeriodType = Common.TimePeriods.MINUTES;

    //Dependent of updatePeriodType (5 MINUTES update period)
    @JsonRemoteProperty
    private int updatePeriods = 5;
    @JsonRemoteProperty
    private int updateAttempts = 5;
    @JsonRemoteProperty
    private String serverIpAddress = DEFAULT_HOST;
    @JsonRemoteProperty
    private String serverPortNumber = DEFAULT_PORT;
    @JsonRemoteProperty
    private String serverVirtualHost = DEFAULT_NOT_SET;
    @JsonRemoteProperty
    private String serverUsername = DEFAULT_NOT_SET;
    @JsonRemoteProperty
    private String serverPassword = DEFAULT_NOT_SET;

    @Override
    public DataSourceRT createDataSourceRT() {
        return new AmqpDataSourceRT(this);
    }

    @Override
    public PointLocatorVO createPointLocator() {
        return new AmqpPointLocatorVO();
    }

    @Override
    public LocalizableMessage getConnectionDescription() {
        if (serverIpAddress.length() == 0 || serverPortNumber.length() == 0)
            return new LocalizableMessage("dsEdit.amqp");
        return null;
    }

    @Override
    public ExportCodes getEventCodes() {
        return EVENT_CODES;
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void validate(DwrResponseI18n response) {
        super.validate(response);
        if (serverIpAddress.isBlank()) {
            response.addContextualMessage("serverIpAddress","validate.required");
        }
        try {
            InetAddress.getByName(serverIpAddress);
        } catch (UnknownHostException e) {
            response.addContextualMessage("serverIpAddress","validate.invalidValue");
        }
        if (serverPortNumber.isBlank() || Integer.parseInt(serverPortNumber) < 0) {
            response.addContextualMessage("serverPortNumber","validate.invalidValue");
        }
        if (updateAttempts < 0 || updateAttempts > 10) {
            response.addContextualMessage("updateAttempts", "validate.updateAttempts");
        }

    }

    @Override
    protected void addEventTypes(List<EventTypeVO> eventTypes) {
        eventTypes.add(createEventType(AmqpDataSourceRT.DATA_SOURCE_EXCEPTION_EVENT, new LocalizableMessage("event.ds.dataSource") ));
        eventTypes.add(createEventType(AmqpDataSourceRT.DATA_POINT_EXCEPTION_EVENT, new LocalizableMessage("event.ds.amqpReceiver") ));

    }

    @Override
    protected void addPropertiesImpl(List<LocalizableMessage> list) {
        AuditEventType.addPeriodMessage(list, "dsEdit.updatePeriod", updatePeriodType, updatePeriods);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.serverIpAddress" , serverIpAddress);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.serverPortNumber" , serverPortNumber);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.serverVirtualHost" , serverVirtualHost);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.serverUsername" , serverUsername);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.serverPassword" , serverPassword);

    }

    @Override
    protected void addPropertyChangesImpl(List<LocalizableMessage> list, AmqpDataSourceVO from) {
        AuditEventType.maybeAddPeriodChangeMessage(list, "dsEdit.updatePeriod", from.updatePeriodType, from.updatePeriods ,updatePeriodType, updatePeriods);
        AuditEventType.maybeAddPropertyChangeMessage(list,"dsEdit.amqp.serverIpAddress",from.serverIpAddress,serverIpAddress);
        AuditEventType.maybeAddPropertyChangeMessage(list,"dsEdit.amqp.serverPortNumber",from.serverPortNumber,serverPortNumber);
        AuditEventType.maybeAddPropertyChangeMessage(list,"dsEdit.amqp.serverVirtualHost",from.serverVirtualHost,serverVirtualHost);
        AuditEventType.maybeAddPropertyChangeMessage(list,"dsEdit.amqp.serverUsername",from.serverUsername,serverUsername);
        AuditEventType.maybeAddPropertyChangeMessage(list,"dsEdit.amqp.serverPassword",from.serverPassword,serverPassword);
    }

    private static final int VERSION = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {

        out.writeInt(VERSION);
        out.writeInt(updatePeriodType);
        out.writeInt(updatePeriods);
        out.writeInt(updateAttempts);
        SerializationHelper.writeSafeUTF(out, serverIpAddress);
        SerializationHelper.writeSafeUTF(out, serverPortNumber);
        SerializationHelper.writeSafeUTF(out, serverVirtualHost);
        SerializationHelper.writeSafeUTF(out, serverUsername);
        SerializationHelper.writeSafeUTF(out, serverPassword);

    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();
        if (ver == 1) {
            updatePeriodType    = in.readInt();
            updatePeriods       = in.readInt();
            updateAttempts      = in.readInt();
            serverIpAddress     = SerializationHelper.readSafeUTF(in);
            serverPortNumber    = SerializationHelper.readSafeUTF(in);
            serverVirtualHost   = SerializationHelper.readSafeUTF(in);
            serverUsername      = SerializationHelper.readSafeUTF(in);
            serverPassword      = SerializationHelper.readSafeUTF(in);

        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map){
        super.jsonSerialize(map);
        serializeUpdatePeriodType(map, updatePeriodType);
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader,json);
        Integer value = deserializeUpdatePeriodType(json);
        if (value != null)
            updatePeriodType = value;
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

    public int getUpdateAttempts() {
        return updateAttempts;
    }

    public void setUpdateAttempts(int updateAttempts) {
        this.updateAttempts = updateAttempts;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public String getServerPortNumber() {
        return serverPortNumber;
    }

    public void setServerPortNumber(String serverPortNumber) {
        this.serverPortNumber = serverPortNumber;
    }

    public String getServerVirtualHost() {
        return serverVirtualHost;
    }

    public void setServerVirtualHost(String serverVirtualHost) {
        this.serverVirtualHost = serverVirtualHost;
    }

    public String getServerUsername() {
        return serverUsername;
    }

    public void setServerUsername(String serverUsername) {
        this.serverUsername = serverUsername;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }
}
