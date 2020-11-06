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
package org.scada_lts.dao;

import com.serotonin.InvalidArgumentException;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.mango.Common;
import org.scada_lts.utils.ColorUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SystemSettings DAO
 *
 * @author Mateusz Kaproń Abil'I.T. development team, sdt@abilit.eu
 */
public class SystemSettingsDAO {

	// Database schema version
	public static final String DATABASE_SCHEMA_VERSION = "databaseSchemaVersion";

	// Servlet context name
	public static final String SERVLET_CONTEXT_PATH = "servletContextPath";

	// Email settings
	public static final String EMAIL_SMTP_HOST = "emailSmtpHost";
	public static final String EMAIL_SMTP_PORT = "emailSmtpPort";
	public static final String EMAIL_FROM_ADDRESS = "emailFromAddress";
	public static final String EMAIL_FROM_NAME = "emailFromName";
	public static final String EMAIL_AUTHORIZATION = "emailAuthorization";
	public static final String EMAIL_SMTP_USERNAME = "emailSmtpUsername";
	public static final String EMAIL_SMTP_PASSWORD = "emailSmtpPassword";
	public static final String EMAIL_TLS = "emailTls";
	public static final String EMAIL_CONTENT_TYPE = "emailContentType";

	// Event purging
	public static final String EVENT_PURGE_PERIOD_TYPE = "eventPurgePeriodType";
	public static final String EVENT_PURGE_PERIODS = "eventPurgePeriods";

	// Report purging
	public static final String REPORT_PURGE_PERIOD_TYPE = "reportPurgePeriodType";
	public static final String REPORT_PURGE_PERIODS = "reportPurgePeriods";

	// HTTP Client configuration
	public static final String HTTP_CLIENT_USE_PROXY = "httpClientUseProxy";
	public static final String HTTP_CLIENT_PROXY_SERVER = "httpClientProxyServer";
	public static final String HTTP_CLIENT_PROXY_PORT = "httpClientProxyPort";
	public static final String HTTP_CLIENT_PROXY_USERNAME = "httpClientProxyUsername";
	public static final String HTTP_CLIENT_PROXY_PASSWORD = "httpClientProxyPassword";

	// New Mango version
	public static final String NEW_VERSION_NOTIFICATION_LEVEL = "newVersionNotificationLevel";
	public static final String NOTIFICATION_LEVEL_STABLE = "S";
	public static final String NOTIFICATION_LEVEL_RC = "C";
	public static final String NOTIFICATION_LEVEL_BETA = "B";

	// i18n
	public static final String LANGUAGE = "language";

	// Customization
	public static final String FILEDATA_PATH = "filedata.path";
	public static final String DATASOURCE_DISPLAY_SUFFIX = ".display";
	public static final String HTTPDS_PROLOGUE = "httpdsPrologue";
	public static final String HTTPDS_EPILOGUE = "httpdsEpilogue";
	public static final String UI_PERFORMANCE = "uiPerformance";
	public static final String GROVE_LOGGING = "groveLogging";
	public static final String FUTURE_DATE_LIMIT_PERIODS = "futureDateLimitPeriods";
	public static final String FUTURE_DATE_LIMIT_PERIOD_TYPE = "futureDateLimitPeriodType";
	public static final String INSTANCE_DESCRIPTION = "instanceDescription";

	// Colours
	public static final String CHART_BACKGROUND_COLOUR = "chartBackgroundColour";
	public static final String PLOT_BACKGROUND_COLOUR = "plotBackgroundColour";
	public static final String PLOT_GRIDLINE_COLOUR = "plotGridlineColour";

	private static final String COLUMN_NAME_SETTING_VALUE = "settingValue";
	private static final String COLUMN_NAME_SETTINGS_NAME = "settingName";

	// Database
	public static final String DATABASE_INFO_SCHEMA_VERSION = "version";

	private static final String DELETE_WATCH_LISTS = "delete from watchLists";
	private static final String DELETE_MANGO_VIEWS = "delete from mangoViews";
	private static final String DELETE_POINT_EVENT_DETECTORS = "delete from pointEventDetectors";
	private static final String DELETE_COMPOUND_EVENT_DETECTORS = "delete from compoundEventDetectors";
	private static final String DELETE_SCHEDULED_EVENTS = "delete from scheduledEvents";
	private static final String DELETE_POINT_LINKS = "delete from pointLinks";
	private static final String DELETE_EVENTS = "delete from events";
	private static final String DELETE_REPORTS = "delete from reports";
	private static final String DELETE_POINT_HIERARCHY = "delete from pointHierarchy";
	private static final String DELETE_EVENT_HANDLERS = "delete from eventHandlers";
	private static final String DELETE_SCRIPTS = "delete from scripts";
	private static final String DELETE_POINT_VALUES = "delete from pointValues";
	private static final String DELETE_MAINTENANCE_EVENTS = "delete from maintenanceEvents";
	private static final String DELETE_MAILING_LISTS = "delete from mailingLists";
	private static final String DELETE_USERS = "delete from users";
	private static final String DELETE_PUBLISHERS = "delete from publishers";
	private static final String DELETE_DATA_POINT_USERS = "delete from dataPointUsers";
	private static final String DELETE_DATA_SOURCE_USERS = "delete from dataSourceUsers";
	private static final String DELETE_DATA_POINTS = "delete from dataPoints";
	private static final String DELETE_DATA_SOURCES = "delete from dataSources";

