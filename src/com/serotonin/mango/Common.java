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
package com.serotonin.mango;

import gnu.io.CommPortIdentifier;

import java.io.File;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.joda.time.Period;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.KeyValuePair;
import org.scada_lts.dao.SystemSettingsDAO;
import com.serotonin.mango.util.BackgroundContext;
import com.serotonin.mango.util.CommPortConfigException;
import com.serotonin.mango.util.ExportCodes;
import com.serotonin.mango.view.View;
import com.serotonin.mango.view.custom.CustomView;
import com.serotonin.mango.vo.CommPortProxy;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.web.ContextWrapper;
import com.serotonin.monitor.MonitoredValues;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.timer.RealTimeTimer;
import com.serotonin.util.PropertiesUtils;
import com.serotonin.util.StringUtils;
import com.serotonin.web.i18n.I18NUtils;
import com.serotonin.web.i18n.LocalizableMessage;
import com.serotonin.web.i18n.Utf8ResourceBundle;

public class Common {
	
	private static final String ANON_VIEW_KEY = "anonymousViews";
	private static final String CUSTOM_VIEW_KEY = "customView";

	public static final String SESSION_USER = "sessionUser";
	public static final String UTF8 = "UTF-8";
	public static final Charset UTF8_CS = Charset.forName(UTF8);

	public static final int NEW_ID = -1;
	public static ContextWrapper ctx;

	// This is initialized
	public static final RealTimeTimer timer = new RealTimeTimer();

	public static final MonitoredValues MONITORED_VALUES = new MonitoredValues();

	private static String environmentProfileName = "env";

	private static Long startupTime = null;

	/*
	 * Updating the Mango version: - Create a DBUpdate subclass for the old
	 * version number. This may not do anything in particular to the schema, but
	 * is still required to update the system settings so that the database has
	 * the correct version.
	 */
	public static final String getVersion() {
		return "2.4.0";
	}

	public static final Long getStartupTime() {
		if (startupTime != null) {
			return startupTime;
		} else {
			startupTime = System.currentTimeMillis();
			return startupTime;
		}
	}

	public interface ContextKeys {
		String DATABASE_ACCESS = "DATABASE_ACCESS";
		String IMAGE_SETS = "IMAGE_SETS";
		String DYNAMIC_IMAGES = "DYNAMIC_IMAGES";
		String RUNTIME_MANAGER = "RUNTIME_MANAGER";
		String SCHEDULER = "SCHEDULER";
		String EVENT_MANAGER = "EVENT_MANAGER";
		String FREEMARKER_CONFIG = "FREEMARKER_CONFIG";
		String BACKGROUND_PROCESSING = "BACKGROUND_PROCESSING";
		String HTTP_RECEIVER_MULTICASTER = "HTTP_RECEIVER_MULTICASTER";
		String DOCUMENTATION_MANIFEST = "DOCUMENTATION_MANIFEST";
		String DATA_POINTS_NAME_ID_MAPPING = "DATAPOINTS_NAME_ID_MAPPING";
	}

	public interface TimePeriods {
		int MILLISECONDS = 8;
		int SECONDS = 1;
		int MINUTES = 2;
		int HOURS = 3;
		int DAYS = 4;
		int WEEKS = 5;
		int MONTHS = 6;
		int YEARS = 7;
	}

	public static ExportCodes TIME_PERIOD_CODES = new ExportCodes();
	static {
		TIME_PERIOD_CODES.addElement(TimePeriods.MILLISECONDS, "MILLISECONDS");
		TIME_PERIOD_CODES.addElement(TimePeriods.SECONDS, "SECONDS");
		TIME_PERIOD_CODES.addElement(TimePeriods.MINUTES, "MINUTES");
		TIME_PERIOD_CODES.addElement(TimePeriods.HOURS, "HOURS");
		TIME_PERIOD_CODES.addElement(TimePeriods.DAYS, "DAYS");
		TIME_PERIOD_CODES.addElement(TimePeriods.WEEKS, "WEEKS");
		TIME_PERIOD_CODES.addElement(TimePeriods.MONTHS, "MONTHS");
		TIME_PERIOD_CODES.addElement(TimePeriods.YEARS, "YEARS");
	}

	public interface GroveServlets {
		String VERSION_CHECK = "versionCheckComm";
		String MANGO_LOG = "mangoLog";
	}

