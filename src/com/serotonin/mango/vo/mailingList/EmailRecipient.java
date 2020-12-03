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
package com.serotonin.mango.vo.mailingList;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.util.ExportCodes;

@JsonRemoteEntity(typeFactory = EmailRecipientFactory.class)
abstract public class EmailRecipient implements JsonSerializable {
    public static final int TYPE_MAILING_LIST = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_ADDRESS = 3;
    public static final int TYPE_PHONE = 4;

    public static final ExportCodes TYPE_CODES = new ExportCodes();
    static {
        TYPE_CODES.addElement(TYPE_MAILING_LIST, "MAILING_LIST", "mailingLists.mailingList");
        TYPE_CODES.addElement(TYPE_USER, "USER", "mailingLists.emailAddress");
        TYPE_CODES.addElement(TYPE_ADDRESS, "ADDRESS", "common.user");
        TYPE_CODES.addElement(TYPE_PHONE, "PHONE", "mailingLists.phoneNumber");
    }

    abstract public int getRecipientType();

    abstract public void appendAddresses(Set<String> addresses, DateTime sendTime);

    abstract public void appendAllAddresses(Set<String> addresses);

    abstract public void appendPhones(Set<String> phone, DateTime sendTime);

    abstract public void appendAllPhones(Set<String> phones);

    abstract public int getReferenceId();

    abstract public String getReferenceAddress();

    abstract public String getReferencePhone();

    /**
     * @throws JsonException
     */
    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        // no op. The type value is used by the factory.
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("recipientType", TYPE_CODES.getCode(getRecipientType()));
    }
}