	// @formatter:off
	private static final String SELECT_SETTING_VALUE_WHERE = ""
			+ "select "
			+ COLUMN_NAME_SETTING_VALUE + " "
			+ "from systemSettings where "
			+ COLUMN_NAME_SETTINGS_NAME + "=? ";

	private static final String INSERT_SYSTEM_SETTING = ""
			+ "insert into systemSettings values (?,?) ";

	private static final String DELETE_SYSTEM_SETTINGS_WHERE = ""
			+ "delete from systemSettings where "
			+ COLUMN_NAME_SETTINGS_NAME + "=? ";

	private static final String DATABASE_SIZE = ""
			+ "select sum(data_length + index_length) /1024 /1024 \"size\" "
			+ "from information_schema.TABLES where table_schema=";

	private static final String DATABASE_STATEMENT = "DATABASE()";
	private static final String SELECT_DATABASE = ""
			+ "select "
			+ DATABASE_STATEMENT + ";";

	private static final String SELECT_LATEST_SCHEMA_VERSION = ""
			+ "SELECT version FROM schema_version ORDER BY version DESC LIMIT 1";
	// @formatter:on

	// Value cache
	private static final Map<String, String> cache = new HashMap<String, String>();

	public static String getValue(String key) {
		return getValue(key, (String) DEFAULT_VALUES.get(key));
	}

	public static String getValue(String key, String defaultValue) {
		String result = cache.get(key);
		if (result == null) {
			if (!cache.containsKey(key)) {
				try {
					result = DAO.getInstance().getJdbcTemp().queryForObject(SELECT_SETTING_VALUE_WHERE, new Object[]{key}, String.class);
				} catch (EmptyResultDataAccessException e) {
					result = null;
				}
				cache.put(key, result);
				if (result == null) {
					result = defaultValue;
				}
			} else {
				result = defaultValue;
			}
		}
		return result;
	}

	public static int getIntValue(String key) {
		Integer defaultValue = (Integer) DEFAULT_VALUES.get(key);
		if (defaultValue == null)
			return getIntValue(key, 0);
		return getIntValue(key, defaultValue);
	}

	public static int getIntValue(String key, int defaultValue) {
		String value = getValue(key, null);
		if (value == null)
			return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	public static boolean getBooleanValue(String key, boolean defaultValue) {
		String value = getValue(key, null);
		if (value == null)
			return defaultValue;
		return DAO.charToBool(value);
	}

	public void setValue(final String key, final String value) {
		// Update the cache
		cache.put(key, value);

		// Update the database
		updateDatabase(key, value);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
	private void updateDatabase(final String key, final String value) {
		// Delete any existing value.
		removeValue(key);

		// Insert the new value if it's not null.
		if (value != null) {
			DAO.getInstance().getJdbcTemp().update(INSERT_SYSTEM_SETTING, new Object[]{key, value});
		}
	}

	public void setIntValue(String key, int value) {
		setValue(key, Integer.toString(value));
	}

	public void setBooleanValue(String key, boolean value) {
		setValue(key, DAO.boolToChar(value));
	}

	public void removeValue(String key) {
		// Remove the value from the cache
		cache.remove(key);

		// Reset the cached values too.
		FUTURE_DATE_LIMIT = -1;

		DAO.getInstance().getJdbcTemp().update(DELETE_SYSTEM_SETTINGS_WHERE, new Object[]{key});
	}

	public static long getFutureDateLimit() {
		if (FUTURE_DATE_LIMIT == -1) {
			FUTURE_DATE_LIMIT = Common.getMillis(
					getIntValue(FUTURE_DATE_LIMIT_PERIOD_TYPE),
					getIntValue(FUTURE_DATE_LIMIT_PERIODS));
		}
		return FUTURE_DATE_LIMIT;
	}

	public static Color getColour(String key) {
		try {
			return ColorUtils.toColor(getValue(key));
		} catch (InvalidArgumentException e) {
			// Should never happen. Just use the default.
			try {
				return ColorUtils.toColor((String) DEFAULT_VALUES.get(key));
			} catch (InvalidArgumentException e1) {
				// This should definitely never happen
				throw new ShouldNeverHappenException(e1);
			}
		}
	}

	public String getDatabaseSchemaVersion(String key, String defaultValue) {
		String result = cache.get(key);
		if (result == null) {
			if (!cache.containsKey(key)) {
				try {
					result = DAO.getInstance().getJdbcTemp().queryForObject(SELECT_LATEST_SCHEMA_VERSION, String.class);
				} catch (EmptyResultDataAccessException e) {
					result = null;
				}
				cache.put(key, result);
				if (result == null) {
					result = defaultValue;
				}
			} else {
				result = defaultValue;
			}
		}
		return result;
	}

	/**
	 * Special caching for the future dated values property, which needs high
	 * performance.
	 */
	private static long FUTURE_DATE_LIMIT = -1;

	public static final Map<String, Object> DEFAULT_VALUES = new HashMap<String, Object>();

	static {
		DEFAULT_VALUES.put(DATABASE_SCHEMA_VERSION, "0.7.0");

		DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_SERVER, "");
		DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_PORT, -1);
		DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_USERNAME, "");
		DEFAULT_VALUES.put(HTTP_CLIENT_PROXY_PASSWORD, "");

