package benchmarks;

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MangoValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScriptData {

    private final String script;
    private final Map<String, IDataPoint> pointContext;
    private final Map<String, Object> objectContext;
    private final List<IntValuePair> pointContextPair;
    private final List<IntValuePair> objectContextPair;
    private final int dataTypeId;

    public ScriptData(String script,
                Map<String, IDataPoint> pointContext,
                Map<String, Object> objectContext,
                int dataTypeId) {
        this.script = script;
        this.pointContext = pointContext;
        this.objectContext = objectContext;
        this.pointContextPair = pointContext.entrySet().stream()
                .map(a -> new IntValuePair(0, a.getKey()))
                .collect(Collectors.toList());
        this.objectContextPair = objectContext.entrySet().stream()
                .map(a -> new IntValuePair(0, a.getKey()))
                .collect(Collectors.toList());
        this.dataTypeId = dataTypeId;
    }

    public static ScriptData createWithContextAllTrue(Map<String, Object> objectContext, int dataTypeId, String script) {
        return create(objectContext, dataTypeId, script, new BinaryValue(true));
    }

    public static ScriptData createWithContextAllTrue(int dataTypeId, String script) {
        return create(Collections.emptyMap(), dataTypeId, script, new BinaryValue(true));
    }

    public static ScriptData createWithContextAllFalse(Map<String, Object> objectContext, int dataTypeId, String script) {
        return create(objectContext, dataTypeId, script, new BinaryValue(false));
    }

    public static ScriptData createWithContextAllFalse(int dataTypeId, String script) {
        return create(Collections.emptyMap(), dataTypeId, script, new BinaryValue(false));
    }

    private static ScriptData create(Map<String, Object> objectContext, int dataTypeId, String script, MangoValue mangoValue) {
        Map<String, IDataPoint> pointContext = new HashMap<>();
        List<String> vars = FromScriptUtils.parseJsVariables(script);
        for (String var: vars) {
            pointContext.put(var, new DataPointRTToBenchmark(0, new PointValueTime(mangoValue, System.currentTimeMillis())));
        }
        if(script.contains("this.options") || script.contains("this.selectedIndex")) {
            script = "var temp = new Object(); temp.value=99; var options = [temp]; var selectedIndex = 0;" + script;
        }
        return new ScriptData(script, pointContext, objectContext, dataTypeId);
    }

    public String getScript() {
        return script;
    }

    public Map<String, IDataPoint> getPointContext() {
        return pointContext;
    }

    public Map<String, Object> getObjectContext() {
        return objectContext;
    }

    public List<IntValuePair> getPointContextPair() {
        return pointContextPair;
    }

    public List<IntValuePair>  getObjectContextPair() {
        return objectContextPair;
    }

    public int getDataTypeId() {
        return dataTypeId;
    }
}
