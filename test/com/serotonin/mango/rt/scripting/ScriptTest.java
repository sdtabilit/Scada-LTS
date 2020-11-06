package com.serotonin.mango.rt.scripting;

import br.org.scadabr.rt.scripting.ContextualizedScriptRT;
import br.org.scadabr.rt.scripting.context.DPCommandsScriptContextObject;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import br.org.scadabr.vo.scripting.ContextualizedScriptVO;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueCache;
import com.serotonin.mango.rt.dataImage.types.*;
import com.serotonin.mango.vo.permission.Permissions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.scada_lts.dao.DAO;
import utils.IntValuePairPrinted;
import utils.ScriptTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

import static utils.ScriptTestUtils.createContext;
import static utils.ScriptTestUtils.createDataPointRT;
import static utils.Scripts.createScriptReturnState;
import static utils.Scripts.createScriptWithJavaPointValueTime;
import static utils.Scripts.createScriptWithJavaViewDwr;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({DAO.class, PointValueCache.class, Permissions.class,
        ContextualizedScriptRT.class, ScriptContextObject.class,
        Common.class})
public class ScriptTest {

    @Parameterized.Parameters(name = "{index}: value: {0}, point context: {2}, script: {3}")
    public static Object[][] data() {
        int pointToReadId = 456;
        IntValuePair pointToReadPair = new IntValuePair(pointToReadId, "abc");
        MangoValue binaryValue = new BinaryValue(true);
        MangoValue multistateValue = new MultistateValue(1234);
        MangoValue numericValue = new NumericValue(567.8);
        MangoValue alphanumericValue = new AlphanumericValue("cde");
        MangoValue binaryValue2 = new BinaryValue(false);
        MangoValue multistateValue2 = new MultistateValue(5678);
        MangoValue numericValue2 = new NumericValue(9.1);
        MangoValue alphanumericValue2 = new AlphanumericValue("fgh");

        List<IntValuePairPrinted> pointContext = Collections.singletonList(new IntValuePairPrinted(pointToReadPair));

        return new Object[][] {
                {binaryValue,
                        pointToReadId,
                        pointContext,
                        createScriptReturnState(pointToReadPair)},
                {multistateValue,
                        pointToReadId,
                        pointContext,
                        createScriptReturnState(pointToReadPair)},
                {numericValue,
                        pointToReadId,
                        pointContext,
                        createScriptReturnState(pointToReadPair)},
                {alphanumericValue,
                        pointToReadId,
                        pointContext,
                        createScriptReturnState(pointToReadPair)},

                {binaryValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptReturnState(binaryValue2)},
                {multistateValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptReturnState(multistateValue2)},
                {numericValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptReturnState(numericValue2)},
                {alphanumericValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptReturnState(alphanumericValue2)},

                {binaryValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptWithJavaPointValueTime(binaryValue2)},
                {multistateValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptWithJavaPointValueTime(multistateValue2)},
                {numericValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptWithJavaPointValueTime(numericValue2)},
                {alphanumericValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        createScriptWithJavaPointValueTime(alphanumericValue2)},

                {new AlphanumericValue(""),
                        0,
                        Collections.emptyList(),
                        createScriptWithJavaViewDwr()},
    };
    }

    private final MangoValue mangoValue;
    private final List<IntValuePair> pointContext;
    private final int pointToReadId;
    private final String script;

    public ScriptTest(MangoValue mangoValue,
                      int pointToReadId,
                      List<IntValuePairPrinted> pointContext,
                      String script) {
        this.mangoValue = mangoValue;
        this.pointContext = pointContext.stream().map(IntValuePairPrinted::getIntValuePair).collect(Collectors.toList());
        this.pointToReadId = pointToReadId;
        this.script = script;
    }

    private RuntimeManager runtimeManager = mock(RuntimeManager.class);
    private DPCommandsScriptContextObject scriptContextObject = mock(DPCommandsScriptContextObject.class);

    @Before
    public void config() throws Exception {
        ScriptTestUtils.configMock(runtimeManager, scriptContextObject);
    }

    @Test
    public void test_execute_js_with_object_context_writeDataPoint() throws Exception {

        //given:
        ContextualizedScriptVO contextualizedScriptVO = createContext(pointContext, Collections.emptyList(),
                script);
        ContextualizedScriptRT contextualizedScriptRT = new ContextualizedScriptRT(contextualizedScriptVO);
        DataPointRT pointToRead = createDataPointRT(pointToReadId, mangoValue);

        //mock:
        when(runtimeManager.getDataPoint(eq(pointToReadId))).thenReturn(pointToRead);

        //when
        contextualizedScriptRT.execute();

    }
}
