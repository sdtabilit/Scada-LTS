package utils;

import br.org.scadabr.rt.scripting.context.ScriptContextObject;
import br.org.scadabr.vo.scripting.ContextualizedScriptVO;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.dataSource.PointLocatorVO;
import com.serotonin.mango.vo.dataSource.virtual.VirtualPointLocatorVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.ContextWrapper;
import com.serotonin.util.PropertiesUtils;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

public class ScriptTestUtils {

    public static ContextualizedScriptVO createContext(List<IntValuePair> pointContext,
                                                       List<IntValuePair> objectContext,
                                                       String script) {
        ContextualizedScriptVO contextualizedScriptVO = new ContextualizedScriptVO();
        contextualizedScriptVO.setPointsOnContext(pointContext);
        contextualizedScriptVO.setObjectsOnContext(objectContext);
        contextualizedScriptVO.setScript(script);
        return contextualizedScriptVO;
    }


    public static DataPointRT createDataPointRT(int pointFromContextId, MangoValue mangoValue) {
        PointLocatorVO locatorFromContext = new VirtualPointLocatorVO();
        ((VirtualPointLocatorVO) locatorFromContext).setDataTypeId(mangoValue.getDataType());

        DataPointVO pointFromContextVO = new DataPointVO();
        pointFromContextVO.setPointLocator(locatorFromContext);
        pointFromContextVO.setEventDetectors(Collections.emptyList());
        pointFromContextVO.setId(pointFromContextId);
        pointFromContextVO.setName("point test");

        DataPointRT pointFromContext = new DataPointRT(pointFromContextVO, null);
        pointFromContext.initialize();
        pointFromContext.setPointValue(new PointValueTime(mangoValue,0), null);

        return pointFromContext;
    }

    public static void configMock(RuntimeManager runtimeManager, ScriptContextObject scriptContextObject) throws Exception {
        ContextWrapper contextWrapper = mock(ContextWrapper.class);
        ServletContext servletContext = mock(ServletContext.class);
        Common.ctx = contextWrapper;
        when(contextWrapper.getRuntimeManager()).thenReturn(runtimeManager);
        when(contextWrapper.getServletContext()).thenReturn(servletContext);
        when(servletContext.getRealPath(anyString())).thenReturn("test/scriptFunctions.js");

        PointValueDao pointValueDao = mock(PointValueDao.class);
        whenNew(PointValueDao.class)
                .withNoArguments()
                .thenReturn(pointValueDao);

        DAO dao = mock(DAO.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dao.getJdbcTemp()).thenReturn(jdbcTemplate);
        whenNew(DAO.class)
                .withNoArguments()
                .thenReturn(dao);

        UserDao userDao = mock(UserDao.class);
        whenNew(UserDao.class)
                .withNoArguments()
                .thenReturn(userDao);

        String userName = "user mock";
        User user = new User();
        user.setUsername(userName);
        when(userDao.getUser(any())).thenReturn(user);
        when(userDao.getUser(anyInt())).thenReturn(user);

        mockStatic(Common.class);
        when(Common.getUser()).thenReturn(user);

        PropertiesUtils propertiesUtils = new PropertiesUtils("env");
        when(Common.getEnvironmentProfile()).thenReturn(propertiesUtils);

        mockStatic(Permissions.class);

        mockStatic(ScriptContextObject.Type.class);
        ScriptContextObject.Type type = mock(ScriptContextObject.Type.class);
        when(ScriptContextObject.Type.valueOf(anyInt()))
                .thenReturn(type);
        when(type.createScriptContextObject()).thenReturn(scriptContextObject);
    }

}
