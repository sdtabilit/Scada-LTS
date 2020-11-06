package com.serotonin.mango.rt.scripting;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        ScriptTest.class,
        ScriptWithObjectContextEnableDisableDataPointTest.class,
        ScriptWithObjectContextEnableDisableDataSourceTest.class,
        ScriptWithObjectContextWriteDataPointTest.class
})
public class ContextualizedScriptRTTestsSuite {
}