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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import br.org.scadabr.db.dao.ScriptDao;
import br.org.scadabr.db.dao.UsersProfileDao;
import br.org.scadabr.vo.exporter.ZIPProjectManager;
import br.org.scadabr.vo.exporter.util.PointValueJSONWrapper;
import br.org.scadabr.vo.exporter.util.SystemSettingsJSONWrapper;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonValue;
import com.serotonin.json.JsonWriter;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.CompoundEventDetectorDao;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.db.dao.MailingListDao;
import com.serotonin.mango.db.dao.MaintenanceEventDao;
import com.serotonin.mango.db.dao.PointLinkDao;
import com.serotonin.mango.db.dao.PointValueDao;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.db.dao.ScheduledEventDao;
import com.serotonin.mango.vo.event.PointEventDetectorVO;
import org.scada_lts.dao.PointEventDetectorDAO;
import org.scada_lts.dao.SystemSettingsDAO;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.ViewDao;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.rt.RuntimeManager;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.web.dwr.beans.ImportTask;
import com.serotonin.web.dwr.DwrResponseI18n;

/**
 * @author Matthew Lohbihler
 */
public class EmportDwr extends BaseDwr {
	public static final String GRAPHICAL_VIEWS = "graphicalViews";
	public static final String EVENT_HANDLERS = "eventHandlers";
	public static final String DATA_SOURCES = "dataSources";
	public static final String DATA_POINTS = "dataPoints";
	public static final String SCHEDULED_EVENTS = "scheduledEvents";
	public static final String COMPOUND_EVENT_DETECTORS = "compoundEventDetectors";
	public static final String POINT_LINKS = "pointLinks";
	public static final String USERS = "users";
	public static final String POINT_HIERARCHY = "pointHierarchy";
	public static final String MAILING_LISTS = "mailingLists";
	public static final String PUBLISHERS = "publishers";
	public static final String WATCH_LISTS = "watchLists";
	public static final String MAINTENANCE_EVENTS = "maintenanceEvents";
	public static final String SCRIPTS = "scripts";
	public static final String POINT_VALUES = "pointValues";
	public static final String SYSTEM_SETTINGS = "systemSettings";
	public static final String USERS_PROFILES = "usersProfiles";

	public String createExportData(int prettyIndent, boolean graphicalViews,
			boolean eventHandlers, boolean dataSources, boolean dataPoints,
			boolean scheduledEvents, boolean compoundEventDetectors,
			boolean pointLinks, boolean users, boolean pointHierarchy,
			boolean mailingLists, boolean publishers, boolean watchLists,
			boolean maintenanceEvents, boolean scripts, boolean pointValues,
			int maxPointValues, boolean systemSettings, boolean usersProfiles) {

		if (!Common.getUser().isAdmin()) {
			return "Only admin user can export data.";
		}

		return EmportDwr.createExportJSON(prettyIndent, graphicalViews,
				eventHandlers, dataSources, dataPoints, scheduledEvents,
				compoundEventDetectors, pointLinks, users, pointHierarchy,
				mailingLists, publishers, watchLists, maintenanceEvents,
				scripts, pointValues, maxPointValues, systemSettings,
				usersProfiles);
	}
	public static String exportJSON(String xid){
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		DataPointVO dataPoints = new DataPointDao().getDataPointByXid(xid);
		List<PointEventDetectorVO> detectors = new PointEventDetectorDAO().getPointEventDetectors(dataPoints);
		dataPoints.setEventDetectors(detectors);
		data.put(DATA_POINTS, dataPoints == null?"In the database there is no data point with given xid "+xid:dataPoints);
		JsonWriter writer = new JsonWriter();
		try {
			return writer.write(data);
		} catch (JsonException e) {
			throw new ShouldNeverHappenException(e);
		} catch (IOException e) {
			throw new ShouldNeverHappenException(e);
		}
	}
	public static String createExportJSON(int prettyIndent,
			boolean graphicalViews, boolean eventHandlers, boolean dataSources,
			boolean dataPoints, boolean scheduledEvents,
			boolean compoundEventDetectors, boolean pointLinks, boolean users,
			boolean pointHierarchy, boolean mailingLists, boolean publishers,
			boolean watchLists, boolean maintenanceEvents, boolean scripts,
			boolean pointValues, int maxPointValues, boolean systemSettings,
			boolean usersProfiles) {
		Map<String, Object> data = new LinkedHashMap<String, Object>();

		if (graphicalViews)
			data.put(GRAPHICAL_VIEWS, new ViewDao().getViews());
		if (dataSources)
			data.put(DATA_SOURCES, new DataSourceDao().getDataSources());

		List<DataPointVO> allDataPoints = new DataPointDao().getDataPoints(
				null, true);

		if (dataPoints)
			data.put(DATA_POINTS, allDataPoints);
		if (scheduledEvents)
			data.put(SCHEDULED_EVENTS,
					new ScheduledEventDao().getScheduledEvents());
		if (compoundEventDetectors)
			data.put(COMPOUND_EVENT_DETECTORS,
					new CompoundEventDetectorDao().getCompoundEventDetectors());
		if (pointLinks)
			data.put(POINT_LINKS, new PointLinkDao().getPointLinks());
		if (users)
			data.put(USERS, new UserDao().getUsers());
		if (mailingLists)
			data.put(MAILING_LISTS, new MailingListDao().getMailingLists());
		if (publishers)
			data.put(PUBLISHERS, new PublisherDao().getPublishers());
		if (pointHierarchy)
			data.put(POINT_HIERARCHY, new DataPointDao().getPointHierarchy()
					.getRoot().getSubfolders());
		if (eventHandlers)
			data.put(EVENT_HANDLERS, new EventDao().getEventHandlers());
		if (watchLists) {
			WatchListDao watchListDao = new WatchListDao();
			List<WatchList> wls = watchListDao.getWatchLists();
			watchListDao.populateWatchlistData(wls);
			data.put(WATCH_LISTS, wls);
		}
		if (maintenanceEvents)
			data.put(MAINTENANCE_EVENTS,
					new MaintenanceEventDao().getMaintenanceEvents());

		if (scripts)
			data.put(SCRIPTS, new ScriptDao().getScripts());
		if (pointValues) {
			List<PointValueJSONWrapper> allWrappedValues = new ArrayList<PointValueJSONWrapper>();

			long antes = System.currentTimeMillis();
			PointValueDao dao = new PointValueDao();
			for (DataPointVO dataPointVO : allDataPoints) {
				allWrappedValues.addAll(PointValueJSONWrapper.wrapPointValues(
						dataPointVO.getXid(), dao.getLatestPointValues(
								dataPointVO.getId(), maxPointValues)));
			}
			data.put(POINT_VALUES, allWrappedValues);
		}
		if (systemSettings) {
			SystemSettingsJSONWrapper sysSetWrapp = new SystemSettingsJSONWrapper();
			List<SystemSettingsJSONWrapper> list = new ArrayList<SystemSettingsJSONWrapper>();
			list.add(sysSetWrapp);
			data.put(SYSTEM_SETTINGS, list);
		}
		if (usersProfiles) {
			data.put(USERS_PROFILES, new UsersProfileDao().getUsersProfiles());
		}

		JsonWriter writer = new JsonWriter();
		writer.setPrettyIndent(prettyIndent);
		writer.setPrettyOutput(true);

		try {
			return writer.write(data);
		} catch (JsonException e) {
			throw new ShouldNeverHappenException(e);
		} catch (IOException e) {
			throw new ShouldNeverHappenException(e);
		}
	}