	/**
	 * Returns the length of time in milliseconds that the
	 *
	 * @return
	 */
	public static long getMillis(int periodType, int periods) {
		return getPeriod(periodType, periods).toDurationFrom(null).getMillis();
	}

	public static Period getPeriod(int periodType, int periods) {
		switch (periodType) {
		case TimePeriods.MILLISECONDS:
			return Period.millis(periods);
		case TimePeriods.SECONDS:
			return Period.seconds(periods);
		case TimePeriods.MINUTES:
			return Period.minutes(periods);
		case TimePeriods.HOURS:
			return Period.hours(periods);
		case TimePeriods.DAYS:
			return Period.days(periods);
		case TimePeriods.WEEKS:
			return Period.weeks(periods);
		case TimePeriods.MONTHS:
			return Period.months(periods);
		case TimePeriods.YEARS:
			return Period.years(periods);
		default:
			throw new ShouldNeverHappenException("Unsupported time period: "
					+ periodType);
		}
	}

	public static LocalizableMessage getPeriodDescription(int periodType,
			int periods) {
		String periodKey;
		switch (periodType) {
		case TimePeriods.MILLISECONDS:
			periodKey = "common.tp.milliseconds";
			break;
		case TimePeriods.SECONDS:
			periodKey = "common.tp.seconds";
			break;
		case TimePeriods.MINUTES:
			periodKey = "common.tp.minutes";
			break;
		case TimePeriods.HOURS:
			periodKey = "common.tp.hours";
			break;
		case TimePeriods.DAYS:
			periodKey = "common.tp.days";
			break;
		case TimePeriods.WEEKS:
			periodKey = "common.tp.weeks";
			break;
		case TimePeriods.MONTHS:
			periodKey = "common.tp.months";
			break;
		case TimePeriods.YEARS:
			periodKey = "common.tp.years";
			break;
		default:
			throw new ShouldNeverHappenException("Unsupported time period: "
					+ periodType);
		}

		return new LocalizableMessage("common.tp.description", periods,
				new LocalizableMessage(periodKey));
	}

	//
	// Session user
	public static User getUser() {
		WebContext webContext = WebContextFactory.get();
		if (webContext == null) {
			// If there is no web context, check if there is a background
			// context
			BackgroundContext backgroundContext = BackgroundContext.get();
			if (backgroundContext == null)
				return null;
			return backgroundContext.getUser();
		}
		return getUser(webContext.getHttpServletRequest());
	}

	public static User getUser(HttpServletRequest request) {
		// Check first to see if the user object is in the request.
		User user = (User) request.getAttribute(SESSION_USER);
		if (user != null)
			return user;

		// If not, get it from the session.
		user = (User) request.getSession().getAttribute(SESSION_USER);

		if (user != null)
			// Add the user to the request. This prevents race conditions in
			// which long-ish lasting requests have the
			// user object swiped from them by a quicker (logout) request.
			request.setAttribute(SESSION_USER, user);

		return user;
	}

	public static void setUser(HttpServletRequest request, User user) {
		request.getSession().setAttribute(SESSION_USER, user);
	}

	//
	// Background process description. Used for audit logs when the system
	// automatically makes changes to data, such as
	// safe mode disabling stuff.
	public static String getBackgroundProcessDescription() {
		BackgroundContext backgroundContext = BackgroundContext.get();
		if (backgroundContext == null)
			return null;
		return backgroundContext.getProcessDescriptionKey();
	}

	//
	// Anonymous views
	public static View getAnonymousView(int id) {
		return getAnonymousView(
				WebContextFactory.get().getHttpServletRequest(), id);
	}

	public static View getAnonymousView(HttpServletRequest request, int id) {
		List<View> views = getAnonymousViews(request);
		if (views == null)
			return null;
		for (View view : views) {
			if (view.getId() == id)
				return view;
		}
		return null;
	}

	public static void addAnonymousView(HttpServletRequest request, View view) {
		List<View> views = getAnonymousViews(request);
		if (views == null) {
			views = new ArrayList<View>();
			request.getSession().setAttribute(ANON_VIEW_KEY, views);
		}
		// Remove the view if it already exists.
		for (int i = views.size() - 1; i >= 0; i--) {
			if (views.get(i).getId() == view.getId())
				views.remove(i);
		}
		views.add(view);
	}

