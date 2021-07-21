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
package com.serotonin.mango.web.dwr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import com.serotonin.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.PointLinkDao;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataSource.meta.ResultTypeException;
import com.serotonin.mango.rt.dataSource.meta.ScriptExecutor;
import com.serotonin.mango.rt.link.PointLinkRT;
import com.serotonin.mango.util.LoggingScriptUtils;
import com.serotonin.mango.vo.DataPointExtendedNameComparator;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.link.PointLinkVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.web.taglib.DateFunctions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.serotonin.mango.util.LoggingScriptUtils.infoErrorExecutionScript;

/**
 * @author Matthew Lohbihler
 */
public class PointLinksDwr extends BaseDwr {
    private static final Log LOG = LogFactory.getLog(PointLinksDwr.class);
    public Map<String, Object> init() {
        User user = Common.getUser();
        Map<String, Object> data = new HashMap<String, Object>();

        // Get the points that this user can access.
        List<DataPointVO> allPoints = new DataPointDao().getDataPoints(DataPointExtendedNameComparator.instance, false);
        List<IntValuePair> sourcePoints = new ArrayList<IntValuePair>();
        List<IntValuePair> targetPoints = new ArrayList<IntValuePair>();
        for (DataPointVO point : allPoints) {
            if (Permissions.hasDataPointReadPermission(user, point))
                sourcePoints.add(new IntValuePair(point.getId(), point.getExtendedName()));
            if (point.getPointLocator().isSettable() && Permissions.hasDataPointSetPermission(user, point))
                targetPoints.add(new IntValuePair(point.getId(), point.getExtendedName()));
        }

        data.put("sourcePoints", sourcePoints);
        data.put("targetPoints", targetPoints);

        // Get the existing point links.
        List<PointLinkVO> pointLinks = new ArrayList<PointLinkVO>();
        for (PointLinkVO pointLink : new PointLinkDao().getPointLinks()) {
            if (containsPoint(sourcePoints, pointLink.getSourcePointId())
                    && containsPoint(targetPoints, pointLink.getTargetPointId()))
                pointLinks.add(pointLink);
        }

        data.put("pointLinks", pointLinks);

        return data;
    }

    private boolean containsPoint(List<IntValuePair> pointList, int pointId) {
        for (IntValuePair ivp : pointList) {
            if (ivp.getKey() == pointId)
                return true;
        }
        return false;
    }

    public PointLinkVO getPointLink(int id) {
        PointLinkVO vo;
        PointLinkDao pointLinkDao = new PointLinkDao();
        if (id == Common.NEW_ID) {
            vo = new PointLinkVO();
            vo.setXid(pointLinkDao.generateUniqueXid());
        }
        else
            vo = pointLinkDao.getPointLink(id);
        return vo;
    }

    public DwrResponseI18n savePointLink(int id, String xid, int sourcePointId, int targetPointId, String script,
            int event, boolean disabled) {
        // Validate the given information. If there is a problem, return an appropriate error message.
        PointLinkVO vo = new PointLinkVO();
        vo.setId(id);
        vo.setXid(xid);
        vo.setSourcePointId(sourcePointId);
        vo.setTargetPointId(targetPointId);
        vo.setScript(script);
        vo.setEvent(event);
        vo.setDisabled(disabled);

        DwrResponseI18n response = new DwrResponseI18n();
        PointLinkDao pointLinkDao = new PointLinkDao();

        if (StringUtils.isEmpty(xid))
            response.addContextualMessage("xid", "validate.required");
        else if (!pointLinkDao.isXidUnique(xid, id))
            response.addContextualMessage("xid", "validate.xidUsed");

        vo.validate(response);

        // Save it
        if (!response.getHasMessages())
            Common.ctx.getRuntimeManager().savePointLink(vo);

        response.addData("plId", vo.getId());

        return response;
    }

    public void deletePointLink(int id) {
        Common.ctx.getRuntimeManager().deletePointLink(id);
    }

    public DwrResponseI18n validateScript(String script, int sourcePointId, int targetPointId) {
        DwrResponseI18n response = new DwrResponseI18n();
        LocalizableMessage message;
        ScriptExecutor scriptExecutor = new ScriptExecutor();

        DataPointRT point = Common.ctx.getRuntimeManager().getDataPoint(sourcePointId);
        if (point == null)
            message = new LocalizableMessage("event.pointLink.sourceUnavailable");
        else {
            Map<String, IDataPoint> context = new HashMap<String, IDataPoint>();
            context.put(PointLinkRT.CONTEXT_VAR_NAME, point);
            int targetDataType = new DataPointDao().getDataPoint(targetPointId).getPointLocator().getDataTypeId();

            try {
                PointValueTime pvt = scriptExecutor.execute(script, context, System.currentTimeMillis(),
                        targetDataType, -1);
                if (pvt.getValue() == null)
                    message = new LocalizableMessage("event.pointLink.nullResult");
                else if (pvt.getTime() == -1)
                    message = new LocalizableMessage("pointLinks.validate.success", pvt.getValue());
                else
                    message = new LocalizableMessage("pointLinks.validate.successTs", pvt.getValue(),
                            DateFunctions.getTime(pvt.getTime()));
            }
            catch (ScriptException e) {
                message = new LocalizableMessage("common.default", e.getMessage());
            }
            catch (ResultTypeException e) {
                message = e.getLocalizableMessage();
            }
            catch (Exception e) {
                LOG.error(infoErrorExecutionScript(e, "validateScript"));
                throw e;
            }
        }

        response.addMessage("script", message);
        return response;
    }
}