	public DwrResponseI18n importData(String data) {
		ResourceBundle bundle = getResourceBundle();
		User user = Common.getUser();
		DwrResponseI18n response = EmportDwr.importDataImpl(data, bundle, user);

		return response;
	}

	public static DwrResponseI18n importDataImpl(String data,
			ResourceBundle bundle, User user) {
		DwrResponseI18n response = new DwrResponseI18n();

		Permissions.ensureAdmin(user);

		JsonReader reader = new JsonReader(data);
		try {
			JsonValue value = reader.inflate();
			if (value instanceof JsonObject) {
				JsonObject root = value.toJsonObject();
				ImportTask importTask = new ImportTask(reader, root, bundle,
						user);
				user.setImportTask(importTask);
				response.addData("importStarted", true);
			} else {
				response.addGenericMessage("emport.invalidImportData");
			}
		} catch (ClassCastException e) {
			response.addGenericMessage("emport.parseError", e.getMessage());
		} catch (LocalizableJsonException e) {
			response.addMessage(e.getMsg());
		} catch (JsonException e) {
			response.addGenericMessage("emport.parseError", e.getMessage());
		}

		return response;
	}

	public DwrResponseI18n importUpdate() {
		DwrResponseI18n response;
		User user = Common.getUser();
		ImportTask importTask = user.getImportTask();
		if (importTask != null) {
			response = importTask.getResponse();

			if (importTask.isCancelled()) {
				response.addData("cancelled", true);
				user.setImportTask(null);
			} else if (importTask.isCompleted()) {
				response.addData("complete", true);
				user.setImportTask(null);
			}
		} else {
			response = new DwrResponseI18n();
			response.addData("noImport", true);
		}

		return response;
	}

	public void importCancel() {
		User user = Common.getUser();
		if (user.getImportTask() != null)
			user.getImportTask().cancel();
	}

	public boolean loadProject() {
		User user = Common.getUser();
		ZIPProjectManager importer = user.getUploadedProject();
		try {

			stopRunningDataSources();
			new SystemSettingsDAO().resetDataBase();
			importer.importProject();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void importCancelled() {
		User user = Common.getUser();
		user.setUploadedProject(null);
	}

	private void stopRunningDataSources() {
		List<DataSourceVO<?>> dataSources = new DataSourceDao()
				.getDataSources();

		RuntimeManager rtm = Common.ctx.getRuntimeManager();
		for (DataSourceVO<?> dataSourceVO : dataSources) {
			if (dataSourceVO.isEnabled())
				rtm.stopDataSource(dataSourceVO.getId());
		}

	}
}