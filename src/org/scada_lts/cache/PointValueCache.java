/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.scada_lts.cache;

import com.serotonin.mango.servicebroker.ServiceBrokerPointValue;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.vo.User;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

/**
 * This class maintains an ordered list of the most recent values for a data point. It will mirror values in the
 * database, but provide a much faster lookup for a limited number of values.
 * 
 * Because there is not a significant performance problem for time-based lookups, they are not handled here, but rather
 * are still handled by the database.
 * 
 * @author Matthew Lohbihler
 */
public class PointValueCache {
    private final int dataPointId;
    private final int defaultSize;
    private int maxSize = 0;

    private ServiceBrokerPointValue serviceBrokerPointValue;

    /**
     * IMPORTANT: The list object should never be written to! The implementation here is for performance. Never call
     * methods like add() or remove() on the cache object. Further, since the cache object can be replaced from time to
     * time, always use a local copy of the variable for read purposes.
     */
    private LinkedList<PointValueTime>  cache = new LinkedList<PointValueTime>();

    public PointValueCache(int newDataPointId, int newDefaultSize){
        this.dataPointId = newDataPointId;
        this.defaultSize = newDefaultSize;
    }

    public PointValueCache(int dataPointId, int defaultSize, ServiceBrokerPointValue serviceBrokerPointValue) {
        this(dataPointId,defaultSize);
        this.serviceBrokerPointValue = serviceBrokerPointValue;

        if (defaultSize > 0) {
            refreshCacheByReadNeededDataFromDbDependsOnGivenLimit(defaultSize);
        }
    }
    public List<PointValueTime> getLatestPointValuesUsedForTest(int limit){

        return getCacheContents();
    }

    /**
     * "That method is used only for Junit test and shouldn't be used  in normal work of Scada.
     *
     * @param pointValueTime
     */
    public void addPointValueTimeIntoCacheForTest(PointValueTime pointValueTime){

        savePointValueInCache(pointValueTime);
    }
    public int getDataPointId() {
        return dataPointId;
    }

    public void savePointValueIntoCacheAndIflogValueIntoDbAsyncOrSync(PointValueTime pointValueTime, SetPointSource source, boolean logValue, boolean async) {

        if (logValue) {
            getServiceBrokerPointValue().savePointValueIntoDatabaseAsyncOrSync(

                            pointValueTime,
                            source,
                            async,
                            getDataPointId()
                    );
        }

        setChangeOwnerIfSourceIsNotEmpty( pointValueTime, source);

        savePointValueInCache( pointValueTime );

    }
    private void setChangeOwnerIfSourceIsNotEmpty(PointValueTime pvt, SetPointSource source) {

        if(source!=null)
        {
            if(source instanceof User)
            {
                pvt.setWhoChangedValue(((User)source).getUsername());
            }
        }

    }

    /**
     * "That method is used only for Junit test and shouldn't be used  in normal work of Scada.
     * @param newMaxSize
     */
    public void setMaxSize(int newMaxSize) {

        maxSize = newMaxSize;
    }
    private void savePointValueInCache(PointValueTime pointValueTime) {

        if(cache.size() != 0){

            if(cache.size() == maxSize) {

                cache.removeLast();
                cache.addFirst(pointValueTime);

            }
            else {
                cache.addFirst(pointValueTime);
            }
        }
        else {
            cache.add(pointValueTime);
        }
    }

    /**
     * Saves the given value to the database without adding it to the cache.
     */
    public void savePointValueAsyncToDbByServiceBroker(PointValueTime pointValue, SetPointSource source) {
        // Save the new value and get a point value time back that has the id and annotations set, as appropriate.
        getServiceBrokerPointValue().savePointValueAsyncToDao( pointValue, source, getDataPointId() );
    }

    public PointValueTime getLatestPointValue() {

        refreshCacheDependsOnGivenLimit( 1 );

        if (cache.size() > 0) {
            return cache.get(0);
        }

        return null;
    }


    public List<PointValueTime> getLatestPointValues(int limit) {
        if (maxSize < limit) {
            refreshCacheByReadNeededDataFromDbDependsOnGivenLimit(limit);
        }

        int cacheSize =  cache.size();
        if (limit == cacheSize) {
            return cache;
        }

        if (limit > cacheSize) {
            limit = cacheSize;
        }
        return new ArrayList<PointValueTime>(cache.subList(0, limit));
    }

    public ServiceBrokerPointValue getServiceBrokerPointValue() {

        return serviceBrokerPointValue;

    }

    /**
     * Never manipulate the contents of this list!
     */
    public List<PointValueTime> getCacheContents() {
        return cache;
    }

    public void reset() {

        int size = defaultSize;
        int cacheSize = getCacheContents().size();

        if (cacheSize < size)
            size = cacheSize;

        maxSize = size;
    }
    private void refreshCacheDependsOnGivenLimit(int givenLimit ) {
        if (maxSize == 0) {
            refreshCacheByReadNeededDataFromDbDependsOnGivenLimit( givenLimit );
        }
    }
    private void refreshCacheByReadNeededDataFromDbDependsOnGivenLimit(int limit) {
        if (limit > maxSize) {
            maxSize = limit;
            if (limit == 1) {
                // Performance thingy
                readLatestPointValueFromDaoAndPutIntoCache( getDataPointId() );
            }
            else {
                getCacheContents().clear();
                cache = getServiceBrokerPointValue().getLimitRowsOfLatestPointValuesForGivenDataPointId( getDataPointId(),limit );
            }
        }
    }
    private void readLatestPointValueFromDaoAndPutIntoCache(int dataPointId){

        PointValueTime pointValueTime = getServiceBrokerPointValue().getLatestPointValueFromDao( dataPointId );

        if (pointValueTime != null) {
            savePointValueInCache( pointValueTime );
        }
    }

}
