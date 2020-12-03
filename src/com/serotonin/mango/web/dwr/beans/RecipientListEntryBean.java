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
package com.serotonin.mango.web.dwr.beans;

import java.io.Serializable;
import java.util.Map;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonSerializable;
import com.serotonin.mango.db.dao.MailingListDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.mailingList.AddressEntry;
import com.serotonin.mango.vo.mailingList.EmailRecipient;
import com.serotonin.mango.vo.mailingList.MailingList;
import com.serotonin.mango.vo.mailingList.UserEntry;
import com.serotonin.mango.vo.mailingList.PhoneEntry;

@JsonRemoteEntity
public class RecipientListEntryBean implements Serializable, JsonSerializable {
    private static final long serialVersionUID = -1;

    private int recipientType;
    private int referenceId;
    private String referenceAddress;
    private String referencePhone;

    public EmailRecipient createEmailRecipient() {
        switch (recipientType) {
        case EmailRecipient.TYPE_MAILING_LIST:
            MailingList ml = new MailingList();
            ml.setId(referenceId);
            return ml;
        case EmailRecipient.TYPE_USER:
            UserEntry u = new UserEntry();
            u.setUserId(referenceId);
            return u;
        case EmailRecipient.TYPE_ADDRESS:
            AddressEntry a = new AddressEntry();
            a.setAddress(referenceAddress);
            return a;
        case EmailRecipient.TYPE_PHONE:
            PhoneEntry p = new PhoneEntry();
            p.setPhone(referencePhone);
            return p;
        }
        throw new ShouldNeverHappenException("Unknown email recipient type: " + recipientType);
    }

    public String getReferenceAddress() {
        return referenceAddress;
    }

    public void setReferenceAddress(String address) {
        referenceAddress = address;
    }

    public String getReferencePhone() {
        return referencePhone;
    }

    public void setReferencePhone(String referencePhone) {
        this.referencePhone = referencePhone;
    }

    public int getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(int typeId) {
        recipientType = typeId;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int refId) {
        referenceId = refId;
    }

    public void jsonSerialize(Map<String, Object> map) {
        map.put("recipientType", EmailRecipient.TYPE_CODES.getCode(recipientType));
        if (recipientType == EmailRecipient.TYPE_MAILING_LIST)
            map.put("mailingList", new MailingListDao().getMailingList(referenceId).getXid());
        else if (recipientType == EmailRecipient.TYPE_USER)
            map.put("username", new UserDao().getUser(referenceId).getUsername());
        else if (recipientType == EmailRecipient.TYPE_ADDRESS)
            map.put("address", referenceAddress);
        else if (recipientType == EmailRecipient.TYPE_PHONE)
            map.put("phone", referencePhone);
    }

    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        String text = json.getString("recipientType");
        if (text == null)
            throw new LocalizableJsonException("emport.error.recipient.missing", "recipientType",
                    EmailRecipient.TYPE_CODES.getCodeList());

        recipientType = EmailRecipient.TYPE_CODES.getId(text);
        if (recipientType == -1)
            throw new LocalizableJsonException("emport.error.recipient.invalid", "recipientType", text,
                    EmailRecipient.TYPE_CODES.getCodeList());

        if (recipientType == EmailRecipient.TYPE_MAILING_LIST) {
            text = json.getString("mailingList");
            if (text == null)
                throw new LocalizableJsonException("emport.error.recipient.missing.reference", "mailingList");

            MailingList ml = new MailingListDao().getMailingList(text);
            if (ml == null)
                throw new LocalizableJsonException("emport.error.recipient.invalid.reference", "mailingList", text);

            referenceId = ml.getId();
        }
        else if (recipientType == EmailRecipient.TYPE_USER) {
            text = json.getString("username");
            if (text == null)
                throw new LocalizableJsonException("emport.error.recipient.missing.reference", "username");

            User user = new UserDao().getUser(text);
            if (user == null)
                throw new LocalizableJsonException("emport.error.recipient.invalid.reference", "user", text);

            referenceId = user.getId();
        }
        else if (recipientType == EmailRecipient.TYPE_ADDRESS) {
            referenceAddress = json.getString("address");
            if (referenceAddress == null)
                throw new LocalizableJsonException("emport.error.recipient.missing.reference", "address");
        }
        else if (recipientType == EmailRecipient.TYPE_PHONE) {
            referencePhone = json.getString("phone");
            if (referencePhone == null)
                throw new LocalizableJsonException("emport.error.recipient.missing.reference", "phone");
        }
    }
}
