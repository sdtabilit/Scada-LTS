package org.scada_lts.utils;

import org.apache.commons.logging.LogFactory;
import org.scada_lts.config.ScadaConfig;
import org.scada_lts.web.mvc.api.AggregateSettings;

public final class AggregateUtils {

    private static final String API_AMCHARTS_AGGREGATION_ENABLED_KEY = "api.amcharts.aggregation.enabled";
    private static final String API_AMCHARTS_AGGREGATION_VALUES_LIMIT_KEY = "api.amcharts.aggregation.valuesLimit";
    private static final String API_AMCHARTS_AGGREGATION_LIMIT_FACTOR_KEY = "api.amcharts.aggregation.limitFactor";
    private static final int NUMBER_OF_VALUES_LIMIT_DEFAULT = 10000;
    private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(AggregateUtils.class);


    private AggregateUtils() {}

    public static long calculateIntervalMs(long startTs, long endTs, int numberOfPoints, AggregateSettings aggregateSettings) {
        if(numberOfPoints == 0)
            return calculate(startTs, endTs, 1, aggregateSettings);
        return calculate(startTs, endTs, numberOfPoints, aggregateSettings);
    }

    public static boolean isEnabled() {
        try {
            return ScadaConfig.getInstance().getBoolean(API_AMCHARTS_AGGREGATION_ENABLED_KEY, false);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
    }

    public static int getValuesLimit() {
        try {
            return ScadaConfig.getInstance().getInt(API_AMCHARTS_AGGREGATION_VALUES_LIMIT_KEY, NUMBER_OF_VALUES_LIMIT_DEFAULT);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return NUMBER_OF_VALUES_LIMIT_DEFAULT;
        }
    }

    public static double getLimitFactor() {
        try {
            return Double.parseDouble(ScadaConfig.getInstance().getProperty(API_AMCHARTS_AGGREGATION_LIMIT_FACTOR_KEY));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return 1;
        }
    }

    private static long calculate(long startTs, long endTs, int numberOfPoints, AggregateSettings aggregateSettings) {
        long result = Math.round((endTs - startTs)/(aggregateSettings.getValuesLimit() * aggregateSettings.getLimitFactor()/numberOfPoints));
        return result == 0 ? 1 : result;
    }
}
