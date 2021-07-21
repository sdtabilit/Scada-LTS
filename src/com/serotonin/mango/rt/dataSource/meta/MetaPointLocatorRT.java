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
package com.serotonin.mango.rt.dataSource.meta;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.rt.dataImage.DataPointListener;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;
import com.serotonin.mango.util.DateUtils;
import com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO;
import com.serotonin.timer.AbstractTimer;
import com.serotonin.timer.CronExpression;
import com.serotonin.timer.OneTimeTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.web.i18n.LocalizableMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.serotonin.mango.util.LoggingScriptUtils.generateContext;
import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorExecutionScript;
import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorInitializationScript;

/**
 * @author Matthew Lohbihler
 */
public class MetaPointLocatorRT extends PointLocatorRT implements DataPointListener {
    private static final ThreadLocal<List<Integer>> threadLocal = new ThreadLocal<List<Integer>>();
    private static final int MAX_RECURSION = 10;

    final Boolean LOCK = new Boolean(false);

    final MetaPointLocatorVO vo;
    AbstractTimer timer;
    private MetaDataSourceRT dataSource;
    protected DataPointRT dataPoint;
    protected Map<String, IDataPoint> context;
    boolean initialized;
    TimerTask timerTask;

    private final static Log LOG = LogFactory.getLog(MetaPointLocatorRT.class);

    public MetaPointLocatorRT(MetaPointLocatorVO vo) {
        this.vo = vo;
    }

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

    public MetaPointLocatorVO getPointLocatorVO() {
        return vo;
    }

    boolean isContextCreated() {
        return context != null;
    }

    //
    // Lifecycle stuff. Note, this class does not implement the lifecycle interface because it is not controlled by
    // the RuntimeManager. The owning data source calls these methods then the point is added and removed from the
    // data source.
    //
    public void initialize(AbstractTimer timer, MetaDataSourceRT dataSource, DataPointRT dataPoint) {
        this.timer = timer;
        this.dataSource = dataSource;
        this.dataPoint = dataPoint;

        createContext();

        // Add listener registrations
        RuntimeManager rm = Common.ctx.getRuntimeManager();
        for (IntValuePair contextKey : vo.getContext()) {
            // Points shouldn't listen for their own updates.
            if (dataPoint.getId() != contextKey.getKey())
                rm.addDataPointListener(contextKey.getKey(), this);
        }

        initialized = true;

        initializeTimerTask();
    }

    protected void initializeTimerTask() {
        int updateEventId = vo.getUpdateEvent();
        if (updateEventId != MetaPointLocatorVO.UPDATE_EVENT_CONTEXT_UPDATE)
            // Scheduled update. Create the timeout that will update this point.
            timerTask = new ScheduledUpdateTimeout(calculateTimeout(timer.currentTimeMillis()));
    }

    public void terminate() {
        synchronized (LOCK) {
            // Remove listener registrations
            RuntimeManager rm = Common.ctx.getRuntimeManager();
            for (IntValuePair contextKey : vo.getContext())
                rm.removeDataPointListener(contextKey.getKey(), this);

            // Cancel scheduled job
            if (timerTask != null)
                timerTask.cancel();

            initialized = false;
        }
    }

