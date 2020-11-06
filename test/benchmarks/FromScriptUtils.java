package benchmarks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromScriptUtils {

    private static final Pattern DP_ = Pattern.compile("DP_[a-zA-Z0-9_.]+");
    private static final Pattern DS_ = Pattern.compile("DS_[a-zA-Z0-9_.]+");
    private static final Pattern P = Pattern.compile("p[0-9]+");
    private static final Pattern VAR = Pattern.compile("var\\s+[a-zA-Z0-9_]+");
    private static final Pattern WITHOUT_VAR = Pattern.compile("[a-zA-Z0-9_\\\\]+\\s+=");
    private static final Pattern WITHOUT_VAR_ = Pattern.compile("[a-zA-Z0-9_\\\\]+=");

    private static final Pattern RETURN_NUMBER_ = Pattern.compile("return [0-9]+;");
    private static final Pattern RETURN_NUMBER = Pattern.compile("return [0-9]+");

    private static final Pattern NUMBER_ = Pattern.compile("[0-9]+;");
    private static final Pattern NUMBER = Pattern.compile("[0-9]+");

    private static final Pattern PACAKAGE = Pattern.compile("[a-zA-Z0-9]+\\.[a-zA-Z0-9]+\\.");

    private static final Pattern HTML = Pattern.compile("<\\s*[a-zA-Z]+[^>]*>(.*?)<\\s*/\\s*[a-zA-Z]+>");

    private static final String[] HTML_TAG = {"border", "style", "width", "cellspacing", "cellpadding", "bgcolor",
            "href", "viewId", "align", "color", "size", "onChange", "onclick", "type"};


    public static List<String> parsePointXids(String script) {
        if(script == null)
            return Collections.emptyList();
        Matcher matcher = DP_.matcher(script);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String res = matcher.group();
            if(!result.contains(res))
                result.add(res);
        }
        result.removeAll(parseJsVariables(script));
        return result;
    }

    public static List<String> parseSourceXids(String script) {
        if(script == null)
            return Collections.emptyList();
        Matcher matcher = DS_.matcher(script);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String res = matcher.group();
            if(!result.contains(res))
                result.add(res);
        }
        result.removeAll(parseJsVariables(script));
        return result;
    }

    public static List<String> parseJsVariables(String script) {
        if(script == null)
            return Collections.emptyList();
        Matcher matcher = P.matcher(script);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String res = matcher.group();
            if(!result.contains(res))
                result.add(res);
        }
        matcher = VAR.matcher(script);
        while (matcher.find()) {
            String res = matcher.group().replace("var ", "");
            if(!result.contains(res))
                result.add(res);
        }
        matcher = WITHOUT_VAR.matcher(script);
        while (matcher.find()) {
            String res = matcher.group().replace("\\n", "")
                    .replace("=", "").trim();
            if(!"value".equals(res) && !result.contains(res))
                result.add(res);
        }
        matcher = WITHOUT_VAR_.matcher(script);
        while (matcher.find()) {
            String res = matcher.group().replace("\\n", "")
                    .replace("=", "").trim();
            if(!"value".equals(res) && !result.contains(res))
                result.add(res);
        }
        if(isHtml(script)) {
            result.removeAll(Arrays.asList(HTML_TAG));
        }
        return result;
    }

    public static boolean isVar(String script, String varName) {
        return script.contains("var " + varName)
                || script.contains(varName + "=")
                || Pattern.compile(varName + "\\s+=")
                        .matcher(script)
                        .find();
    }

    public static boolean isXid(String script) {
        if(script == null)
            return false;
        Matcher matcher = DP_.matcher(script);
        return matcher.find();
    }

    public static boolean isPoint(String script) {
        if(script == null)
            return false;
        Matcher matcher = P.matcher(script);
        return matcher.find();
    }

    public static boolean isObject(String script) {
        return isObjectOnDataPoint(script) ||
                isObjectOnDataSource(script);
     }

    public static boolean isObjectOnDataSource(String script) {
        if(script == null)
            return false;
        return script.contains(".enableDataSource") ||
                script.contains(".disableDataSource");
    }

    public static boolean isObjectOnDataPoint(String script) {
        if(script == null)
            return false;
        return script.contains(".writeDataPoint") ||
                script.contains(".enableDataPoint") ||
                script.contains(".disableDataPoint");
    }

    public static boolean isReturnState(String script) {
        if(script == null)
            return false;
        return script.matches(RETURN_NUMBER_.pattern()) || script.matches(RETURN_NUMBER.pattern())
                || script.equals("return '';")  || script.equals("return ''")
                || script.equals("return false;") || script.equals("return false")
                || script.equals("return true;") || script.equals("return true")
                || script.equals("return 0.0;")  || script.equals("return 0.0")
                || script.matches(NUMBER_.pattern()) || script.matches(NUMBER.pattern())
                || script.equals("false;") || script.equals("false")
                || script.equals("true;") || script.equals("true")
                || script.equals("0.0;")  || script.equals("0.0");
    }

    public static boolean isEmpty(String script) {
        return script == null || script.isEmpty();
    }


    public static boolean isHtml(String script) {
        if(script == null)
            return false;
        Matcher matcher = HTML.matcher(script);
        return matcher.find()
                || script.contains("<input")
                || script.contains("/>");
    }

    public static boolean isLongerThan1000charactersScript(String script) {
        if(script == null)
            return false;
        return script.length() > 1000;
    }

    public static boolean isShorterOrEqualsThan1000andLongerThan500charactersScript(String script) {
        if(script == null)
            return false;
        return script.length() > 500 && script.length() <= 1000;
    }

    public static boolean isShorterOrEqualsThan500andLongerThan100charactersScript(String script) {
        if(script == null)
            return false;
        return script.length() > 100 && script.length() <= 500;
    }

    public static boolean isShorterOrEqualsThan100charactersScript(String script) {
        if(script == null)
            return false;
        return script.length() <= 100;
    }

    public static boolean isJavaClass(String script) {
        if(script == null)
            return false;
        Matcher matcher = PACAKAGE.matcher(script);
        while(matcher.find()) {
            String result = matcher.group();
            if(!result.contains("this.") && !result.contains("mango.view.")
                    && !result.contains("www.") && !result.contains("zm.sivec.")
                    && !result.contains("scadaprod.sivec") && !result.contains("value.")) {
                return true;
            }
        }
        return script.contains("import ")
                || script.contains("importClass")
                || script.contains("importPackage");

                /*script.contains("import ")
                || (script.contains("new ") && !script.contains("new Array")
                && !script.contains("new RegExp")) && !script.contains("new Date")
                && !script.contains("new Image")
                || script.contains("org.") || script.contains("com.")
                || script.contains("br.") || script.contains("cc.");*/
    }

    public static boolean isContainsDpVar(String script) {
        if(script == null)
            return false;
        return script.contains("dp.")
                || script.contains("DP.")
                || script.contains("dP.")
                || script.contains("Dp.");
    }

    public static boolean isContainsDsVar(String script) {
        if(script == null)
            return false;
        return script.contains("ds.")
                || script.contains("DS.")
                || script.contains("dS.")
                || script.contains("Ds.");
    }

    public static boolean isModifiedDataPointInJs(String script) {
        if(script == null)
            return false;
        return script.contains("setPoint");
    }

    public static boolean isDwr(String script) {
        if(script == null)
            return false;
        return script.contains("mango.view.");
    }
}
