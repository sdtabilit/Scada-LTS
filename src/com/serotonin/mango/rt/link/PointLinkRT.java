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
package com.serotonin.mango.rt.link;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import com.serotonin.mango.rt.dataImage.DataPointListener;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.PointLinkSetPointSource;
import com.serotonin.mango.rt.maint.work.PointLinkSetPointWorkItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataSource.meta.ResultTypeException;
import com.serotonin.mango.rt.dataSource.meta.ScriptExecutor;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.vo.link.PointLinkVO;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.LocalizableMessage;

import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorExecutionScript;

/**
 * @author Matthew Lohbihler
 */
public class PointLinkRT implements DataPointListener, PointLinkSetPointSource {
	public static final String CONTEXT_VAR_NAME = "source";
	private final PointLinkVO vo;
	private final SystemEventType eventType;
	private Log LOG = LogFactory.getLog(PointLinkRT.class);

	// Added to stop excessive point link calls
	private volatile boolean ready;
	private final Object lock = new Object();


	public PointLinkRT(PointLinkVO vo) {
		this.vo = vo;
		eventType = new SystemEventType(
				SystemEventType.TYPE_POINT_LINK_FAILURE, vo.getId(),
				EventType.DuplicateHandling.IGNORE_SAME_MESSAGE);
		ready = true;
	}

	public void initialize() {
		Common.ctx.getRuntimeManager().addDataPointListener(
				vo.getSourcePointId(), this);
		checkSource();
	}

	public void terminate() {
		Common.ctx.getRuntimeManager().removeDataPointListener(
				vo.getSourcePointId(), this);
		returnToNormal();
	}

	public int getId() {
		return vo.getId();
	}

	private void checkSource() {
		DataPointRT source = Common.ctx.getRuntimeManager().getDataPoint(
				vo.getSourcePointId());
		if (source == null)
			// The source has been terminated, was never enabled, or not longer
			// exists.
			raiseFailureEvent(new LocalizableMessage(
					"event.pointLink.sourceUnavailable"));
		else
			// Everything is good
			returnToNormal();
	}

	private void raiseFailureEvent(LocalizableMessage message) {
		raiseFailureEvent(System.currentTimeMillis(), message);
	}

	private void raiseFailureEvent(long time, LocalizableMessage message) {
		SystemEventType.raiseEvent(eventType, time, true, message);
	}

	private void returnToNormal() {
		SystemEventType.returnToNormal(eventType, System.currentTimeMillis());
	}
	private void execute(PointValueTime newValue) {

		// Bail out if already running a point link operation
		synchronized (lock) {
			if (!ready) {
				LOG.trace("PointLinkRT.ready is set to false.Any of scripts (in meaning source-target) will not work.");
				return;
			}
			else {
				LOG.trace("PointLinkRT.ready will set to false.Scripts (in meaning source-target) will not work.");
				ready = false; // Stop anyone else from using this
			}
		}

		// Propagate the update to the target point. Validate that the target
		// point is available.
		DataPointRT targetPoint = Common.ctx.getRuntimeManager().getDataPoint(
				vo.getTargetPointId());
		if (targetPoint == null) {
			raiseFailureEvent(newValue.getTime(), new LocalizableMessage(
					"event.pointLink.targetUnavailable"));
			return;
		}

		if (!targetPoint.getPointLocator().isSettable()) {
			raiseFailureEvent(newValue.getTime(), new LocalizableMessage(
					"event.pointLink.targetNotSettable"));
			return;
		}

		int targetDataType = targetPoint.getVO().getPointLocator()
				.getDataTypeId();

		if (!StringUtils.isEmpty(vo.getScript())) {
			ScriptExecutor scriptExecutor = new ScriptExecutor();
			Map<String, IDataPoint> context = new HashMap<String, IDataPoint>();
			DataPointRT source = Common.ctx.getRuntimeManager().getDataPoint(
					vo.getSourcePointId());
			context.put(CONTEXT_VAR_NAME, source);

			try {
				PointValueTime pvt = scriptExecutor.execute(vo.getScript(),
						context, newValue.getTime(), targetDataType,
						newValue.getTime());
				if (pvt.getValue() == null) {
					raiseFailureEvent(
							newValue.getTime(),
							new LocalizableMessage("event.pointLink.nullResult"));
					return;
				}
				newValue = pvt;
			} catch (ScriptException e) {
				raiseFailureEvent(newValue.getTime(), new LocalizableMessage(
						"common.default", e.getMessage()));
				LOG.error(infoErrorExecutionScript(e, vo, targetPoint, source));
				return;
			} catch (ResultTypeException e) {
				raiseFailureEvent(newValue.getTime(), e.getLocalizableMessage());
				LOG.error(infoErrorExecutionScript(e, vo, targetPoint, source));
				return;
			} catch (Exception e) {
				LOG.error(infoErrorExecutionScript(e, vo, targetPoint, source));
				throw e;
			}
		}

		if (DataTypes.getDataType(newValue.getValue()) != targetDataType) {
			raiseFailureEvent(newValue.getTime(), new LocalizableMessage(
					"event.pointLink.convertError"));
			return;
		}

		// Queue a work item to perform the update.
		Common.ctx.getBackgroundProcessing().addWorkItem(
				new PointLinkSetPointWorkItem(vo.getTargetPointId(), newValue, this));
		returnToNormal();
	}

	//
	// /
	// / DataPointListener
	// /
	//
	@Override
	public void pointInitialized() {
		checkSource();
	}

	@Override
	public void pointTerminated() {
		checkSource();
	}

	@Override
	public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
		if (vo.getEvent() == PointLinkVO.EVENT_CHANGE)
			execute(newValue);
	}

	@Override
	public void pointSet(PointValueTime oldValue, PointValueTime newValue) {
		// No op
	}

	@Override
	public void pointBackdated(PointValueTime value) {
		// No op
	}

	@Override
	public void pointUpdated(PointValueTime newValue) {
		if (vo.getEvent() == PointLinkVO.EVENT_UPDATE)
			execute(newValue);
	}

	//
	// /
	// / SetPointSource
	// /
	//
	@Override
	public int getSetPointSourceId() {
		return vo.getId();
	}

	@Override
	public int getSetPointSourceType() {
		return Types.POINT_LINK;
	}

	@Override
	public void raiseRecursionFailureEvent() {
		raiseFailureEvent(new LocalizableMessage(
				"event.pointLink.recursionFailure"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.serotonin.m2m2.pointLinks.PointLinkSetPointSource#pointSetComplete()
	 */
	@Override
	public void pointSetComplete() {
		this.ready = true;
		LOG.trace("PointLinkRT.pointSetComplete. Ready property is set to true ");
	}
}