	@SuppressWarnings("unchecked")
	private static List<View> getAnonymousViews(HttpServletRequest request) {
		return (List<View>) request.getSession().getAttribute(ANON_VIEW_KEY);
	}

	//
	// Custom views
	public static CustomView getCustomView() {
		return getCustomView(WebContextFactory.get().getHttpServletRequest());
	}

	public static CustomView getCustomView(HttpServletRequest request) {
		return (CustomView) request.getSession().getAttribute(CUSTOM_VIEW_KEY);
	}

	public static void setCustomView(HttpServletRequest request, CustomView view) {
		request.getSession().setAttribute(CUSTOM_VIEW_KEY, view);
	}

	//
	// Environment profile
	public static PropertiesUtils getEnvironmentProfile() {
		return new PropertiesUtils(environmentProfileName);
	}

	public static void changeEnvironmentProfile(String provilePath) {
		environmentProfileName = provilePath;
	}

	public static String getGroveUrl(String servlet) {
		String grove = getEnvironmentProfile().getString("grove.url",
				"http://mango.serotoninsoftware.com/servlet");
		return grove + "/" + servlet;
	}

	public static String getDocPath() {
		return ctx.getServletContext().getRealPath("WEB-INF/dox") + "/";
	}

	private static String lazyFiledataPath = null;

	public static String getFiledataPath() {
		if (lazyFiledataPath == null) {
			String name = SystemSettingsDAO
					.getValue(SystemSettingsDAO.FILEDATA_PATH);
			if (name.startsWith("~"))
				name = ctx.getServletContext().getRealPath(name.substring(1));

			File file = new File(name);
			if (!file.exists())
				file.mkdirs();

			lazyFiledataPath = name;
		}
		return lazyFiledataPath;
	}

	public static CronTimerTrigger getCronTrigger(int periodType,
			int delaySeconds) {
		int delayMinutes = 0;
		if (delaySeconds >= 60) {
			delayMinutes = delaySeconds / 60;
			delaySeconds %= 60;

			if (delayMinutes >= 60)
				delayMinutes = 59;
		}

		try {
			switch (periodType) {
			case TimePeriods.MILLISECONDS:
				throw new ShouldNeverHappenException(
						"Can't create a cron trigger for milliseconds");
			case TimePeriods.SECONDS:
				return new CronTimerTrigger("* * * * * ?");
			case TimePeriods.MINUTES:
				return new CronTimerTrigger(delaySeconds + " * * * * ?");
			case TimePeriods.HOURS:
				return new CronTimerTrigger(delaySeconds + " " + delayMinutes
						+ " * * * ?");
			case TimePeriods.DAYS:
				return new CronTimerTrigger(delaySeconds + " " + delayMinutes
						+ " 0 * * ?");
			case TimePeriods.WEEKS:
				return new CronTimerTrigger(delaySeconds + " " + delayMinutes
						+ " 0 ? * MON");
			case TimePeriods.MONTHS:
				return new CronTimerTrigger(delaySeconds + " " + delayMinutes
						+ " 0 1 * ?");
			case TimePeriods.YEARS:
				return new CronTimerTrigger(delaySeconds + " " + delayMinutes
						+ " 0 1 JAN ?");
			default:
				throw new ShouldNeverHappenException(
						"Invalid cron period type: " + periodType);
			}
		} catch (ParseException e) {
			throw new ShouldNeverHappenException(e);
		}
	}

	//
	// Misc
	public static List<CommPortProxy> getCommPorts()
			throws CommPortConfigException {
		try {
			List<CommPortProxy> ports = new LinkedList<CommPortProxy>();
			Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();
			CommPortIdentifier cpid;
			while (portEnum.hasMoreElements()) {
				cpid = (CommPortIdentifier) portEnum.nextElement();
				if (cpid.getPortType() == CommPortIdentifier.PORT_SERIAL)
					ports.add(new CommPortProxy(cpid));
			}
			return ports;
		} catch (UnsatisfiedLinkError e) {
			throw new CommPortConfigException(e.getMessage());
		} catch (NoClassDefFoundError e) {
			throw new CommPortConfigException(
					"Comm configuration error. Check that rxtx DLL or libraries have been correctly installed.");
		}
	}