		DEFAULT_VALUES.put(EMAIL_SMTP_HOST, "");
		DEFAULT_VALUES.put(EMAIL_SMTP_PORT, 25);
		DEFAULT_VALUES.put(EMAIL_FROM_ADDRESS, "");
		DEFAULT_VALUES.put(EMAIL_SMTP_USERNAME, "");
		DEFAULT_VALUES.put(EMAIL_SMTP_PASSWORD, "");
		DEFAULT_VALUES.put(EMAIL_FROM_NAME, "Scada-LTS");

		DEFAULT_VALUES.put(EVENT_PURGE_PERIOD_TYPE, Common.TimePeriods.YEARS);
		DEFAULT_VALUES.put(EVENT_PURGE_PERIODS, 1);

		DEFAULT_VALUES.put(REPORT_PURGE_PERIOD_TYPE, Common.TimePeriods.MONTHS);
		DEFAULT_VALUES.put(REPORT_PURGE_PERIODS, 1);

		DEFAULT_VALUES.put(NEW_VERSION_NOTIFICATION_LEVEL,
				NOTIFICATION_LEVEL_STABLE);

		DEFAULT_VALUES.put(LANGUAGE, "en");

		DEFAULT_VALUES.put(FILEDATA_PATH, "~/WEB-INF/filedata");
		DEFAULT_VALUES.put(HTTPDS_PROLOGUE, "");
		DEFAULT_VALUES.put(HTTPDS_EPILOGUE, "");
		DEFAULT_VALUES.put(UI_PERFORMANCE, 2000);
		DEFAULT_VALUES.put(GROVE_LOGGING, false);
		DEFAULT_VALUES.put(FUTURE_DATE_LIMIT_PERIODS, 24);
		DEFAULT_VALUES.put(FUTURE_DATE_LIMIT_PERIOD_TYPE,
				Common.TimePeriods.HOURS);
		DEFAULT_VALUES.put(INSTANCE_DESCRIPTION, "Scada-LTS - 2.2");

		DEFAULT_VALUES.put(CHART_BACKGROUND_COLOUR, "white");
		DEFAULT_VALUES.put(PLOT_BACKGROUND_COLOUR, "white");
		DEFAULT_VALUES.put(PLOT_GRIDLINE_COLOUR, "silver");
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, rollbackFor = SQLException.class)
	public void resetDataBase() {
		DAO.getInstance().getJdbcTemp().update(DELETE_WATCH_LISTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_MANGO_VIEWS);
		DAO.getInstance().getJdbcTemp().update(DELETE_POINT_EVENT_DETECTORS);
		DAO.getInstance().getJdbcTemp().update(DELETE_COMPOUND_EVENT_DETECTORS);
		DAO.getInstance().getJdbcTemp().update(DELETE_SCHEDULED_EVENTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_POINT_LINKS);
		DAO.getInstance().getJdbcTemp().update(DELETE_EVENTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_REPORTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_POINT_HIERARCHY);
		DAO.getInstance().getJdbcTemp().update(DELETE_EVENT_HANDLERS);
		DAO.getInstance().getJdbcTemp().update(DELETE_SCRIPTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_POINT_VALUES);
		DAO.getInstance().getJdbcTemp().update(DELETE_MAINTENANCE_EVENTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_MAILING_LISTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_USERS);
		DAO.getInstance().getJdbcTemp().update(DELETE_PUBLISHERS);
		DAO.getInstance().getJdbcTemp().update(DELETE_DATA_POINT_USERS);
		DAO.getInstance().getJdbcTemp().update(DELETE_DATA_SOURCE_USERS);
		DAO.getInstance().getJdbcTemp().update(DELETE_DATA_POINTS);
		DAO.getInstance().getJdbcTemp().update(DELETE_DATA_SOURCES);
	}

	public double getDataBaseSize() {

		final List<Double> size = new ArrayList<>();

		Statement statement;
		ResultSet rs;
		try {
			statement = DAO.getInstance().getJdbcTemp().getDataSource().getConnection().createStatement();
			rs = statement.executeQuery(SELECT_DATABASE);
			String dbName = "";
			while (rs.next()) {
				dbName = rs.getString(DATABASE_STATEMENT);
			}
			rs.close();

			rs = statement.executeQuery(DATABASE_SIZE + "\"" + dbName + "\";");
			while (rs.next()) {
				size.add(Math.round(Double.parseDouble(rs.getString("size")) * 100) / 100d);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return size.get(0);
	}
}
