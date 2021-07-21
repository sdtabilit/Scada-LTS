package com.serotonin.mango.util;

import br.org.scadabr.vo.scripting.ScriptVO;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.view.component.ScriptComponent;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.link.PointLinkVO;
import org.scada_lts.dao.model.ScadaObjectIdentifier;

import java.text.MessageFormat;
import java.util.Map;

public final class LoggingScriptUtils {

    public static final String VIEW_IDENTIFIER = "viewIdentifier";

    private LoggingScriptUtils() {}

    public static String infoErrorExecutionScript(Exception e,
                                                  Map<String, Object> model,
                                                  ScriptComponent scriptComponent) {
        if(model != null && !model.isEmpty()) {
            Object identifier = model.get(LoggingScriptUtils.VIEW_IDENTIFIER);
            if (identifier instanceof ScadaObjectIdentifier)
                return infoErrorExecutionScript(e, (ScadaObjectIdentifier) identifier, scriptComponent);
            else
                return infoErrorExecutionScript(e, scriptComponent);
        }
        return "[no model in context]";
    }

    public static String infoErrorExecutionScript(Map<String, Object> model,
                                                  ScriptComponent scriptComponent) {
        return infoErrorExecutionScript(null, model, scriptComponent);
    }

    public static String infoErrorExecutionScript(Exception e,
                                                  DataPointRT dataPoint,
                                                  DataSourceRT dataSource) {
        String context = generateContext(dataPoint, dataSource);
        return infoErrorExecutionScript(e, context);
    }

    public static String infoErrorInitializationScript(Exception e,
                                                  DataPointRT dataPoint,
                                                  DataSourceRT dataSource) {
        return infoErrorExecutionScript(e, dataPoint, dataSource);
    }

    public static String infoErrorExecutionScript(Exception e,
                                                  ScadaObjectIdentifier viewIdentifier,
                                                  ScriptComponent scriptComponent) {
        String context = generateContext(viewIdentifier, scriptComponent);
        return infoErrorExecutionScript(e, context);
    }

    public static String infoErrorExecutionScript(Exception e,
                                                  ScriptComponent scriptComponent) {
        String context = generateContext(null, scriptComponent);
        return infoErrorExecutionScript(e, context);
    }

    public static String infoErrorExecutionScript(Exception e,
                                                  PointLinkVO vo,
                                                  DataPointRT target,
                                                  DataPointRT source) {
        String context = generateContext(vo, target, source);

        return infoErrorExecutionScript(e, context);
    }

    public static String infoErrorExecutionScript(Exception e, ScriptVO<?> vo) {
        String context = generateContext(vo);
        return infoErrorExecutionScript(e, context);
    }

    public static String infoErrorExecutionScript(Exception e, String context) {
        String info = "Problem with executing script: context: {0} {1}";
        if(e != null) {
            return MessageFormat.format(info, context, ", " + exceptionInfo(e));
        }
        return MessageFormat.format(info, context, "");
    }

    public static String generateContext(DataPointRT dataPoint, DataSourceRT dataSource) {
        String context;
        if(dataSource != null) {
            context = dataSourceInfo(dataSource);
        } else {
            context = "[no dataSource in context]";
        }

        if(dataPoint != null && dataPoint.getVO() != null) {
            DataPointVO dataPointVO = dataPoint.getVO();
            context += ", " + dataPointInfo(dataPointVO);
        }
        return context;
    }

    private static String generateContext(ScadaObjectIdentifier view, ScriptComponent scriptComponent) {
        String context;
        if(view != null) {
            context = identifierInfo(view, "view");
        } else {
            context = "[no view in context]";
        }

        if(scriptComponent != null) {
            context += ", " + scriptComponentInfo(scriptComponent);
        }

        if(scriptComponent != null && scriptComponent.tgetDataPoint() != null) {
            context += ", " + dataPointInfo(scriptComponent.tgetDataPoint());
        }

        return context;
    }

    private static String generateContext(PointLinkVO vo, DataPointRT target, DataPointRT source) {
        String context;
        if(vo != null) {
            context = pointLinkInfo(vo);
        } else {
            context = "[no pointLink in context]";
        }

        if(target != null && target.getVO() != null) {
            context += ", target: " + dataPointInfo(target.getVO());
        }

        if(source != null && source.getVO() != null) {
            context += ", source: " + dataPointInfo(source.getVO());
        }
        return context;
    }

    private static String generateContext(ScriptVO<?> vo) {
        String context = "";

        if(vo != null) {
            context = scriptInfo(vo);
        }
        return context;
    }

    private static String dataPointInfo(DataPointVO dataPointVO) {
        String dataPointInfo = "datapoint: {0} (id: {1}, xid: {2})";
        return MessageFormat.format(dataPointInfo, dataPointVO.getName(), String.valueOf(dataPointVO.getId()), dataPointVO.getXid());
    }

    private static String dataSourceInfo(DataSourceRT dataSource) {
        String dataSourceInfo = "datasource: {0} (id: {1})";
        return MessageFormat.format(dataSourceInfo, dataSource.getName(), String.valueOf(dataSource.getId()));
    }

    private static String pointLinkInfo(PointLinkVO pointLink) {
        String dataPointInfo = "pointLink: {0} (id: {1}, xid: {2})";
        return MessageFormat.format(dataPointInfo, pointLink.getTypeKey(), String.valueOf(pointLink.getId()), pointLink.getXid());
    }

    private static String scriptInfo(ScriptVO<?> script) {
        String dataPointInfo = "script: {0} (id: {1}, xid: {2})";
        return MessageFormat.format(dataPointInfo, script.getName(), String.valueOf(script.getId()), script.getXid());
    }

    private static String exceptionInfo(Exception ex) {
        String exceptionInfo = "exception: {0} (msg: {1})";
        return MessageFormat.format(exceptionInfo, ex.getClass().getSimpleName(), ex.getMessage());
    }

    private static String scriptComponentInfo(ScriptComponent pointComponent) {
        String pointComponentInfo = "scriptComponent: {0} (id: {1}, type: {2}, x: {3}, y: {4})";
        return MessageFormat.format(pointComponentInfo, pointComponent.getDefName(),
                pointComponent.getId(), pointComponent.getClass().getSimpleName(), pointComponent.getX(),
                pointComponent.getY());
    }

    private static String identifierInfo(ScadaObjectIdentifier identifier, String object) {
        String viewInfo = object + ": {0} (id: {1}, xid: {2})";
        return MessageFormat.format(viewInfo, identifier.getName(), identifier.getId(), identifier.getXid());
    }
}
