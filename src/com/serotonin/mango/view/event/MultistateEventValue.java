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
package com.serotonin.mango.view.event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.serotonin.util.SerializationHelper;

public class MultistateEventValue implements Serializable {
    private int key;
    private String text;

    /**
     * Required by DWR. Should not be used otherwise.
     */
    public MultistateEventValue() {
        // no op
    }

    /**
     * Required by DWR. Should not be used otherwise.
     */
    public void setKey(int key) {
        this.key = key;
    }

    /**
     * Required by DWR. Should not be used otherwise.
     */
    public void setText(String text) {
        this.text = text;
    }

    public MultistateEventValue(int key, String text) {
        this.key = key;
        this.text = text;
    }

    public int getKey() {
        return key;
    }

    public String getText() {
        return text;
    }


    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(key);
        SerializationHelper.writeSafeUTF(out, text);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            key = in.readInt();
            text = SerializationHelper.readSafeUTF(in);
        }
    }
}
