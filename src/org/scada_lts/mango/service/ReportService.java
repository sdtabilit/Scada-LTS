/*
 * (c) 2016 Abil'I.T. http://abilit.eu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.scada_lts.mango.service;

import com.serotonin.mango.Common;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.type.EventType;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.UserComment;
import com.serotonin.mango.vo.report.*;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.I18NUtils;
import com.serotonin.web.taglib.Functions;
import org.scada_lts.dao.DAO;
import org.scada_lts.dao.report.*;
import org.scada_lts.mango.adapter.MangoReport;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ReportService
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public class ReportService implements MangoReport {

	//	@Autowired
	private ReportDAO reportDAO = new ReportDAO();

	//	@Autowired
	private ReportInstanceDAO reportInstanceDAO = new ReportInstanceDAO();

	//	@Autowired
	private ReportInstanceDataDAO reportInstanceDataDAO = new ReportInstanceDataDAO();

	//	@Autowired
	private ReportInstancePointDAO reportInstancePointDAO = new ReportInstancePointDAO();

	private ReportInstanceUserCommentDAO reportInstanceUserCommentDAO = new ReportInstanceUserCommentDAO();

	private void setReportDataValue(List<ReportPointInfo> pointInfos, final ReportDataStreamHandler handler) {
		final ReportDataValue rdv = new ReportDataValue();
		for (final ReportPointInfo point: pointInfos) {
			handler.startPoint(point);
			rdv.setReportPointId(point.getReportPointId());
			reportInstanceDataDAO.setReportValue(point, rdv, handler);
		}
		handler.done();
	}

	/**
	 * This method guarantees that the data is provided to the setData handler method grouped by point (points are not
	 * ordered), and sorted by time ascending.
	 */
	@Override
	public void reportInstanceData(int instanceId, final ReportDataStreamHandler handler) {
		List<ReportPointInfo> pointInfos = reportInstancePointDAO.getPointInfos(instanceId);
		setReportDataValue(pointInfos, handler);
	}

	@Override
	public List<ReportVO> getReports() {
		return reportDAO.getReports();
	}

	@Override
	public List<ReportVO> getReports(int userId) {
		return reportDAO.getReports(userId);
	}

	@Override
	public ReportVO getReport(int id) {
		return reportDAO.getReport(id);
	}

	@Override
	public void saveReport(ReportVO report) {
		if(report.getDateRangeType() == ReportVO.DATE_RANGE_TYPE_RELATIVE) {
			report.setFromNone(false);
			report.setToNone(false);
		}
		if (report.getId() == Common.NEW_ID) {
			report.setId(reportDAO.insert(report));
		} else {
			reportDAO.update(report);
		}
	}

	@Override
	public void deleteReport(int reportId) {
		reportDAO.delete(reportId);
	}

	@Override
	public List<ReportInstance> getReportInstances(int userId) {
		return reportInstanceDAO.getReportInstances(userId);
	}

	@Override
	public ReportInstance getReportInstance(int id) {
		return reportInstanceDAO.getReportInstance(id);
	}

	@Override
	public void deleteReportInstance(int id, int userId) {
		reportInstanceDAO.delete(id, userId);
	}

	@Override
	public int purgeReportsBefore(long time) {
		return reportInstanceDAO.deleteReportBefore(time);
	}

	@Override
	public void setReportInstancePreventPurge(int id, boolean preventPurge, int userId) {
		reportInstanceDAO.updatePreventPurge(id, preventPurge, userId);
	}

	/**
	 * This method should only be called by the ReportWorkItem.
	 */
	public void saveReportInstance(ReportInstance reportInstance) {
		if (reportInstance.getId() == Common.NEW_ID) {
			reportInstance.setId(reportInstanceDAO.insert(reportInstance));
		} else {
			reportInstanceDAO.updateTime(reportInstance);
		}
	}

	@Override
	public int runReport(final ReportInstance instance, List<ReportInstancePointDAO.PointInfo> points, ResourceBundle bundle) {
		PointValueService pointValueService = new PointValueService();
		int count = 0;

		//TODO SeroUtils
		String userLabel = I18NUtils.getMessage(bundle, "common.user");
		String setPointLabel = I18NUtils.getMessage(bundle, "annotation.eventHandler");
		String anonymousLabel = I18NUtils.getMessage(bundle, "annotation.anonymous");
		String deletedLabel = I18NUtils.getMessage(bundle, "common.deleted");

		// The timestamp selection code is used multiple times for different tables
		String timestampSql;
		Object[] timestampParams;
		if (instance.isFromInception() && instance.isToNow()) {
			timestampSql = "";
			timestampParams = new Object[0];
		}
		else if (instance.isFromInception()) {
			timestampSql = "and ${field}<?";
			timestampParams = new Object[] { instance.getReportEndTime() };
		}
		else if (instance.isToNow()) {
			timestampSql = "and ${field}>=?";
			timestampParams = new Object[] { instance.getReportStartTime() };
		}
		else {
			timestampSql = "and ${field}>=? and ${field}<?";
			timestampParams = new Object[] { instance.getReportStartTime(), instance.getReportEndTime() };
		}

		// For each point.
		for (ReportInstancePointDAO.PointInfo pointInfo : points) {
			DataPointVO point = pointInfo.getPoint();
			int dataType = point.getPointLocator().getDataTypeId();

			MangoValue startValue = null;
			if (!instance.isFromInception()) {
				// Get the value just before the start of the report
				PointValueTime pvt = pointValueService.getPointValueBefore(point.getId(), instance.getReportStartTime());
				if (pvt != null)
					startValue = pvt.getValue();

				// Make sure the data types match
				if (DataTypes.getDataType(startValue) != dataType)
					startValue = null;
			}

			// Insert the reportInstancePoints record
			//TODO SeroUtils
			String name = Functions.truncate(point.getName(), 100);

			int reportPointId = reportInstancePointDAO.insert(instance, point, name, dataType, startValue, pointInfo);
			count += reportInstanceDataDAO.insert(appendParameters(timestampParams, point.getId(), dataType), reportPointId, StringUtils.replaceMacro(timestampSql, "field", "ts"));

			String annotationCase = "    case pva.sourceType" //
					+ "      when 1 then concat('" + userLabel + ": ',ifnull(u.username,'" + deletedLabel + "')) " //
					+ "      when 2 then '" + setPointLabel + "'" //
					+ "      when 3 then '" + anonymousLabel + "'" //
					+ "      else concat('Unknown source type: ', pva.sourceType)" //
					+ "    end ";

			// Insert the reportInstanceDataAnnotations records
			reportInstanceDataDAO.insertReportInstanceDataAnnotations(annotationCase, reportPointId);

			//TODO
			// Insert the reportInstanceEvents records for the point.
			if (instance.getIncludeEvents() != ReportVO.EVENTS_NONE) {
				String eventSQL = "insert into reportInstanceEvents " //
						+ "  (eventId, reportInstanceId, typeId, typeRef1, typeRef2, activeTs, rtnApplicable, rtnTs," //
						+ "   rtnCause, alarmLevel, message, ackTs, ackUsername, alternateAckSource)" //
						+ "  select e.id, " + instance.getId() + ", e.typeId, e.typeRef1, e.typeRef2, e.activeTs, " //
						+ "    e.rtnApplicable, e.rtnTs, e.rtnCause, e.alarmLevel, e.message, e.ackTs, u.username, " //
						+ "    e.alternateAckSource " //
						+ "  from events e join userEvents ue on ue.eventId=e.id " //
						+ "    left join users u on e.ackUserId=u.id " //
						+ "  where ue.userId=? " //
						+ "    and e.typeId=" //
						+ EventType.EventSources.DATA_POINT //
						+ "    and e.typeRef1=? ";

				if (instance.getIncludeEvents() == ReportVO.EVENTS_ALARMS) {
					eventSQL += "and e.alarmLevel > 0 ";
				}

				eventSQL += StringUtils.replaceMacro(timestampSql, "field", "e.activeTs");
				DAO.getInstance().getJdbcTemp().update(eventSQL, appendParameters(timestampParams, instance.getUserId(), point.getId()));
			}

			// Insert the reportInstanceUserComments records for the point.
			if (instance.isIncludeUserComments()) {
				String commentSQL = "insert into reportInstanceUserComments " //
						+ "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
						+ "  select " + instance.getId() + ", u.username, " + UserComment.TYPE_POINT + ", " //
						+ reportPointId + ", uc.ts, uc.commentText " //
						+ "  from userComments uc " //
						+ "    left join users u on uc.userId=u.id " //
						+ "  where uc.commentType=" + UserComment.TYPE_POINT //
						+ "    and uc.typeKey=? ";

				// Only include comments made in the duration of the report.
				commentSQL += StringUtils.replaceMacro(timestampSql, "field", "uc.ts");
				DAO.getInstance().getJdbcTemp().update(commentSQL, appendParameters(timestampParams, point.getId()));
			}
		}

		// Insert the reportInstanceUserComments records for the selected events
		if (instance.isIncludeUserComments()) {
			String commentSQL = "insert into reportInstanceUserComments " //
					+ "  (reportInstanceId, username, commentType, typeKey, ts, commentText)" //
					+ "  select " + instance.getId() + ", u.username, " + UserComment.TYPE_EVENT + ", uc.typeKey, " //
					+ "    uc.ts, uc.commentText " //
					+ "  from userComments uc " //
					+ "    left join users u on uc.userId=u.id " //
					+ "    join reportInstanceEvents re on re.eventId=uc.typeKey " //
					+ "  where uc.commentType=" + UserComment.TYPE_EVENT //
					+ "    and re.reportInstanceId=? ";
			DAO.getInstance().getJdbcTemp().update(commentSQL, new Object[] { instance.getId() });
		}

		// If the report had undefined start or end times, update them with values from the data.
		if (instance.isFromInception() || instance.isToNow()) {
			setReportTime(instance);
		}
		return count;
	}

	private void setReportTime(final ReportInstance reportInstance) {

		DAO.getInstance().getJdbcTemp().query(ReportInstanceDataDAO.REPORT_INSTANCE_POINT_SELECT_MIN_MAX, new Object[]{reportInstance.getId()}, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				if (reportInstance.isFromInception()) {
					reportInstance.setReportStartTime(rs.getLong(ReportInstanceDataDAO.COLUMN_NAME_D_TS_MIN));
				}
				if (reportInstance.isToNow()) {
					reportInstance.setReportEndTime(rs.getLong(ReportInstanceDataDAO.COLUMN_NAME_D_TS_MAX));
				}
			}
		});

	}

	@Override
	public List<EventInstance> getReportInstanceEvents(int instanceId) {

		final List<EventInstance> events = reportInstanceDAO.getReportInstanceEvents(instanceId);
		addCommentsToEvent(events, instanceId);

		return events;
	}

	@Override
	public List<ReportUserComment> getReportInstanceUserComments(int instanceId) {
		return reportInstanceUserCommentDAO.getReportUserComments(instanceId);
	}

	//TODO
	private void addCommentsToEvent(final List<EventInstance> events, final int instanceId) {
		DAO.getInstance().getJdbcTemp().query(ReportInstanceUserCommentDAO.REPORT_USER_COMMENT_SELECT_WHERE, new Object[]{
				instanceId,
				UserComment.TYPE_EVENT
		}, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				UserComment userComment = new UserComment();
				userComment.setUsername(rs.getString(ReportInstanceUserCommentDAO.COLUMN_NAME_USERNAME));
				userComment.setTs(rs.getLong(ReportInstanceUserCommentDAO.COLUMN_NAME_TS));
				userComment.setComment(rs.getString(ReportInstanceUserCommentDAO.COLUMN_NAME_COMMENT_TEXT));

				// Find the event and add the comment
				int eventId = rs.getInt(ReportInstanceUserCommentDAO.COLUMN_NAME_COMMENT_TYPE);
				for (EventInstance event : events) {
					if (event.getId() == eventId) {
						if (event.getEventComments() == null) {
							event.setEventComments(new ArrayList<UserComment>());
						}
						event.addEventComment(userComment);
					}
				}
			}
		});
	}

	private Object[] appendParameters(Object[] toAppend, Object... params) {
		if (toAppend.length == 0) {
			return params;
		}
		if (params.length == 0) {
			return toAppend;
		}

		Object[] result = new Object[params.length + toAppend.length];
		System.arraycopy(params, 0, result, 0, params.length);
		System.arraycopy(toAppend, 0, result, params.length, toAppend.length);
		return result;
	}
}