	public synchronized static String encrypt(String plaintext) {
		try {
			String alg = getEnvironmentProfile().getString(
					"security.hashAlgorithm", "SHA");
			if ("NONE".equals(alg))
				return plaintext;

			MessageDigest md = MessageDigest.getInstance(alg);
			if (md == null)
				throw new ShouldNeverHappenException(
						"MessageDigest algorithm "
								+ alg
								+ " not found. Set the 'security.hashAlgorithm' property in env.properties appropriately. "
								+ "Use 'NONE' for no hashing.");
			md.update(plaintext.getBytes(UTF8_CS));
			byte raw[] = md.digest();
			String hash = new String(Base64.encodeBase64(raw));
			return hash;
		} catch (NoSuchAlgorithmException e) {
			// Should never happen, so just wrap in a runtime exception and
			// rethrow
			throw new ShouldNeverHappenException(e);
		}
	}

	//
	// HttpClient
	public static HttpClient getHttpClient() {
		return getHttpClient(30000); // 30 seconds.
	}

	public static HttpClient getHttpClient(int timeout) {
		HttpConnectionManagerParams managerParams = new HttpConnectionManagerParams();
		managerParams.setConnectionTimeout(timeout);
		managerParams.setSoTimeout(timeout);

		HttpClientParams params = new HttpClientParams();
		params.setSoTimeout(timeout);

		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().setParams(managerParams);
		client.setParams(params);

		if (SystemSettingsDAO
				.getBooleanValue(SystemSettingsDAO.HTTP_CLIENT_USE_PROXY)) {
			String proxyHost = SystemSettingsDAO
					.getValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_SERVER);
			int proxyPort = SystemSettingsDAO
					.getIntValue(SystemSettingsDAO.HTTP_CLIENT_PROXY_PORT);

			// Set up the proxy configuration.
			client.getHostConfiguration().setProxy(proxyHost, proxyPort);

			// Set up the proxy credentials. All realms and hosts.
			client.getState()
					.setProxyCredentials(
							AuthScope.ANY,
							new UsernamePasswordCredentials(
									SystemSettingsDAO
											.getValue(
													SystemSettingsDAO.HTTP_CLIENT_PROXY_USERNAME,
													""),
									SystemSettingsDAO
											.getValue(
													SystemSettingsDAO.HTTP_CLIENT_PROXY_PASSWORD,
													"")));
		}

		return client;
	}

	//
	//
	// i18n
	//
	private static Object i18nLock = new Object();
	private static String systemLanguage;
	private static ResourceBundle systemBundle;

	public static String getMessage(String key) {
		ensureI18n();
		return I18NUtils.getMessage(systemBundle, key);
	}

	public static ResourceBundle getBundle() {
		ensureI18n();
		return systemBundle;
	}

	private static void ensureI18n() {
		if (systemLanguage == null) {
			synchronized (i18nLock) {
				if (systemLanguage == null) {
					systemLanguage = SystemSettingsDAO
							.getValue(SystemSettingsDAO.LANGUAGE);
					Locale locale = findLocale(systemLanguage);
					if (locale == null)
						throw new IllegalArgumentException(
								"Locale for given language not found: "
										+ systemLanguage);
					systemBundle = Utf8ResourceBundle.getBundle("messages",
							locale);
				}
			}
		}
	}

	public static String getMessage(String key, Object... args) {
		String pattern = getMessage(key);
		return MessageFormat.format(pattern, args);
	}

	public static void setSystemLanguage(String language) {
		if (findLocale(language) == null)
			throw new IllegalArgumentException(
					"Locale for given language not found: " + language);
		new SystemSettingsDAO().setValue(SystemSettingsDAO.LANGUAGE, language);
		systemLanguage = null;
		systemBundle = null;
	}

	private static Locale findLocale(String language) {
		for (Locale locale : Locale.getAvailableLocales()) {
			if (locale.getLanguage().equals(language))
				return locale;
		}
		return null;
	}

	public static List<KeyValuePair> getLanguages() {
		List<KeyValuePair> languages = new ArrayList<KeyValuePair>();
		ResourceBundle i18n = Utf8ResourceBundle.getBundle("i18n");
		for (String key : i18n.keySet())
			languages.add(new KeyValuePair(key, i18n.getString(key)));
		return languages;
	}

	public static String generateXid(String prefix) {
		return prefix + StringUtils.generateRandomString(6, "0123456789");
	}

}
