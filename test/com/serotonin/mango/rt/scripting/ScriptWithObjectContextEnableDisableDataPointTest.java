package com.serotonin.mango.rt.scripting;

import br.org.scadabr.rt.scripting.ContextualizedScriptRT;
import br.org.scadabr.rt.scripting.context.DPCommandsScriptContextObject;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import br.org.scadabr.vo.scripting.ContextualizedScriptVO;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import utils.ScriptTestUtils;
import com.serotonin.mango.rt.dataImage.PointValueCache;
import com.serotonin.mango.vo.permission.Permissions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.scada_lts.dao.DAO;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static utils.ScriptTestUtils.createContext;
import static utils.Scripts.createScriptWithObjectContextDisableDataPoint;
import static utils.Scripts.createScriptWithObjectContextEnableDataPoint;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PrepareForTest({DAO.class, PointValueCache.class, Permissions.class,
        ContextualizedScriptRT.class, ScriptContextObject.class,
        Common.class})
public class ScriptWithObjectContextEnableDisableDataPointTest {

    private static String pointToChangeXid = "DP_093765";
    private final List<IntValuePair> objectContext = Arrays.asList(new IntValuePair(2, "dp"));
    private RuntimeManager runtimeManager = mock(RuntimeManager.class);
    private DPCommandsScriptContextObject scriptContextObject = mock(DPCommandsScriptContextObject.class);

    @Before
    public void config() throws Exception {
        ScriptTestUtils.configMock(runtimeManager, scriptContextObject);
    }

    @After
    public void resetMock() {
        reset(scriptContextObject);
        reset(runtimeManager);
    }

    @Test
    public void test_execute_js_with_object_context_enableDataPoint() throws Exception {

        //given:
        String script = createScriptWithObjectContextEnableDataPoint(pointToChangeXid);
        ContextualizedScriptVO contextualizedScriptVO = createContext(Collections.emptyList(), objectContext, script);
        ContextualizedScriptRT contextualizedScriptRT = new ContextualizedScriptRT(contextualizedScriptVO);

        //when
        contextualizedScriptRT.execute();

        //then
        verify(scriptContextObject, times(1))
                .enableDataPoint(eq(pointToChangeXid));
        verify(scriptContextObject, times(0))
                .disableDataPoint(eq(pointToChangeXid));
    }

    @Test
    public void test_execute_js_with_object_context_disableDataPoint() throws Exception {

        //given:
        String script = createScriptWithObjectContextDisableDataPoint(pointToChangeXid);
        ContextualizedScriptVO contextualizedScriptVO = createContext(Collections.emptyList(), objectContext, script);
        ContextualizedScriptRT contextualizedScriptRT = new ContextualizedScriptRT(contextualizedScriptVO);

        //when
        contextualizedScriptRT.execute();

        //then
        verify(scriptContextObject, times(1))
                .disableDataPoint(eq(pointToChangeXid));
        verify(scriptContextObject, times(0))
                .enableDataPoint(eq(pointToChangeXid));
    }

    @Test
    public void test_execute_js_with_object_context_enableDataPoint_disableDataPoint() throws Exception {

        //given:
        String script = createScriptWithObjectContextEnableDataPoint(pointToChangeXid) + "\n" +
                createScriptWithObjectContextDisableDataPoint(pointToChangeXid);
        ContextualizedScriptVO contextualizedScriptVO = createContext(Collections.emptyList(), objectContext, script);
        ContextualizedScriptRT contextualizedScriptRT = new ContextualizedScriptRT(contextualizedScriptVO);

        //when
        contextualizedScriptRT.execute();

        //then
        verify(scriptContextObject, times(1))
                .enableDataPoint(eq(pointToChangeXid));
        verify(scriptContextObject, times(1))
                .disableDataPoint(eq(pointToChangeXid));
    }
}
