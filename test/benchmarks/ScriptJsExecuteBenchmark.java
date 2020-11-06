package benchmarks;

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.rt.dataSource.meta.ResultTypeException;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import utils.IntValuePairPrinted;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"-Xms2G","-Xmx2G"})
public class ScriptJsExecuteBenchmark {

    @Param({"-1", "0", "1", "9"})
    private int jsOptimizationLevel;

    private JsExecutor jsExecutor;

    @Setup
    public void initialize() {
        IDataPoint p1 = new DataPointRTToBenchmark(1, new PointValueTime(new NumericValue(11.11111), 0));
        IDataPoint p2 = new DataPointRTToBenchmark(2,new PointValueTime(new NumericValue(23454.34513414), 0));
        IDataPoint p3 = new DataPointRTToBenchmark(3,new PointValueTime(new NumericValue(3352343.4324), 0));
        IDataPoint p4 = new DataPointRTToBenchmark(4,new PointValueTime(new NumericValue(432423423.23234), 0));
        IDataPoint p5 = new DataPointRTToBenchmark(5,new PointValueTime(new NumericValue(5.23423432), 0));

        Map<String, IDataPoint> pointContext = new HashMap<>();
        pointContext.put("p1", p1);
        pointContext.put("p2", p2);
        pointContext.put("p3", p3);
        pointContext.put("p4", p4);
        pointContext.put("p5", p5);

        Map<String, Object> objectContext = new HashMap<>();
        objectContext.put("pv", new PointValueTime(new NumericValue(11.131), System.currentTimeMillis()));
        objectContext.put("pv2", new PointValueTime(new NumericValue(1234), System.currentTimeMillis()));
        objectContext.put("pair", new IntValuePairPrinted(new IntValuePair(222,"abc")));
        objectContext.put("p", new IntValuePairPrinted(new IntValuePair(222,"a")));
        objectContext.put("abcdefghijklmnopqrstuwxyz", new IntValuePairPrinted(new IntValuePair(1351525233,"abcdefghijklmnopqrstuwxyz")));
        objectContext.put("dp", new DataPointWriterToBenchmark());

       jsExecutor = new JsExecutor(pointContext, objectContext,3);
    }

    @Benchmark
    public void execute_script_with_object_context() throws ScriptException, ResultTypeException {
        jsExecutor.execute("return (pv.getTime() + pv.getValue().getObjectValue()/pv2.getTime()) * pv2.getValue() * pair.getIntValuePair().getKey();", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_with_object_context_var_name_long() throws ScriptException, ResultTypeException {
        jsExecutor.execute("return abcdefghijklmnopqrstuwxyz.getIntValuePair().getKey();", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_with_object_context_var_name_short() throws ScriptException, ResultTypeException {
        jsExecutor.execute("return p.getIntValuePair().getKey();", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_with_point_context() throws ScriptException, ResultTypeException {
        jsExecutor.execute("return (p1.value/p2.value * p3.value + p4.value + p5.value*p5.value*p5.value);", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_with_js_fun() throws ScriptException, ResultTypeException {
        jsExecutor.execute("return p1.value.toFixed(2) * Math.PI * Math.pow(12, 12) * Math.sqrt(12351454);", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_return_state() throws ScriptException, ResultTypeException {
        jsExecutor.execute("return 1;", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_with_java() throws ScriptException, ResultTypeException {
        jsExecutor.execute("var pointValueTime = com.serotonin.mango.rt.dataImage.PointValueTime(new com.serotonin.mango.rt.dataImage.types.NumericValue(12345.5),0);" +
                "return pointValueTime.getDoubleValue()", jsOptimizationLevel);
    }

    @Benchmark
    public void execute_script_big() throws ScriptException, ResultTypeException {
        jsExecutor.execute("var today = new Date();\n// var mi = today.getMinutes();\nvar " +
                "hh = today.getHours();\nvar dow = today.getDay();\nvar dd = today.getDate();\nvar " +
                "mm = today.getMonth()+1; //January is 0!\nvar yyyy = today.getFullYear();\n\n// 2016 hollidays\n\n" +
                "if (yyyy == 2016) {\n    " +
                "if ( (dd == 5) && (mm == 5) ) return false;\n}\n\n// 2017 hollidays\n\n" +
                "if (yyyy == 2017) {\n    if ( (dd == 25) && (mm == 5) ) return false;\n}\n\n// 2018 hollidays\n\n" +
                "if (yyyy == 2018) {\n    if ( (dd == 10) && (mm == 5) ) return false;\n}\n\n// 2019 hollidays\n\n" +
                "if (yyyy == 2019) {\n    if ( (dd == 30) && (mm == 5) ) return false;\n}\n\n// 2020 hollidays\n\n" +
                "if (yyyy == 2020) {\n    if ( (dd == 21) && (mm == 5) ) return false;\n}\n\n// Every year hollidays\n\n" +
                "if ( (dd == 23) && (mm == 6) ) return false;\n// Closed the 23/6\n\n" +
                "if ( (dd == 25) && (mm == 12) ) return false;\n// Closed the 25/12\n\n" +
                "if ( (dd == 26) && (mm == 12) ) return false;\n// Closed the 26/12\n\n" +
                "if ( (dd == 1) && (mm == 11) ) return false;\n// Closed the 1/11\n\n" +
                "if ( (dd == 1) && (mm == 1) ) return false;\n// Closed the 1/1\n\n" +
                "if ( (dd == 15) && (mm == 8) ) return false;\n// Closed the 15/8\n\n" +
                "if ( (dd == 1) && (mm == 5) ) return false;\n// Closed the 1/5\n\n" +
                "if (dow < 2) return false;\n// If sunday or monday, closed.\n\n" +
                "if (dow == 6) {\n    if ((hh < 7 ) || (hh >= 16)) return false;\n    else return true;\n}\n// If saturday before 7 or after 16, closed.\n\n" +
                "if ((hh < 8 ) || (hh >= 17)) return false;\n// Otherwise (from tuesday to friday), before 8 and after 17, closed.\n\nreturn true;\n// Otherwise, open.",
                jsOptimizationLevel, 1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ScriptJsExecuteBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .resultFormat(ResultFormatType.CSV)
                .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
}
