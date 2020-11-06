package com.serotonin.mango.view.component;

import br.org.scadabr.rt.scripting.ContextualizedScriptRT;
import br.org.scadabr.rt.scripting.context.DPCommandsScriptContextObject;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.PointValueCache;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.rt.dataSource.meta.ScriptExecutor;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.PropertiesUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.scada_lts.dao.DAO;
import utils.ScriptTestUtils;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DAO.class, PointValueCache.class, Permissions.class,
        ContextualizedScriptRT.class, ScriptContextObject.class,
        ScriptExecutor.class, Common.class, ScriptComponent.class})
public class ScriptComponentTest {

    @BeforeClass
    public static void config() throws Exception {

        RuntimeManager runtimeManager = mock(RuntimeManager.class);
        DPCommandsScriptContextObject scriptContextObject = mock(DPCommandsScriptContextObject.class);
        ScriptTestUtils.configMock(runtimeManager, scriptContextObject);
    }

    @Test
    public void test_execute_js_with_return_state() {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new BinaryValue(true), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("return 2.2;");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("2.2", result);
    }

    @Test
    public void test_execute_js_with_return_state_alphanumeric() {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new BinaryValue(true), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("return 'abc';");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("abc", result);
    }

    @Test
    public void test_execute_js_with_point_context_binary() {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new BinaryValue(true), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("if (this.value) return 5; return 3;");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void test_execute_js_with_point_context_numeric() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new NumericValue(1.1), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("if (this.value == 1.1) return 5; return 3;");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void test_execute_js_with_point_context_alphanumeric() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new AlphanumericValue("alphanumeric test"), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("if (this.value == 'alphanumeric test') return 5; return 3;");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("5.0", result);
    }


    @Test
    public void test_execute_js_with_point_context_multistatic() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new MultistateValue(1234), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("if (this.value == 1234) return 5; return 3;");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void test_execute_js_with_java_PointValueTime() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new MultistateValue(1234), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("var pointValueTime = com.serotonin.mango.rt.dataImage.PointValueTime(new com.serotonin.mango.rt.dataImage.types.NumericValue(12345.5),0);" +
                "return pointValueTime.getDoubleValue();");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("12345.5", result);

    }

    @Test
    public void test_execute_js_with_java_PointValueTime_new() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new MultistateValue(1234), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("var pointValueTime = new com.serotonin.mango.rt.dataImage.PointValueTime(new com.serotonin.mango.rt.dataImage.types.NumericValue(12345.5),0);" +
                "return pointValueTime.getDoubleValue();");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("12345.5", result);

    }

    @Test
    public void test_execute_js_with_java_create_ViewDwr() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new MultistateValue(1234), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("var mydwr=new com.serotonin.mango.web.dwr.ViewDwr();" +
                "return 'test_new_ViewDwr';");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("test_new_ViewDwr", result);
    }

    @Test
    public void test_execute_js_with_java_invoke_method_getLoggedUser_in_ViewDwr() throws Exception {

        //given:
        String userName = "user mock";

        mockStatic(Common.class);
        User user = new User();
        user.setUsername(userName);
        when(Common.getUser()).thenReturn(user);

        PropertiesUtils propertiesUtils = new PropertiesUtils("env");
        PowerMockito.when(Common.getEnvironmentProfile()).thenReturn(propertiesUtils);

        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new MultistateValue(1234), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("var mydwr=new com.serotonin.mango.web.dwr.ViewDwr();" +
                "var user=mydwr.getLoggedUser();" +
                "return user + '';");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("user mock", result);
    }

    @Test
    public void test_execute_js_with_html() throws Exception {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new AlphanumericValue("abc"), 0);

        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.setScript("if (this.value != '') \n  " +
                "return \"<a href='/ScadaBR/views.shtm?viewId=39#SA'><font color=white><FONT style='BACKGROUND-COLOR: red'><b>&nbsp;Not&nbsp;Aus!&nbsp;</b><font color=black><FONT style='BACKGROUND-COLOR: white'></a>\"; " +
                "\nelse\n  " +
                "return \"<a href='/ScadaBR/views.shtm?viewId=5'><font color=white><FONT style='BACKGROUND-COLOR: #C0C0C0'>&nbsp;Not&nbsp;Aus&nbsp;<font color=black><FONT style='BACKGROUND-COLOR: white'</a>\";");

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("<a href='/ScadaBR/views.shtm?viewId=39#SA'><font color=white><FONT style='BACKGROUND-COLOR: red'><b>&nbsp;Not&nbsp;Aus!&nbsp;</b><font color=black><FONT style='BACKGROUND-COLOR: white'></a>", result);
    }
}
