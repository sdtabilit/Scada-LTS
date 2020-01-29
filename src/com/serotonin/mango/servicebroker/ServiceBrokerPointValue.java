package com.serotonin.mango.servicebroker;

import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;

import java.util.LinkedList;

/**
 * Responsibility:
 *
 * interface to broker between PointValueCache and PointValueDao
 *
 * @author Mateusz Hyski Abil'I.T. development team, sdt@abilit.eu,mateusz.hyski@softq.pl, hyski.mateusz@gmail.com
 */
public interface ServiceBrokerPointValue {

    PointValueTime savePointValueIntoDatabaseAsyncOrSync(
            PointValueTime pointValueTime,
            SetPointSource pointSource,
            boolean async,
            int dataPointId);

    void savePointValueAsyncToDao(PointValueTime pointValue, SetPointSource source, int dataPointId);

    PointValueTime getLatestPointValueFromDao(int dataPointId);

    LinkedList<PointValueTime> getLimitRowsOfLatestPointValuesForGivenDataPointId(int dataPointId, int limit);
}
