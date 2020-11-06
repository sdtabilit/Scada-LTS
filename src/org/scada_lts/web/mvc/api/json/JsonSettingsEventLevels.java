package org.scada_lts.web.mvc.api.json;

import com.serotonin.mango.web.dwr.beans.IntegerPair;

public class JsonSettingsEventLevels extends IntegerPair {

    private String translation;

    public JsonSettingsEventLevels() {}

    public JsonSettingsEventLevels(int i1, int i2, String translation) {
        super(i1, i2);
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}
