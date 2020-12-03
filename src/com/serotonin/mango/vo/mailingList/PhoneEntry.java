package com.serotonin.mango.vo.mailingList;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.util.StringUtils;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

@JsonRemoteEntity
public class PhoneEntry extends EmailRecipient {
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public int getRecipientType() {
        return EmailRecipient.TYPE_PHONE;
    }

    @Override
    public void appendAddresses(Set<String> addresses, DateTime sendTime) {
        appendAllAddresses(addresses);
    }

    @Override
    public void appendAllAddresses(Set<String> addresses) {
        return;
    }

    @Override
    public void appendPhones(Set<String> phone, DateTime sendTime) {
        appendAllPhones(phone);
    }

    @Override
    public void appendAllPhones(Set<String> phones) {
        phones.add(phone);
    }

    @Override
    public int getReferenceId() {
        return 0;
    }

    @Override
    public String getReferenceAddress() {
        return null;
    }

    @Override
    public String getReferencePhone() {
        return phone;
    }

    @Override
    public String toString() {return phone;}

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        map.put("phone", phone);
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);

        phone = json.getString("phone");
        if (StringUtils.isEmpty(phone))
            throw new LocalizableJsonException("emport.error.recipient.missing.reference", "phone");
    }
}
