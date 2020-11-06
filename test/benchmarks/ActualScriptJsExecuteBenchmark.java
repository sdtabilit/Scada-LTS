package benchmarks;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataSource.meta.ResultTypeException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"-Xms2G","-Xmx2G"})
public class ActualScriptJsExecuteBenchmark {


    private JsExecutor jsExecutor;

    private static Map<String, ScriptData> actualScripts = new HashMap<>();

    static {
        PointValueTime pointValueTime = new PointValueTime(RandomValue.generateNumeric(), System.currentTimeMillis());
        Map<String, Object> objectContext = new HashMap<>();
        objectContext.put("dp", new DataPointWriterToBenchmark());
        objectContext.put("value", pointValueTime.getValue().getObjectValue());
        objectContext.put("point", new DataPointRTToBenchmark(1234, pointValueTime));
        objectContext.put("pointComponent", new ViewComponentToBenchmark(){});

        actualScripts.put("Init Values For All Meta Data Points - SCRIPTS", ScriptData.createWithContextAllTrue(objectContext, 1, " // service = new org.scada_lts.mango.service.PointValueService();\n// service.updateAllMetaDataPointsByScript();\n\n\n//waiting\n\n// test alarm on SK Bettange off\ndp.writeDataPoint('DP_374268',0);\n// test alarm on Ara Reckange off\ndp.writeDataPoint('DP_600004',0);\n// disable Voralarm Überflutung !!! what if true ? may try calculate\ndp.writeDataPoint('DP_868218',0);\n\ndp.writeDataPoint('DP_299794',0);\n\n\n//lock\ndp.writeDataPoint('DP_216492',0);\ndp.writeDataPoint('DP_003277',0);\ndp.writeDataPoint('DP_712253',0); //DP_712253\ndp.writeDataPoint('DP_598517',0);\ndp.writeDataPoint('DP_Bx_ID_WA_MA01_Lock',0);\ndp.writeDataPoint('DP_462924',0);\ndp.writeDataPoint('DP_870621',0);\ndp.writeDataPoint('DP_721666',0);\ndp.writeDataPoint('DP_276211',0);\ndp.writeDataPoint('DP_684396',0);\ndp.writeDataPoint('DP_678964',0);\ndp.writeDataPoint('DP_246667',0);\ndp.writeDataPoint('DP_386104',0);\n\n// RüB Belvaux\ndp.writeDataPoint('Bx_ID_Störung_Test',0);\n\n//RüB Ehlerange lock\n//DP_Eh_ID_WA_MP01_Lock\n//DP_Eh_ID_WA_MP02_Lock\n\n// Disable storung on SK 1 Dippach Gare\n// Storung test\ndp.writeDataPoint('DP_571129',0);\n"));
        actualScripts.put("Be Störung  Allg - Be ID - META_DATA_SOURCES", ScriptData.createWithContextAllTrue(4, "s= '';\nif (p8755.value || p8812.value) s = s + ' Not-Aus Rechenanlage ausgelöst \\n';\nif (p8736.value || p8812.value) s = s + ' Durchflussmessung Drosselkammer Störung Steuersicherung ausgelöst \\n';\nif (p8733.value || p8812.value) s = s + ' Niveaumessung Zulaufkammer Störung Steuersicherung ausgelöst \\n';\nif (p8743.value || p8812.value) s = s + ' Niveauüberwachung Zulaufkammer Störung Steuersicherung ausgelöst \\n';\nif (p8744.value || p8812.value) s = s + ' Positionsschalter Rückstauklappe Störung Steuersicherung ausgelöst \\n';\nif (p8745.value || p8812.value) s = s + ' Niveauüberwachung Abschlagskammer Störung Steuersicherung ausgelöst \\n';\nif (p8746.value || p8812.value) s = s + ' Positionsschalter Schaltschranktüren Störung Steuersicherung ausgelöst \\n';\nif (p8740.value || p8812.value) s = s + ' Temperaturmessung Außen Störung Steuersicherung ausgelöst \\n';\nif (p8749.value || p8812.value) s = s + ' Einspeisung Überspannungsschutz ausgelöst \\n';\nif (p8750.value || p8812.value) s = s + ' Einspeisung Netzausfall \\n';\nif (p8751.value || p8812.value) s = s + ' USV 24VDC Sammelstörung \\n';\nif (p8752.value || p8812.value) s = s + ' USV 24VDC Überspannungsschutz ausgelöst \\n';\nif (p8753.value || p8812.value) s = s + ' USV 24VDC Störung Batterie \\n';\nif (p8747.value || p8812.value) s = s + ' Hauptschalter ausgeschaltet \\n';\nif (p8748.value || p8812.value) s = s + ' Hauptschalter ausgelöst \\n';\nif (p8756.value || p8812.value) s = s + ' Not-Aus Störung Steuersicherung ausgelöst \\n';\nif (p8754.value || p8812.value) s = s + ' Not-Aus Not-Aus Gesamt ausgelöst \\n';\n\nreturn s;"));
        actualScripts.put("Re ID RLS01 Steuern - SCRIPTS", ScriptData.createWithContextAllTrue(objectContext, 1, "PLS_Start='Re_Sob_RLS01_PLS_Ein'; \nPLS_Stop='Re_Sob_RLS01_PLS_Aus';\nPLS_Hand_akt='Re_Sob_RLS01_PLS_Hand';\nSteuern='DP_2674345';\n\n//If we are going to Auto mode,\n//Reset PLS order bits\nif (p8092.value == 4) {\n// I must stop the pumps as the SPS may leave it running despite our allowance\n    dp.writeDataPoint(PLS_Start,0); // PLS Akt.\n    //java.lang.Thread.sleep(5000);\n    dp.writeDataPoint(PLS_Stop,1); // PLS Desakt.\n    //java.lang.Thread.sleep(5000);\n    dp.writeDataPoint(PLS_Hand_akt,0);\n    //java.lang.Thread.sleep(5000);\n    return;\n}\n\n//If we are going to PLS Hand mode,\n//Set PLS order bits\nif (p8092.value == 8) {\n    dp.writeDataPoint(PLS_Hand_akt,1);\n    //java.lang.Thread.sleep(5000);\n    dp.writeDataPoint(PLS_Start,0); // PLS Akt.\n    //java.lang.Thread.sleep(5000);\n    dp.writeDataPoint(PLS_Stop,1); //\n    //java.lang.Thread.sleep(5000); \n    return;\n}\n\n// If Start + Setzen is set,\nif (p8092.value == 3) {\n    dp.writeDataPoint(PLS_Stop,0); // PLS Desakt.\n    //java.lang.Thread.sleep(5000);\n    dp.writeDataPoint(PLS_Start,1); // PLS Akt.\n    //java.lang.Thread.sleep(5000);\n    return;\n}\n\n// If Stop + Setzen is set,\nif (p8092.value == 1) {\n    dp.writeDataPoint(PLS_Start,0); // PLS Akt.\n    java.lang.Thread.sleep(5000);\n    dp.writeDataPoint(PLS_Stop,1); // PLS Desakt.\n    java.lang.Thread.sleep(5000);\n    return;\n}\n\nreturn;"));
        actualScripts.put("Be FSR01 Steuern - SCRIPTS", ScriptData.createWithContextAllTrue(objectContext, 1, "\n PLS_Akt='DP_Be_RM_FSR01_Steuern.9';\nSteuern='DP_200964';\nVor='DP_Be_RM_FSR01_Steuern.10';\nRueck='DP_Be_RM_FSR01_Steuern.11';\nStop='DP_Be_RM_FSR01_Steuern.12';\nAuto_Akt='DP_Be_RM_FSR01_Steuern.8';\n\n//If we are returning to Auto mode,\n//Reset PLS order bits\nif (p8808.value == 12 || p8808.value == 4) {\n  dp.writeDataPoint(PLS_Akt,0); // PLS Akt.\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,1); // Stop\n  dp.writeDataPoint(Auto_Akt,1); // Auto Akt.\n  sleep(2);\n  dp.writeDataPoint(PLS_Akt,0); // PLS Akt.\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Auto_Akt,0); // Auto Akt.\n  return;\n}\n\n//If we are going to PLS Hand mode,\n//Set PLS order bits\nif (p8808.value == 8) {\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Auto_Akt,0); // Auto Akt.\n  dp.writeDataPoint(PLS_Akt,1); // PLS Akt.\n  sleep(2);\n  dp.writeDataPoint(PLS_Akt,0); // PLS Akt.\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Auto_Akt,0); // Auto Akt.\n  return;\n}\n\n// If we go to STOP mode,\nif (p8808.value == 2) {\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,1); // Stop\n  sleep(2);\n  dp.writeDataPoint(PLS_Akt,0); // PLS Akt.\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Auto_Akt,0); // Auto Akt.\n  return;\n}\n\n// If we go to FORWARD mode,\nif (p8808.value == 3) {\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Vor,1);\n  sleep(2);\n  dp.writeDataPoint(PLS_Akt,0); // PLS Akt.\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Auto_Akt,0); // Auto Akt.\n  return;\n}\n\n// If we go to BACKWARDS mode,\nif (p8808.value == 1) {\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Vor,0);\n  dp.writeDataPoint(Rueck,1);\n  sleep(2);\n  dp.writeDataPoint(PLS_Akt,0); // PLS Akt.\n  dp.writeDataPoint(Vor,0); // Reset both points\n  dp.writeDataPoint(Rueck,0);\n  dp.writeDataPoint(Stop,0); // Stop\n  dp.writeDataPoint(Auto_Akt,0); // Auto Akt.\n  return;\n} function sleep(s) { java.lang.Thread.sleep(s*1000)}; "));
        actualScripts.put("RÜB Bm 2 Reinigung - GRAPHICAL_VIEWS", ScriptData.createWithContextAllTrue(objectContext, 4, "// If we are not available\nif (-1==this.value) return \"\";\n\nvar s=\"<select onChange='if (this.options[this.selectedIndex].value == 99) { mango.view.setPoint(\"+ point.id +\", \\\"\" + pointComponent.id + \"\\\", prev); } else { if (this.options[this.selectedIndex].value > 3) {mango.view.setPoint(\"+ point.id +\", \\\"\" + pointComponent.id + \"\\\", this.options[this.selectedIndex].value); } else prev=this.options[this.selectedIndex].value; }'>\";\n\n// If we are in PLS Hand mode\nif (this.value!=4) {    \n    var choices=new Array(\"Stop\",\"Start\",\"Auto\",\"\",\"Setzen!\");  \n    //the values for the choices  \n    var values=new Array(1,3,4,0,99);  \n    var min = 0;  \n    var max = choices.length;  \n\n    //check if list has values  \n    if(max==0){return \"No selections defined\";}  \n    //check the array sizes match  \n    if(choices.length!=values.length){return \"Array sizes not equal\";}  \n\n    for (var i = min; i < max ;i++)  \n    {  \n       s+= \"<option value=\"+ values[i];\n\n       if (value == values[i] ) { s+= \" selected \";}  \n       s+= \"> \"+ choices[i] +\"</option>\";  \n    }  \n    s+=\"</select>\";  \n      \n    return s; }\n\n// If we are in Auto mode\n\n    var choices=new Array(\"Auto\",\"PLS Hand\");  \n    //the values for the choices  \n    var values=new Array(4,8);  \n    var min = 0;  \n    var max = choices.length;  \n      \n    //check if list has values  \n    if(max==0){return \"No selections defined\";}  \n    //check the array sizes match  \n    if(choices.length!=values.length){return \"Array sizes not equal\";}  \n\n    for (var i = min; i < max ;i++)  \n    {  \n       s+= \"<option value=\"+ values[i];  \n      \n       if (value == values[i] ) { s+= \" selected \";}  \n       s+= \"> \"+ choices[i] +\"</option>\";  \n    }  \n    s+=\"</select>\";  \n      \n    return s;"));

    }

    @Param({"-1", "9"})
    private int jsOptimizationLevel;

    @Param({"Be FSR01 Steuern - SCRIPTS",
            "Re ID RLS01 Steuern - SCRIPTS",
            "Be Störung  Allg - Be ID - META_DATA_SOURCES",
            "Init Values For All Meta Data Points - SCRIPTS",
            "RÜB Bm 2 Reinigung - GRAPHICAL_VIEWS"
    })
    private String scriptName;

    @Setup
    public void initialize() {
        jsExecutor = new JsExecutor();
    }

    @TearDown
    public void memory() {
        System.out.println("alocate memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }

    @Benchmark
    public void execute_script() throws ScriptException, ResultTypeException {
        PointValueTime result = jsExecutor.execute(actualScripts.get(scriptName), jsOptimizationLevel);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ActualScriptJsExecuteBenchmark.class.getSimpleName())
                .warmupIterations(3)
                .measurementIterations(3)
                .resultFormat(ResultFormatType.CSV)
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
