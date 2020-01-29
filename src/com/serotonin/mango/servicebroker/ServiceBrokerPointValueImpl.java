package com.serotonin.mango.servicebroker;

import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.servicebroker.ServiceBrokerPointValue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Responsibility:
 *
 * That is broker between PointValueCache and PointValueDao
 *
 * @author Mateusz Hyski Abil'I.T. development team, sdt@abilit.eu,mateusz.hyski@softq.pl, hyski.mateusz@gmail.com
 */
public class ServiceBrokerPointValueImpl implements ServiceBrokerPointValue {

    private PointValueDao pointValueDao;

    public ServiceBrokerPointValueImpl() {}

    @Override
    public PointValueTime savePointValueIntoDatabaseAsyncOrSync(
            PointValueTime pointValueTime,
            SetPointSource pointSource,
            boolean async,
            int dataPointId){

        if (async) {
            getPointValueDao().savePointValueAsync(dataPointId, pointValueTime, pointSource);
        }
        else {
            pointValueTime = getPointValueDao().savePointValueSync(dataPointId, pointValueTime, pointSource);
        }

        return pointValueTime;
    }
    @Override
    public void savePointValueAsyncToDao(PointValueTime pointValue, SetPointSource source, int dataPointId) {
        // Save the new value and get a point value time back that has the id and annotations set, as appropriate.
        getPointValueDao().savePointValueAsync(dataPointId, pointValue, source);

    }
    @Override
    public PointValueTime getLatestPointValueFromDao(int dataPointId) {

        return getPointValueDao().getLatestPointValue( dataPointId );

    }

    @Override
    public LinkedList<PointValueTime> getLimitRowsOfLatestPointValuesForGivenDataPointId(int dataPointId, int limit) {

        LinkedList<PointValueTime> pointValueTimes = new LinkedList<PointValueTime>();

        for(PointValueTime pointValueTime: getLatestPointValuesForDataPointIdAndLimit(dataPointId,limit)){
            pointValueTimes.addFirst(pointValueTime);
        }

        return pointValueTimes;
    }

    private List<PointValueTime> getLatestPointValuesForDataPointIdAndLimit(int dataPointId, int limit) {

        List<PointValueTime> pointValueTimes = getPointValueDao().getLatestPointValues(dataPointId,limit);

        return (pointValueTimes == null)
                ?Collections.emptyList()
                :pointValueTimes;
    }

    private PointValueDao getPointValueDao() {

        if(pointValueDao == null) {
            pointValueDao = new PointValueDao();
        }

        return pointValueDao;

    }
}
