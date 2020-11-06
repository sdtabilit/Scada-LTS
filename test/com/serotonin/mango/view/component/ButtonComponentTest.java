package com.serotonin.mango.view.component;

import br.org.scadabr.rt.scripting.ContextualizedScriptRT;
import br.org.scadabr.rt.scripting.context.DPCommandsScriptContextObject;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import br.org.scadabr.view.component.ButtonComponent;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueCache;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.AlphanumericValue;
import com.serotonin.mango.rt.dataImage.types.BinaryValue;
import com.serotonin.mango.rt.dataImage.types.MultistateValue;
import com.serotonin.mango.rt.dataImage.types.NumericValue;
import com.serotonin.mango.rt.dataSource.meta.ScriptExecutor;
import com.serotonin.mango.view.text.MultistateRenderer;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.permission.Permissions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.scada_lts.dao.DAO;
import utils.ScriptTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DAO.class, PointValueCache.class, Permissions.class,
        ContextualizedScriptRT.class, ScriptContextObject.class,
        ScriptExecutor.class, Common.class})
public class ButtonComponentTest {

    @Before
    public void config() throws Exception {

        RuntimeManager runtimeManager = mock(RuntimeManager.class);
        DPCommandsScriptContextObject scriptContextObject = mock(DPCommandsScriptContextObject.class);
        ScriptTestUtils.configMock(runtimeManager, scriptContextObject);
    }

    @Test
    public void test_execute_js_with_button_off_script_html() {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new BinaryValue(true), 0);

        ButtonComponent scriptComponent = new ButtonComponent();

        DataPointRT dataPointRT = ScriptTestUtils.createDataPointRT(1234, value.getValue());
        TextRenderer textRenderer = new MultistateRenderer();
        DataPointVO dataPointVO = dataPointRT.getVO();
        dataPointVO.setTextRenderer(textRenderer);

        scriptComponent.setNameOverride("point");
        scriptComponent.setBkgdColorOverride("");
        scriptComponent.setDisplayControls(true);
        scriptComponent.setSettableOverride(true);
        scriptComponent.tsetDataPoint(dataPointVO);

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("<input type='button' value='OFF' onclick='mango.view.setPoint(1234,0, false);return false;' />", result);
    }

    @Test
    public void test_execute_js_with_button_on_script_html() {

        //given:
        Map<String, Object> model = new HashMap<>();
        PointValueTime value = new PointValueTime(new BinaryValue(false), 0);

        ButtonComponent scriptComponent = new ButtonComponent();

        DataPointRT dataPointRT = ScriptTestUtils.createDataPointRT(1234, value.getValue());
        TextRenderer textRenderer = new MultistateRenderer();
        DataPointVO dataPointVO = dataPointRT.getVO();
        dataPointVO.setTextRenderer(textRenderer);

        scriptComponent.setNameOverride("point");
        scriptComponent.setBkgdColorOverride("");
        scriptComponent.setDisplayControls(true);
        scriptComponent.setSettableOverride(true);
        scriptComponent.tsetDataPoint(dataPointVO);

        //when:
        scriptComponent.addDataToModel(model, value);

        //then:
        Object result = model.get("scriptContent");
        Assert.assertEquals("<input type='button' value='ON' onclick='mango.view.setPoint(1234,0, true);return true;' />", result);
    }

}
