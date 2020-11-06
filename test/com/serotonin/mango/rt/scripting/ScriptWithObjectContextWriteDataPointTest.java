package com.serotonin.mango.rt.scripting;

import br.org.scadabr.rt.scripting.ContextualizedScriptRT;
import br.org.scadabr.rt.scripting.context.DPCommandsScriptContextObject;
import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import br.org.scadabr.vo.scripting.ContextualizedScriptVO;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import utils.IntValuePairPrinted;
import utils.ScriptTestUtils;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueCache;
import com.serotonin.mango.rt.dataImage.types.*;
import com.serotonin.mango.vo.permission.Permissions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.scada_lts.dao.DAO;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static utils.ScriptTestUtils.createContext;
import static utils.ScriptTestUtils.createDataPointRT;
import static utils.Scripts.createScriptWithObjectContextWriteDataPoint;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({DAO.class, PointValueCache.class, Permissions.class,
        ContextualizedScriptRT.class, ScriptContextObject.class,
        Common.class})
public class ScriptWithObjectContextWriteDataPointTest {

    private static String pointToChangeXid = "DP_093765";

    @Parameterized.Parameters(name = "{index}: value: {0}, point context: {2}, object context: {3}, script: {4}")
    public static Object[][] data() {
        int pointToReadId = 456;
        IntValuePair pointToReadPair = new IntValuePair(pointToReadId, "abc");
        IntValuePair objectContextDataPointPair = new IntValuePair(2, "dp");
        MangoValue binaryValue = new BinaryValue(true);
        MangoValue multistateValue = new MultistateValue(1234);
        MangoValue numericValue = new NumericValue(567.8);
        MangoValue alphanumericValue = new AlphanumericValue("cde");
        MangoValue binaryValue2 = new BinaryValue(false);
        MangoValue multistateValue2 = new MultistateValue(5678);
        MangoValue numericValue2 = new NumericValue(9.1);
        MangoValue alphanumericValue2 = new AlphanumericValue("fgh");

        List<IntValuePairPrinted> pointContext = Collections.singletonList(new IntValuePairPrinted(pointToReadPair));
        List<IntValuePairPrinted> objectContext = Collections.singletonList(new IntValuePairPrinted(objectContextDataPointPair));

        return new Object[][] {
                {binaryValue,
                        pointToReadId,
                        pointContext,
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, pointToReadPair)},
                {multistateValue,
                        pointToReadId,
                        pointContext,
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, pointToReadPair)},
                {numericValue,
                        pointToReadId,
                        pointContext,
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, pointToReadPair)},
                {alphanumericValue,
                        pointToReadId,
                        pointContext,
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, pointToReadPair)},

                {binaryValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, binaryValue2)},
                {multistateValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, multistateValue2)},
                {numericValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, numericValue2)},
                {alphanumericValue2,
                        pointToReadId,
                        Collections.emptyList(),
                        objectContext,
                        createScriptWithObjectContextWriteDataPoint(pointToChangeXid, alphanumericValue2)}
    };
    }

    private final MangoValue mangoValue;
    private final int pointToReadId;
    private final List<IntValuePair> pointContext;
    private final List<IntValuePair> objectContext;
    private final String script;

    public ScriptWithObjectContextWriteDataPointTest(MangoValue mangoValue,
                                                     int pointToReadId,
                                                     List<IntValuePairPrinted> pointContext,
                                                     List<IntValuePairPrinted> objectContext,
                                                     String script) {
        this.mangoValue = mangoValue;
        this.pointToReadId = pointToReadId;
        this.pointContext = pointContext.stream().map(IntValuePairPrinted::getIntValuePair).collect(Collectors.toList());
        this.objectContext = objectContext.stream().map(IntValuePairPrinted::getIntValuePair).collect(Collectors.toList());
        this.script = script;
    }

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
    public void test_execute_js_with_object_context_writeDataPoint() throws Exception {

        //given:
        ContextualizedScriptVO contextualizedScriptVO = createContext(pointContext, objectContext, script);
        ContextualizedScriptRT contextualizedScriptRT = new ContextualizedScriptRT(contextualizedScriptVO);
        DataPointRT pointToRead = createDataPointRT(pointToReadId, mangoValue);

        //mock:
        when(runtimeManager.getDataPoint(eq(pointToReadId))).thenReturn(pointToRead);

        //when
        contextualizedScriptRT.execute();

        //then
        verify(scriptContextObject, times(1))
                .writeDataPoint(eq(pointToChangeXid), eq(String.valueOf(mangoValue.getObjectValue())));
    }
}
