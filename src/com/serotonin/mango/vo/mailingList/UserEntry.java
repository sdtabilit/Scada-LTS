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
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.User;

@JsonRemoteEntity
public class UserEntry extends EmailRecipient {
    private int userId;
    private User user;

    @Override
    public int getRecipientType() {
        return EmailRecipient.TYPE_USER;
    }

    @Override
    public int getReferenceId() {
        return userId;
    }

    @Override
    public String getReferenceAddress() {
        return null;
    }

    @Override
    public String getReferencePhone() {
        return null;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void appendAddresses(Set<String> addresses, DateTime sendTime) {
        appendAllAddresses(addresses);
    }

    @Override
    public void appendAllAddresses(Set<String> addresses) {
        if (user == null)
            return;
        if (!user.isDisabled())
            addresses.add(user.getEmail());
    }

    @Override
    public void appendPhones(Set<String> phones, DateTime sendTime) {
        appendAllPhones(phones);
    }

    @Override
    public void appendAllPhones(Set<String> phones) {
        if (user == null)
            return;
        if (!user.isDisabled())
            phones.add(user.getPhone());
    }

    @Override
    public String toString() {
        if (user == null)
            return "userId=" + userId;
        return user.getUsername();
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        if (user == null)
            user = new UserDao().getUser(userId);
        map.put("username", user.getUsername());
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);

        String username = json.getString("username");
        if (username == null)
            throw new LocalizableJsonException("emport.error.recipient.missing.reference", "username");

        user = new UserDao().getUser(username);
        if (user == null)
            throw new LocalizableJsonException("emport.error.recipient.invalid.reference", "username", username);

        userId = user.getId();
    }
}
