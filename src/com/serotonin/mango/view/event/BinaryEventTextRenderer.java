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

import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.view.ImplDefinition;
import com.serotonin.util.SerializationHelper;

/**
 * This class is called "binary" so that we can refer to values as 0 and 1, which is the actual representation in most
 * BA systems. However, the render method actually expects a boolean value which (arbitrarily) maps 0 to false and 1 to
 * true.
 * 
 * @author Artur Wolak
 */
@JsonRemoteEntity
public class BinaryEventTextRenderer extends BaseEventTextRenderer {
    private static ImplDefinition definition = new ImplDefinition(BinaryEventTextRenderer.TYPE_NAME, "EVENT_BINARY",
            "textRenderer.binary", new int[] { DataTypes.BINARY });

    public static ImplDefinition getDefinition() {
        return definition;
    }

    public String getTypeName() {
        return definition.getName();
    }

    public ImplDefinition getDef() {
        return definition;
    }

    public static final String TYPE_NAME = "eventTextRendererBinary";

    @JsonRemoteProperty
    private String zeroLabel;
    @JsonRemoteProperty
    private String oneLabel;

    public BinaryEventTextRenderer() {
        // no op
    }

    public BinaryEventTextRenderer(String zeroValue, String oneValue) {
        zeroLabel = zeroValue;
        oneLabel = oneValue;
    }

    @Override
    protected String getTextImpl(MangoValue value) {
        if (!(value instanceof BinaryValue))
            return null;
        return getText(value.getBooleanValue());
    }

    public String getZeroLabel() {
        return zeroLabel;
    }

    public void setZeroLabel(String zeroLabel) {
        this.zeroLabel = zeroLabel;
    }

    public String getOneLabel() {
        return oneLabel;
    }

    public void setOneLabel(String oneLabel) {
        this.oneLabel = oneLabel;
    }

    @Override
    public String getText(boolean value) {
        if (value)
            return oneLabel;
        return zeroLabel;
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
        SerializationHelper.writeSafeUTF(out, zeroLabel);
        SerializationHelper.writeSafeUTF(out, oneLabel);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            zeroLabel = SerializationHelper.readSafeUTF(in);
            oneLabel = SerializationHelper.readSafeUTF(in);
        }
    }
}
