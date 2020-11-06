package benchmarks;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.*;
import com.serotonin.mango.rt.dataSource.meta.*;
import com.serotonin.web.i18n.LocalizableMessage;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsExecutor {

    private static final String SCRIPT_PREFIX = "function __scriptExecutor__() {";
    private static final String SCRIPT_SUFFIX = "\r\n}\r\n__scriptExecutor__();";

    private final Map<String, IDataPoint> pointContext;
    private final Map<String, Object> objectContext;
    private final int dataTypeId;

    public JsExecutor() {
        this.pointContext = Collections.emptyMap();
        this.objectContext = Collections.emptyMap();
        this.dataTypeId = 0;
    }

    public JsExecutor(Map<String, IDataPoint> pointContext,
                      Map<String, Object> objectContext,
                      int dataTypeId) {
        this.pointContext = pointContext;
        this.objectContext = objectContext;
        this.dataTypeId = dataTypeId;
    }

    public PointValueTime execute(ScriptData script, int jsOptimizationLevel) throws ScriptException, ResultTypeException {
        return execute(script.getScript(), script.getPointContext(),
                script.getObjectContext(), jsOptimizationLevel,
                script.getDataTypeId());
    }

    public PointValueTime executeWithActualPointValue(ScriptData script, int jsOptimizationLevel) throws ScriptException, ResultTypeException {
        return execute(script.getScript(), script.getPointContextPair(),
                script.getObjectContextPair(), jsOptimizationLevel,
                script.getDataTypeId());
    }

    public PointValueTime execute(String script, int jsOptimizationLevel) throws ScriptException, ResultTypeException {
        return execute(script, pointContext, objectContext, jsOptimizationLevel, dataTypeId);
    }

    public PointValueTime execute(String script, int jsOptimizationLevel, int dataTypeId) throws ScriptException, ResultTypeException {
        return execute(script, pointContext, objectContext, jsOptimizationLevel, dataTypeId);
    }

    private static PointValueTime execute(String script,
                                  Map<String, IDataPoint> pointContext,
                                  Map<String, Object> objectContext,
                                  int jsOptimizationLevel,
                                  int dataTypeId) throws ScriptException, ResultTypeException {

        long timestamp = 0;
        Context cx = Context.enter();
        cx.setOptimizationLevel(jsOptimizationLevel);
        // Execute.
        try {
            Scriptable scope = cx.initStandardObjects();

            // Create the wrapper object context.
            WrapperContext wrapperContext = new WrapperContext(System.currentTimeMillis());

            // Add constants to the context.
            scope.put("SECOND", scope, Common.TimePeriods.SECONDS);
            scope.put("MINUTE", scope, Common.TimePeriods.MINUTES);
            scope.put("HOUR", scope, Common.TimePeriods.HOURS);
            scope.put("DAY", scope, Common.TimePeriods.DAYS);
            scope.put("WEEK", scope, Common.TimePeriods.WEEKS);
            scope.put("MONTH", scope, Common.TimePeriods.MONTHS);
            scope.put("YEAR", scope, Common.TimePeriods.YEARS);
            scope.put("CONTEXT", scope, wrapperContext);

            // Put the context variables into the engine with engine scope.
            for (String varName : pointContext.keySet()) {
                IDataPoint point = pointContext.get(varName);
                AbstractPointWrapper distinctPointWrapper = convert(point, wrapperContext);
                scope.put(varName, scope, distinctPointWrapper);
            }
            for (String varName : objectContext.keySet()) {
                Object object = objectContext.get(varName);
                scope.put(varName, scope, object);
            }
            String scriptToExecute = SCRIPT_PREFIX + script + SCRIPT_SUFFIX;
            Object result;
            try {

                result = cx.evaluateString(scope, scriptToExecute, "<cmd>", 1,
                        null);

            } catch (Exception e) {
                throw new ScriptException(e.getMessage());
            }

            Object ts = scope.get("TIMESTAMP", scope);
            if (ts != null) {
                if (ts instanceof Number)
                    timestamp = ((Number) ts).longValue();
            }
            MangoValue value = convert(dataTypeId, String.valueOf(result));
            return new PointValueTime(value, timestamp);
        } finally {
            Context.exit();
        }
    }

    private static PointValueTime execute(String script,
                                          List<IntValuePair> pointContext,
                                          List<IntValuePair> objectContext,
                                          int jsOptimizationLevel,
                                          int dataTypeId) throws ScriptException, ResultTypeException {

        long timestamp = 0;
        Context cx = Context.enter();
        cx.setOptimizationLevel(jsOptimizationLevel);
        // Execute.
        try {
            Scriptable scope = cx.initStandardObjects();

            // Create the wrapper object context.
            WrapperContext wrapperContext = new WrapperContext(System.currentTimeMillis());

            // Add constants to the context.
            scope.put("SECOND", scope, Common.TimePeriods.SECONDS);
            scope.put("MINUTE", scope, Common.TimePeriods.MINUTES);
            scope.put("HOUR", scope, Common.TimePeriods.HOURS);
            scope.put("DAY", scope, Common.TimePeriods.DAYS);
            scope.put("WEEK", scope, Common.TimePeriods.WEEKS);
            scope.put("MONTH", scope, Common.TimePeriods.MONTHS);
            scope.put("YEAR", scope, Common.TimePeriods.YEARS);
            scope.put("CONTEXT", scope, wrapperContext);

            // Put the context variables into the engine with engine scope.
            for (IntValuePair varName : pointContext) {
                IDataPoint point = getDataPoint(varName.getKey());
                AbstractPointWrapper distinctPointWrapper = convert(point, wrapperContext);
                scope.put(varName.getValue(), scope, distinctPointWrapper);
            }
            for (IntValuePair varName : objectContext) {
                Object object = getObject(varName.getKey());
                scope.put(varName.getValue(), scope, object);
            }
            String scriptToExecute = SCRIPT_PREFIX + script + SCRIPT_SUFFIX;
            Object result;
            try {

                result = cx.evaluateString(scope, scriptToExecute, "<cmd>", 1,
                        null);

            } catch (Exception e) {
                throw new ScriptException(e.getMessage());
            }

            Object ts = scope.get("TIMESTAMP", scope);
            if (ts != null) {
                if (ts instanceof Number)
                    timestamp = ((Number) ts).longValue();
            }
            MangoValue value = convert(dataTypeId, String.valueOf(result));
            return new PointValueTime(value, timestamp);
        } finally {
            Context.exit();
        }
    }

    private static IDataPoint getDataPoint(int id) {
        return new DataPointRTToBenchmark(id, new PointValueTime(new BinaryValue(true), System.currentTimeMillis()));
    }

    private static Object getObject(int id) {
        return new DataPointWriterToBenchmark();
    }

    private static AbstractPointWrapper convert(IDataPoint point, WrapperContext wrapperContext) {
        switch (point.getDataTypeId()) {
            case DataTypes.BINARY: return new BinaryPointWrapper(point, wrapperContext);
            case DataTypes.MULTISTATE: return new MultistatePointWrapper(point, wrapperContext);
            case DataTypes.NUMERIC: return new NumericPointWrapper(point, wrapperContext);
            case DataTypes.ALPHANUMERIC: return new AlphanumericPointWrapper(point, wrapperContext);
            default: throw new ShouldNeverHappenException(
                    "Unknown data type id: " + point.getDataTypeId());
        }
    }

    private static MangoValue convert(int dataTypeId, String value) throws ResultTypeException {
        if(value == null) {
            return getDefault(dataTypeId);
        }
        switch (dataTypeId) {
            case DataTypes.BINARY: return new BinaryValue(Boolean.valueOf(value));
            case DataTypes.MULTISTATE: return new MultistateValue(Integer.valueOf(value));
            case DataTypes.NUMERIC: return new NumericValue(Double.valueOf(value));
            case DataTypes.ALPHANUMERIC: return new AlphanumericValue(value);
            default: throw new ResultTypeException(new LocalizableMessage(
                    "event.script.convertError", value,
                    DataTypes.getDataTypeMessage(dataTypeId)));
        }
    }

    private static MangoValue getDefault(int dataTypeId) {
        switch (dataTypeId) {
            case DataTypes.BINARY: return new BinaryValue(false);
            case DataTypes.MULTISTATE: return new MultistateValue(0);
            case DataTypes.NUMERIC: return new NumericValue(0);
            case DataTypes.ALPHANUMERIC: return new AlphanumericValue("");
            default: throw new ShouldNeverHappenException(
                    "Unknown data type id: " + dataTypeId);
        }
    }
}