    //
    // / DataPointListener
    //
    public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
        // No op. Events are covered in pointUpdated.
    }

    public void pointSet(PointValueTime oldValue, PointValueTime newValue) {
        // No op. Events are covered in pointUpdated.
    }

    public void pointUpdated(PointValueTime newValue) {
        // Ignore if this is not a context update.
        if (vo.getUpdateEvent() == MetaPointLocatorVO.UPDATE_EVENT_CONTEXT_UPDATE) {
            // Check for infinite loops
            List<Integer> sourceIds;
            if (threadLocal.get() == null)
                sourceIds = new ArrayList<Integer>();
            else
                sourceIds = threadLocal.get();

            long time = newValue.getTime();
            if (vo.getExecutionDelaySeconds() == 0)
                execute(time, sourceIds);
            else {
                synchronized (LOCK) {
                    if (initialized) {
                        if (timerTask != null)
                            timerTask.cancel();
                        timerTask = new ExecutionDelayTimeout(time, sourceIds);
                    }
                }
            }
        }
    }

    public void pointBackdated(PointValueTime value) {
        // No op.
    }

    public void pointInitialized() {
        createContext();
        dataSource.checkForDisabledPoints();
    }

    public void pointTerminated() {
        createContext();
        dataSource.checkForDisabledPoints();
    }

    //
    //
    // TimeoutClient
    //
    class ScheduledUpdateTimeout extends TimerTask {
        ScheduledUpdateTimeout(long fireTime) {
            super(new OneTimeTrigger(new Date(fireTime)));
            timer.schedule(this);
        }

        @Override
        public void run(long fireTime) {
            // Execute the update
            execute(fireTime - vo.getExecutionDelaySeconds() * 1000, new ArrayList<Integer>());

            // Schedule the next timeout.
            synchronized (LOCK) {
                if (initialized)
                    timerTask = new ScheduledUpdateTimeout(calculateTimeout(fireTime));
            }
        }
    }

    long calculateTimeout(long time) {
        int updateEventId = vo.getUpdateEvent();
        long timeout;
        if (updateEventId == MetaPointLocatorVO.UPDATE_EVENT_CRON) {
            try {
                CronExpression ce = new CronExpression(vo.getUpdateCronPattern());
                timeout = ce.getNextValidTimeAfter(new Date(time)).getTime();
            }
            catch (ParseException e) {
                throw new ShouldNeverHappenException(e);
            }
        }
        else
            timeout = DateUtils.next(time, updateEventId);
        return timeout + vo.getExecutionDelaySeconds() * 1000;
    }

    class ExecutionDelayTimeout extends TimerTask {
        private final long updateTime;
        private final List<Integer> sourceIds;

        public ExecutionDelayTimeout(long updateTime, List<Integer> sourceIds) {
            super(new OneTimeTrigger(new Date(updateTime + vo.getExecutionDelaySeconds() * 1000)));
            this.updateTime = updateTime;
            this.sourceIds = sourceIds;
            timer.schedule(this);
        }

        @Override
        public void run(long fireTime) {
            // When this runs it is because there were no context updates within the delay period. Still, we want the
            // reading to be published with the time of the context update, not the timeout fire time.
            execute(updateTime, sourceIds);
        }
    }

    void execute(long runtime, List<Integer> sourceIds) {
        if (context == null) {
            LOG.warn("MetaPointLocatorRT.context is null, Context: " + generateContext(dataPoint, dataSource));
            return;
        }

        // Check if we've reached the maximum number of recursions for this point
        int count = 0;
        for (Integer id : sourceIds) {
            if (id.intValue() == dataPoint.getId())
                count++;
        }

        if (count > MAX_RECURSION) {
            handleError(runtime, new LocalizableMessage("event.meta.recursionFailure"));
            String msg = MessageFormat.format("Recursion failure: exceeded MAX_RECURSION: expected <= {0} but was {1}, Context: {2}",
                    String.valueOf(MAX_RECURSION), count, generateContext(dataPoint, dataSource));
            LOG.error(msg);
            return;
        }

        sourceIds.add(dataPoint.getId());
        threadLocal.set(sourceIds);
        try {
            ScriptExecutor executor = new ScriptExecutor();
            try {
                PointValueTime pvt = executor.execute(vo.getScript(), context, timer.currentTimeMillis(),
                        vo.getDataTypeId(), runtime);
                if (pvt.getValue() == null)
                    handleError(runtime, new LocalizableMessage("event.meta.nullResult"));
                else
                    updatePoint(pvt);
            }
            catch (ScriptException e) {
                handleError(runtime, new LocalizableMessage("common.default", e.getMessage()));
                LOG.error(infoErrorExecutionScript(e, dataPoint, dataSource));
            }
            catch (ResultTypeException e) {
                handleError(runtime, e.getLocalizableMessage());
                LOG.error(infoErrorExecutionScript(e, dataPoint, dataSource));
            }
            catch (Exception e) {
                LOG.error(infoErrorExecutionScript(e, dataPoint, dataSource));
                throw e;
            }
        }
        finally {
            threadLocal.remove();
        }
    }

    private void createContext() {
        context = null;
        try {
            ScriptExecutor scriptExecutor = new ScriptExecutor();
            context = scriptExecutor.convertContext(vo.getContext());
        } catch (Exception e) {
            LOG.error(infoErrorInitializationScript(e, dataPoint, dataSource));
        }
    }

    protected void updatePoint(PointValueTime pvt) {
        dataPoint.updatePointValue(pvt);
    }

    protected void handleError(long runtime, LocalizableMessage message) {
        dataSource.raiseScriptError(runtime, dataPoint, message);
    }
}
