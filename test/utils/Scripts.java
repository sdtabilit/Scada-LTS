package utils;

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.rt.dataImage.types.*;
import java.text.MessageFormat;


public class Scripts {

    public static String createScriptWithObjectContextWriteDataPoint(String pointToChangeXid, MangoValue value)  {
        String script = _alphanumeric("dp.writeDataPoint(''{0}'', {1});", value, 1);
        return MessageFormat.format(script, pointToChangeXid, String.valueOf(value.getObjectValue()));
    }

    public static String createScriptWithObjectContextWriteDataPoint(String pointToChangeXid, IntValuePair pointToRead)  {
        return MessageFormat.format("dp.writeDataPoint(''{0}'', {1}.value);", pointToChangeXid, String.valueOf(pointToRead.getValue()));
    }

    public static String createScriptWithObjectContextEnableDataPoint(String pointToChangeXid)  {
        return MessageFormat.format("dp.enableDataPoint(''{0}'');", pointToChangeXid);
    }

    public static String createScriptWithObjectContextDisableDataPoint(String pointToChangeXid)  {
        return MessageFormat.format("dp.disableDataPoint(''{0}'');", pointToChangeXid);
    }

    public static String createScriptWithObjectContextEnableDataSource(String sourceoChangeXid)   {
        return MessageFormat.format("ds.enableDataSource(''{0}'');", sourceoChangeXid);
    }

    public static String createScriptWithObjectContextDisableDataSource(String sourceoChangeXid)  {
        return MessageFormat.format("ds.disableDataSource(''{0}'');", sourceoChangeXid);
    }

    public static String createScriptReturnState(MangoValue value)  {
        String script = _alphanumeric("return {0};", value, 0);
        return MessageFormat.format(script, String.valueOf(value.getObjectValue()));
    }

    public static String createScriptWithJavaPointValueTime(MangoValue value) {
        return MessageFormat.format("var pointValueTime = " +
                "new com.serotonin.mango.rt.dataImage.PointValueTime({0}, {1});" +
                "return pointValueTime.getStringValue();", Value.getNewInstance(value),
                String.valueOf(System.currentTimeMillis()));
    }

    public static String createScriptWithJavaViewDwr() {
        return "var mydwr=new com.serotonin.mango.web.dwr.ViewDwr();" +
                "var user=mydwr.getLoggedUser();" +
                "return user + '';";
    }

    public static String createScriptReturnState(IntValuePair var)  {
        return MessageFormat.format("return {0}.value;", var.getValue());
    }

    public static String createScriptIf(IntValuePair var, MangoValue conditionValue, MangoValue returnIfTrue,
                                        MangoValue returnIfFalse) {
        String script = _alphanumeric("if ({0}.value == {1})", conditionValue, 1) +
                _alphanumeric("return {2};", returnIfTrue, 2) +
                _alphanumeric("return {3};", returnIfFalse, 3);
        return MessageFormat.format(script, var.getValue(), String.valueOf(conditionValue.getObjectValue()),
                String.valueOf(returnIfTrue.getObjectValue()),
                String.valueOf(returnIfFalse.getObjectValue()));
    }

    private static String _alphanumeric(String script, MangoValue value, int index) {
        if(value instanceof AlphanumericValue)
            return script.replace("{" + index + "}", "''{" + index + "}''");
        return script;
    }

    enum Value {
        NUMERIC_VALUE("new com.serotonin.mango.rt.dataImage.types.NumericValue({0})", NumericValue.class),
        ALPHANUMERIC_VALUE("new com.serotonin.mango.rt.dataImage.types.AlphanumericValue(''{0}'')", AlphanumericValue.class),
        BINARY_VALUE("new com.serotonin.mango.rt.dataImage.types.BinaryValue({0})", BinaryValue.class),
        MULTISTATE_VALUE("new com.serotonin.mango.rt.dataImage.types.MultistateValue({0})", MultistateValue.class),
        NONE("new java.lang.Object()", Object.class);

        private final String newInstance;
        private final Class<?> key;

        Value(String newInstance, Class<?> key) {
            this.newInstance = newInstance;
            this.key = key;
        }

        public static String getNewInstance(MangoValue mangoValue) {
            for (Value value: Value.values()) {
                if(value.key.equals(mangoValue.getClass())) {
                    return MessageFormat.format(value.newInstance, String.valueOf(mangoValue.getObjectValue()));
                }
            }
            return Value.NONE.newInstance;
        }

    }
}
