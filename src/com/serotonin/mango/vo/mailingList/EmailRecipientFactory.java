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

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonValue;
import com.serotonin.json.TypeFactory;
import com.serotonin.mango.util.LocalizableJsonException;

public class EmailRecipientFactory implements TypeFactory {
    @Override
    public Class<?> getType(JsonValue jsonValue) throws JsonException {
        if (jsonValue.isNull())
            return null;

        JsonObject json = jsonValue.toJsonObject();

        String text = json.getString("recipientType");
        if (text == null)
            throw new LocalizableJsonException("emport.error.recipient.missing", "recipientType",
                    EmailRecipient.TYPE_CODES);

        int type = EmailRecipient.TYPE_CODES.getId(text);
        if (!EmailRecipient.TYPE_CODES.isValidId(type))
            throw new LocalizableJsonException("emport.error.recipient.invalid", "recipientType", text,
                    EmailRecipient.TYPE_CODES.getCodeList());

        if (type == EmailRecipient.TYPE_MAILING_LIST)
            return MailingList.class;
        if (type == EmailRecipient.TYPE_USER)
            return UserEntry.class;
        if (type == EmailRecipient.TYPE_ADDRESS)
            return AddressEntry.class;
        return PhoneEntry.class;
    }
}
