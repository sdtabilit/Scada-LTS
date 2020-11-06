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
package org.scada_lts.dao.report;

import com.mysql.jdbc.Statement;
import com.serotonin.mango.DataTypes;
import com.serotonin.mango.rt.dataImage.types.*;
import com.serotonin.mango.vo.report.ReportDataStreamHandler;
import com.serotonin.mango.vo.report.ReportDataValue;
import com.serotonin.mango.vo.report.ReportPointInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.DAO;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ReportInstanceData and ReportInstanceDataAnnotation DAO
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public class ReportInstanceDataDAO {

	private static final Log LOG = LogFactory.getLog(ReportInstanceDataDAO.class);

	private static final String	COLUMN_NAME_ID = "id";

	public static final String COLUMN_NAME_D_POINT_VALUE = "pointValue";
	public static final String COLUMN_NAME_D_TS = "ts";
	public static final String COLUMN_NAME_D_TS_MIN = "tsMin";
	public static final String COLUMN_NAME_D_TS_MAX = "tsMax";
	public static final String COLUMN_NAME_D_POINT_VALUE_ID = "pointValueId";
	public static final String COLUMN_NAME_D_REPORT_INSTANCE_POINT_ID = "reportInstancePointId";

	public static final String COLUMN_NAME_A_TEXT_POINT_VALUE_SHORT = "textPointValueShort";
	public static final String COLUMN_NAME_A_TEXT_POINT_VALUE_LONG = "textPointValueLong";
	public static final String COLUMN_NAME_A_SOURCE_VALUE = "sourceValue";
	public static final String COLUMN_NAME_A_SOURCE_TYPE = "sourceType";
	public static final String COLUMN_NAME_A_SOURCE_ID = "sourceId";
	public static final String COLUMN_NAME_A_POINT_VALUE_ID = "pointValueId";
	public static final String COLUMN_NAME_A_REPORT_INSTANCE_POINT_ID = "reportInstancePointId";

	private static final String COLUMN_NAME_DATA_POINT_ID = "dataPointId";
	private static final String COLUMN_NAME_DATA_TYPE = "dataType";
	private static final String COLUMN_NAME_REPORT_INSTANCE_ID = "reportInstanceId";

	// @formatter:off
	public static final String REPORT_INSTANCE_DATA_INSERT_FIRST = ""
			+ "insert into reportInstanceData "
			+ "select "
				+ COLUMN_NAME_ID + ", ";


	public static final String REPORT_INSTANCE_DATA_INSERT_SECOND = ""
			+ ", "
				+ COLUMN_NAME_D_POINT_VALUE + ", "
				+ COLUMN_NAME_D_TS + " "
			+ "from pointValues where "
				+ COLUMN_NAME_DATA_POINT_ID + "=? "
			+ "and "
				+ COLUMN_NAME_DATA_TYPE + "=? ";

	private static final String REPORT_INSTANCE_DATA_SELECT = ""
			+ "select "
				+ "rd." + COLUMN_NAME_D_POINT_VALUE + ", "
				+ "rda." + COLUMN_NAME_A_TEXT_POINT_VALUE_SHORT + ", "
				+ "rda." + COLUMN_NAME_A_TEXT_POINT_VALUE_LONG + ", "
				+ "rd." + COLUMN_NAME_D_TS + ", "
				+ "rda." + COLUMN_NAME_A_SOURCE_VALUE + " "
			+ "from reportInstanceData rd left join reportInstanceDataAnnotations rda on "
				+ "rd." + COLUMN_NAME_D_POINT_VALUE_ID + "="
				+ "rda." + COLUMN_NAME_A_POINT_VALUE_ID + " "
			+ "and "
				+ "rd." + COLUMN_NAME_D_REPORT_INSTANCE_POINT_ID + "="
				+ "rda." + COLUMN_NAME_A_REPORT_INSTANCE_POINT_ID + " ";

	public static final String REPORT_INSTANCE_DATA_SELECT_WHERE = ""
				+ REPORT_INSTANCE_DATA_SELECT
			+ "where "
				+ "rd." + COLUMN_NAME_D_REPORT_INSTANCE_POINT_ID + "=? "
			+ "order by "
				+ "rd." + COLUMN_NAME_D_TS + " ";

	public static final String REPORT_INSTANCE_POINT_SELECT_MIN_MAX = ""
			+ "select min("
				+ "rd." + COLUMN_NAME_D_TS + ") as " + COLUMN_NAME_D_TS_MIN + ", "
			+ "max("
				+ "rd." + COLUMN_NAME_D_TS + ") as " + COLUMN_NAME_D_TS_MAX + " "
			+ "from reportInstancePoints rp "
			+ "join reportInstanceData rd "
			+ "on "
				+ "rp." + COLUMN_NAME_ID + "="
				+ "rd." + COLUMN_NAME_A_REPORT_INSTANCE_POINT_ID + " "
			+ "where "
				+ "rp." + COLUMN_NAME_REPORT_INSTANCE_ID + "=? ";

	private static final String REPORT_INSTANCE_DATA_ANNOTATION_INSERT_FIRST = ""
			+ "insert into reportInstanceDataAnnotations ("
				+ COLUMN_NAME_A_POINT_VALUE_ID + ", "
				+ COLUMN_NAME_A_REPORT_INSTANCE_POINT_ID + ", "
				+ COLUMN_NAME_A_TEXT_POINT_VALUE_SHORT + ", "
				+ COLUMN_NAME_A_TEXT_POINT_VALUE_LONG + ", "
				+ COLUMN_NAME_A_SOURCE_VALUE + ") "
			+ "select "
				+ "rd." + COLUMN_NAME_D_POINT_VALUE_ID + ", "
				+ "rd." + COLUMN_NAME_D_REPORT_INSTANCE_POINT_ID + ", "
				+ "pva." + COLUMN_NAME_A_TEXT_POINT_VALUE_SHORT + ", "
				+ "pva." + COLUMN_NAME_A_TEXT_POINT_VALUE_LONG + ", ";


	private static final String REPORT_INSTANCE_DATA_ANNOTATION_INSERT_SECOND = ""
			+ "from reportInstanceData rd "
			+ "join reportInstancePoints rp on "
				+ "rd." + COLUMN_NAME_D_REPORT_INSTANCE_POINT_ID + "="
				+ "rp." + COLUMN_NAME_ID + " "
			+ "join pointValueAnnotations pva on "
				+ "rd." + COLUMN_NAME_D_POINT_VALUE_ID + "="
				+ "pva." + COLUMN_NAME_A_POINT_VALUE_ID + " "
			+ "left join users u on "
				+ "pva." + COLUMN_NAME_A_SOURCE_TYPE + "=1 "
			+ "and "
				+ "pva." + COLUMN_NAME_A_SOURCE_ID + "="
				+ "u." + COLUMN_NAME_ID + " "
			+ "where "
				+ "rp." + COLUMN_NAME_ID + "=?";
	// @formatter:on

	@Transactional(readOnly = false,propagation= Propagation.REQUIRES_NEW,isolation= Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public int insert(final Object[] params, final int reportPointId, final String timestampSql) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("insertInstanceData(Object[] params, final int reportPointId, final String timestampSql) reportPointId:" + reportPointId + ", timestampSql:" + timestampSql);
		}

		DAO.getInstance().getJdbcTemp().update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement preparedStatement = connection.prepareStatement(REPORT_INSTANCE_DATA_INSERT_FIRST + reportPointId + REPORT_INSTANCE_DATA_INSERT_SECOND + timestampSql);
				new ArgumentPreparedStatementSetter(params).setValues(preparedStatement);
				return preparedStatement;
			}
		});

		return reportPointId;
	}

	@Transactional(readOnly = false,propagation= Propagation.REQUIRES_NEW,isolation= Isolation.READ_COMMITTED,rollbackFor=SQLException.class)
	public int insertReportInstanceDataAnnotations(String annotationCase, final int reportPointId) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("insertReportInstanceDataAnnotations(String annotationCase, final int reportPointId) annotationCase:" + annotationCase + ", reportPointId:" + reportPointId);
		}

		final String template = REPORT_INSTANCE_DATA_ANNOTATION_INSERT_FIRST + annotationCase + REPORT_INSTANCE_DATA_ANNOTATION_INSERT_SECOND;

		KeyHolder keyHolder = new GeneratedKeyHolder();
		DAO.getInstance().getJdbcTemp().update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement preparedStatement = connection.prepareStatement(template, Statement.RETURN_GENERATED_KEYS);
				new ArgumentPreparedStatementSetter(new Object[]{reportPointId}).setValues(preparedStatement);
				return preparedStatement;
			}
		}, keyHolder);

		return reportPointId;
	}

	public void setReportValue(final ReportPointInfo point, final ReportDataValue rdv, final ReportDataStreamHandler handler) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("setReportValue(ReportPointInfo point, ReportDataValue rdv, final ReportDataStreamHandler handler) point:" + point.toString() + ", rdv:" + rdv.toString() + ", handler:" + handler.toString());
		}

		final int dataType = point.getDataType();
		DAO.getInstance().getJdbcTemp().query(REPORT_INSTANCE_DATA_SELECT_WHERE, new Object[]{point.getReportPointId()}, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				switch (dataType) {
					case (DataTypes.NUMERIC):
						rdv.setValue(new NumericValue(rs.getDouble(COLUMN_NAME_D_POINT_VALUE)));
						break;
					case (DataTypes.BINARY):
						rdv.setValue(new BinaryValue(rs.getDouble(COLUMN_NAME_D_POINT_VALUE) == 1));
						break;
					case (DataTypes.MULTISTATE):
						rdv.setValue(new MultistateValue(rs.getInt(COLUMN_NAME_D_POINT_VALUE)));
						break;
					case (DataTypes.ALPHANUMERIC):
						rdv.setValue(new AlphanumericValue(rs.getString(COLUMN_NAME_A_TEXT_POINT_VALUE_SHORT)));
						if (rs.wasNull())
							rdv.setValue(new AlphanumericValue(rs.getString(COLUMN_NAME_A_TEXT_POINT_VALUE_LONG)));
						break;
					case (DataTypes.IMAGE):
						rdv.setValue(new ImageValue(Integer.parseInt(rs.getString(COLUMN_NAME_A_TEXT_POINT_VALUE_SHORT)), rs.getInt(COLUMN_NAME_D_POINT_VALUE)));
						break;
					default:
						rdv.setValue(null);
				}
				rdv.setTime(rs.getLong(ReportInstanceDataDAO.COLUMN_NAME_D_TS));
				rdv.setAnnotation(rs.getString(ReportInstanceDataDAO.COLUMN_NAME_A_SOURCE_VALUE));
				handler.pointData(rdv);
			}
		});
	}
}
