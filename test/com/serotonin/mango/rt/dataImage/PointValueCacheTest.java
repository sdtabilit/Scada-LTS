package com.serotonin.mango.rt.dataImage;

import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.link.PointLinkRT;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.link.PointLinkVO;
import org.junit.Assert;
import org.junit.Test;
import org.scada_lts.cache.PointValueCache;
import java.util.ArrayList;
import java.util.List;

public class PointValueCacheTest {

    @Test
    public void sourceIsNotEmptyFillNeededPropertyInPointValueTime_Test(){
        User source = new User();
        source.setUsername("testName");
        PointValueTime pvt = savePropertiesAboutOwnerOfPointValueChange(source);
        Assert.assertEquals("testName",pvt.getWhoChangedValue());
    }

    @Test
    public void sourceIsEmptyNotFillNeededPropertyInPointValueTime_Test(){
        PointLinkRT source = new PointLinkRT(new PointLinkVO());
        PointValueTime pointValueTime = savePropertiesAboutOwnerOfPointValueChange(source);
        Assert.assertNotSame("testName",pointValueTime.getWhoChangedValue());
    }

    @Test
    public void addMoreInstancesIntoCacheThanMaxSize_Test( ){

        List<PointValueTime> pointValueTimesList = new ArrayList<PointValueTime>();
        int MAX_SIZE_OF_POINTVALUECACHE = 2;
        int COUNTER_OF_OBJECTS_IN_POINTVALUECACHE = 0;

        PointValueCache pointValueCache = new PointValueCache(-1,-1);
        pointValueCache.setMaxSize( MAX_SIZE_OF_POINTVALUECACHE );

        for(int pointValue = 1;pointValue <4;pointValue++) {

            addNewPointValueTimeIntoCacheAndLocalList(
                    new PointValueTime(MangoValue.stringToValue(String.valueOf(pointValue), 3), new Long(1)),
                    pointValueCache,
                    pointValueTimesList);

            COUNTER_OF_OBJECTS_IN_POINTVALUECACHE = doCacheContainsAllInsertedPointValueTimeObjects(pointValueTimesList,pointValueCache);

        }
        Assert.assertEquals(MAX_SIZE_OF_POINTVALUECACHE,COUNTER_OF_OBJECTS_IN_POINTVALUECACHE);
    }

    private PointValueTime savePropertiesAboutOwnerOfPointValueChange (SetPointSource source ){
        PointValueTime pointValueTime = new PointValueTime(MangoValue.stringToValue("1",1), new Long(1));
        PointValueCache pointValueCache = new PointValueCache(-1,-1);
        pointValueCache.savePointValueIntoCacheAndIflogValueIntoDbAsyncOrSync(pointValueTime,source,Boolean.FALSE,Boolean.FALSE);
        return pointValueTime;
    }

    private void addNewPointValueTimeIntoCacheAndLocalList(
            PointValueTime pointValueTime,
            PointValueCache pointValueCache,
            List<PointValueTime> pointValueTimesList){

        pointValueCache.savePointValueIntoCacheAndIflogValueIntoDbAsyncOrSync(pointValueTime, null, Boolean.FALSE, Boolean.FALSE);
        pointValueTimesList.add(pointValueTime);
    }

    private int doCacheContainsAllInsertedPointValueTimeObjects(
            List<PointValueTime> pointValueTimesList,
            PointValueCache pointValueCache){
        int COUNTER_OF_OBJECTS_IN_POINTVALUECACHE = 0;
        for(PointValueTime pointValueTime : pointValueTimesList) {

            if (pointValueCache.getCacheContents().contains(pointValueTime)) {
                COUNTER_OF_OBJECTS_IN_POINTVALUECACHE++;
            }
        }

        return COUNTER_OF_OBJECTS_IN_POINTVALUECACHE;
    }
}
