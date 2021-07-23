package org.scada_lts.utils;

import org.scada_lts.web.mvc.api.AggregateSettings;

public class SystemSettingsUtils {

    public static String validateAggregateSettings(AggregateSettings body) {
        String msg = ValidationUtils.msgIfNullOrInvalid("valuesLimit must be >= 0; ", body.getValuesLimit(), a -> a <= 0);
        msg += ValidationUtils.msgIfNullOrInvalid("limitFactor must be > 0; ", body.getLimitFactor(), a -> a <= 0.0);
        msg += ValidationUtils.msgIfNull("Correct property enabled; ", body.isEnabled());
        return msg;
    }
}
