package com.serotonin.mango.rt.dataImage;

import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import org.junit.Assert;
import org.junit.Test;
import org.scada_lts.cache.PointValueCache;

public class PointValueFacadeTest {

    private DataPointRT dataPointRT;
    private PointValueCache pointValueCache;

    @Test
    public void fillCacheWithPointValueWhichContainsOneNotEmptySource_Test(){
        final String GIVEN_USERNAME="testName";
        final String DO_THIS_USERNAME_EXIST="testName";
        DataPointVO dataPointVO = new DataPointVO();
        dataPointVO.setId(2);
        dataPointVO.setDefaultCacheSize(10);

        dataPointRT = new DataPointRT(dataPointVO);
        pointValueCache = dataPointRT.getPointValueCache();
        pointValueCache.addPointValueTimeIntoCacheForTest(new PointValueTime(true,new Long(3)));

        User source = new User();
        source.setUsername(GIVEN_USERNAME);
        PointValueTime pvt = new PointValueTime(true, new Long(5));
        pointValueCache.setMaxSize(10);
        pointValueCache.savePointValueIntoCacheAndIflogValueIntoDbAsyncOrSync(pvt,source,Boolean.FALSE,Boolean.FALSE);
        boolean sourceIsNotEmpty=Boolean.FALSE;
        for(PointValueTime pointValueTime:pointValueCache.getLatestPointValuesUsedForTest(2))
        {
            if(pointValueTime.getWhoChangedValue().equals(DO_THIS_USERNAME_EXIST)) sourceIsNotEmpty=Boolean.TRUE;
        }
        Assert.assertEquals(Boolean.TRUE,sourceIsNotEmpty);

    }


}
