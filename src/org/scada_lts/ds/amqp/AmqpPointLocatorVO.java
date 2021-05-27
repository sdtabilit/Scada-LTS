package org.scada_lts.ds.amqp;

import com.serotonin.json.*;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.rt.event.type.AuditEventType;
import com.serotonin.mango.vo.dataSource.AbstractPointLocatorVO;
import com.serotonin.util.SerializationHelper;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

public class AmqpPointLocatorVO extends AbstractPointLocatorVO implements JsonSerializable {

    private static final String DEFAULT_NOT_SET = "";
    private static final boolean DEFAULT_WRITABLE = true;

    @JsonRemoteProperty
    private boolean settable;
    @JsonRemoteProperty
    private boolean writable = DEFAULT_WRITABLE;
    @JsonRemoteProperty
    private ExchangeType exchangeType = ExchangeType.NONE;
    @JsonRemoteProperty
    private String exchangeName = "";
    @JsonRemoteProperty
    private String  queueName = DEFAULT_NOT_SET;
    @JsonRemoteProperty
    private String  routingKey = DEFAULT_NOT_SET;
    @JsonRemoteProperty
    private DurabilityType queueDurability = DurabilityType.DURABLE;
    @JsonRemoteProperty
    private MessageAckType messageAck = MessageAckType.NO_ACK;

    private int     dataTypeId;

    @Override
    public LocalizableMessage getConfigurationDescription() {
        return new LocalizableMessage("common.default", queueName);
    }

    @Override
    public PointLocatorRT createRuntime() {
        return new AmqpPointLocatorRT(this);
    }

    @Override
    public void validate(DwrResponseI18n response) {
        if (exchangeType == ExchangeType.NONE) {
            routingKey = "";
            exchangeName = "";
            if (queueName.isBlank()) {
                response.addContextualMessage("queueName", "validate.invalidValue");
            }
        }
        if (exchangeType == ExchangeType.DIRECT || exchangeType == ExchangeType.TOPIC){
            queueName = "";
            if (exchangeName.isBlank()) {
                response.addContextualMessage("exchangeName", "validate.invalidValue");
            }
            if (routingKey.isBlank()) {
                response.addContextualMessage("routingKey", "validate.invalidValue");
            }
        }

        if (exchangeType == ExchangeType.FANOUT) {
            routingKey = "";
            if (exchangeName.isBlank()) {
                response.addContextualMessage("exchangeName", "validate.invalidValue");
            }
        }

    }

    @Override
    public void addProperties(List<LocalizableMessage> list) {
        AuditEventType.addPropertyMessage(list, "dsEdit.settable", settable);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.exchangeType", exchangeType);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.exchangeName", exchangeName);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.queueName", queueName);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.queueDurability", queueDurability);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.routingKey", routingKey);
        AuditEventType.addPropertyMessage(list, "dsEdit.amqp.messageAck", messageAck);
    }

    @Override
    public void addPropertyChanges(List<LocalizableMessage> list, Object o) {
        AmqpPointLocatorVO from = (AmqpPointLocatorVO) o;
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.settable", from.settable, settable);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.amqp.exchangeType", from.exchangeType, exchangeType);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.amqp.exchangeName", from.exchangeName, exchangeName);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.amqp.queueName", from.queueName, queueName);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.amqp.queueDurability", from.queueDurability, queueDurability);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.amqp.routingKey", from.routingKey, routingKey);
        AuditEventType.maybeAddPropertyChangeMessage(list, "dsEdit.amqp.messageAck", from.messageAck, routingKey);
    }

    // Getters and Setters //
    @Override
    public int getDataTypeId() {
        return dataTypeId;
    }

    @Override
    public boolean isSettable() {
        return settable;
    }

    public void setSettable(boolean settable) {
        this.settable = settable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public DurabilityType getQueueDurability() {
        return queueDurability;
    }

    public void setQueueDurability(DurabilityType queueDurability) {
        this.queueDurability = queueDurability;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public MessageAckType getMessageAck() {
        return messageAck;
    }

    public void setMessageAck(MessageAckType messageAck) {
        this.messageAck = messageAck;
    }

    private static final int VERSION = 1;
    // Serialization //
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(VERSION);
        out.writeBoolean(settable);
        out.writeBoolean(writable);
        out.writeInt(dataTypeId);
        SerializationHelper.writeSafeUTF(out, exchangeType.name());
        SerializationHelper.writeSafeUTF(out, exchangeName);
        SerializationHelper.writeSafeUTF(out, queueName);
        SerializationHelper.writeSafeUTF(out, queueDurability.name());
        SerializationHelper.writeSafeUTF(out, routingKey);
        SerializationHelper.writeSafeUTF(out, messageAck.name());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();
        if(ver == 1) {
            settable        = in.readBoolean();
            writable        = in.readBoolean();
            dataTypeId      = in.readInt();
            exchangeType    = ExchangeType.valueOf(SerializationHelper.readSafeUTF(in));
            exchangeName    = SerializationHelper.readSafeUTF(in);
            queueName       = SerializationHelper.readSafeUTF(in);
            queueDurability = DurabilityType.valueOf(SerializationHelper.readSafeUTF(in));
            routingKey      = SerializationHelper.readSafeUTF(in);
            messageAck      = MessageAckType.valueOf(SerializationHelper.readSafeUTF(in));
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        serializeDataType(map);
    }

    @Override
    public void jsonDeserialize(JsonReader jsonReader, JsonObject jsonObject) throws JsonException {
        deserializeDataType(jsonObject);

    }

}
