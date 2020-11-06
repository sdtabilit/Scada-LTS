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
package com.serotonin.mango.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import br.org.scadabr.vo.exporter.ZIPProjectManager;
import br.org.scadabr.vo.usersProfiles.UsersProfileVO;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonArray;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonObject;
import com.serotonin.json.JsonReader;
import com.serotonin.json.JsonRemoteEntity;
import com.serotonin.json.JsonRemoteProperty;
import com.serotonin.json.JsonSerializable;
import com.serotonin.json.JsonValue;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.DataPointDao;
import com.serotonin.mango.db.dao.DataSourceDao;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.view.View;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import com.serotonin.mango.vo.permission.DataPointAccess;
import com.serotonin.mango.vo.permission.Permissions;
import com.serotonin.mango.vo.publish.PublishedPointVO;
import com.serotonin.mango.vo.publish.PublisherVO;
import com.serotonin.mango.web.dwr.beans.DataExportDefinition;
import com.serotonin.mango.web.dwr.beans.EventExportDefinition;
import com.serotonin.mango.web.dwr.beans.ImportTask;
import com.serotonin.mango.web.dwr.beans.TestingUtility;
import com.serotonin.util.StringUtils;
import com.serotonin.web.dwr.DwrResponseI18n;
import com.serotonin.web.i18n.LocalizableMessage;

@JsonRemoteEntity
public class User implements SetPointSource, HttpSessionBindingListener,
		JsonSerializable {
	private int id = Common.NEW_ID;
	@JsonRemoteProperty
	private String username;
	@JsonRemoteProperty
	private String password;
	@JsonRemoteProperty
	private String email;
	@JsonRemoteProperty
	private String phone;
	@JsonRemoteProperty
	private boolean admin;
	@JsonRemoteProperty
	private boolean disabled;
	private List<Integer> dataSourcePermissions;
	private List<DataPointAccess> dataPointPermissions;
	private int selectedWatchList;
	@JsonRemoteProperty
	private String homeUrl;
	private long lastLogin;
	private int receiveAlarmEmails;
	@JsonRemoteProperty
	private boolean receiveOwnAuditEvents;

	//
	// Session data. The user object is stored in session, and some other
	// session-based information is cached here
	// for convenience.
	//
	@JsonRemoteProperty
	private int userProfile = Common.NEW_ID;

	private transient View view;
	private transient WatchList watchList;
	private transient DataPointVO editPoint;
	private transient DataSourceVO<?> editDataSource;
	private transient TestingUtility testingUtility;
	private transient Map<String, byte[]> reportImageData;
	private transient PublisherVO<? extends PublishedPointVO> editPublisher;
	private transient ImportTask importTask;
	private transient boolean muted = false;
	private transient DataExportDefinition dataExportDefinition;
	private transient EventExportDefinition eventExportDefinition;
	private transient Map<String, Object> attributes = new HashMap<String, Object>();

	/**
	 * Used for various display purposes.
	 */
	public String getDescription() {
		return username + " (" + id + ")";
	}

	public boolean isFirstLogin() {
		return lastLogin == 0;
	}

	//
	// /
	// / SetPointSource implementation
	// /
	//
	public int getSetPointSourceId() {
		return id;
	}

	public int getSetPointSourceType() {
		return SetPointSource.Types.USER;
	}

	@Override
	public void raiseRecursionFailureEvent() {
		throw new ShouldNeverHappenException("");
	}

	//
	// /
	// / HttpSessionBindingListener implementation
	// /
	//
	public void valueBound(HttpSessionBindingEvent evt) {
		// User is bound to a session when logged in. Notify the event manager.
		SystemEventType.raiseEvent(new SystemEventType(
				SystemEventType.TYPE_USER_LOGIN, id), System
				.currentTimeMillis(), true, new LocalizableMessage(
				"event.login", username));
	}

	public void valueUnbound(HttpSessionBindingEvent evt) {
		// User is unbound from a session when logged out or the session
		// expires.
		SystemEventType.returnToNormal(new SystemEventType(
				SystemEventType.TYPE_USER_LOGIN, id), System
				.currentTimeMillis());

		// Terminate any testing utility
		if (testingUtility != null)
			testingUtility.cancel();
	}

	// Convenience method for JSPs
	public boolean isDataSourcePermission() {
		return Permissions.hasDataSourcePermission(this);
	}

	//
	// Testing utility management
	public <T extends TestingUtility> T getTestingUtility(Class<T> requiredClass) {
		TestingUtility tu = testingUtility;

		if (tu != null) {
			try {
				return requiredClass.cast(tu);
			} catch (ClassCastException e) {
				tu.cancel();
				testingUtility = null;
			}
		}
		return null;
	}

	public void setTestingUtility(TestingUtility testingUtility) {
		TestingUtility tu = this.testingUtility;
		if (tu != null)
			tu.cancel();
		this.testingUtility = testingUtility;
	}

	public void cancelTestingUtility() {
		setTestingUtility(null);
	}

	// Properties
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public WatchList getWatchList() {
		return watchList;
	}

	public void setWatchList(WatchList watchList) {
		this.watchList = watchList;
	}

	public DataPointVO getEditPoint() {
		return editPoint;
	}

	public void setEditPoint(DataPointVO editPoint) {
		this.editPoint = editPoint;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public List<Integer> getDataSourcePermissions() {
		if (dataSourcePermissions==null) {
			dataSourcePermissions = new LinkedList<Integer>();
		}
		
		return dataSourcePermissions;
		
	}

	public void setDataSourcePermissions(List<Integer> dataSourcePermissions) {
		this.dataSourcePermissions = dataSourcePermissions;
	}

	public List<DataPointAccess> getDataPointPermissions() {
		if (dataPointPermissions==null) {
			dataPointPermissions = new LinkedList<DataPointAccess>();
		} 
		return dataPointPermissions;
		
	}

	public void setDataPointPermissions(
			List<DataPointAccess> dataPointPermissions) {
		this.dataPointPermissions = dataPointPermissions;
	}

	public DataSourceVO<?> getEditDataSource() {
		return editDataSource;
	}

	public void setEditDataSource(DataSourceVO<?> editDataSource) {
		this.editDataSource = editDataSource;
	}

	public int getSelectedWatchList() {
		return selectedWatchList;
	}

	public void setSelectedWatchList(int selectedWatchList) {
		this.selectedWatchList = selectedWatchList;
	}

	public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Map<String, byte[]> getReportImageData() {
		return reportImageData;
	}

	public void setReportImageData(Map<String, byte[]> reportImageData) {
		this.reportImageData = reportImageData;
	}

	public PublisherVO<? extends PublishedPointVO> getEditPublisher() {
		return editPublisher;
	}

	public void setEditPublisher(
			PublisherVO<? extends PublishedPointVO> editPublisher) {
		this.editPublisher = editPublisher;
	}

	public ImportTask getImportTask() {
		return importTask;
	}

	public void setImportTask(ImportTask importTask) {
		this.importTask = importTask;
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public int getReceiveAlarmEmails() {
		return receiveAlarmEmails;
	}

	public void setReceiveAlarmEmails(int receiveAlarmEmails) {
		this.receiveAlarmEmails = receiveAlarmEmails;
	}

	public boolean isReceiveOwnAuditEvents() {
		return receiveOwnAuditEvents;
	}

	public void setReceiveOwnAuditEvents(boolean receiveOwnAuditEvents) {
		this.receiveOwnAuditEvents = receiveOwnAuditEvents;
	}

	public DataExportDefinition getDataExportDefinition() {
		return dataExportDefinition;
	}

	public void setDataExportDefinition(
			DataExportDefinition dataExportDefinition) {
		this.dataExportDefinition = dataExportDefinition;
	}

	public EventExportDefinition getEventExportDefinition() {
		return eventExportDefinition;
	}

	public void setEventExportDefinition(
			EventExportDefinition eventExportDefinition) {
		this.eventExportDefinition = eventExportDefinition;
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public Object removeAttribute(String key) {
		return attributes.remove(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String key) {
		return (T) attributes.get(key);
	}

	public void validate(DwrResponseI18n response) {
		if (StringUtils.isEmpty(username))
			response.addMessage("username", new LocalizableMessage(
					"validate.required"));
		if (StringUtils.isEmpty(email))
			response.addMessage("email", new LocalizableMessage(
					"validate.required"));
		if (id == Common.NEW_ID && StringUtils.isEmpty(password))
			response.addMessage("password", new LocalizableMessage(
					"validate.required"));

		// Check field lengths
		if (StringUtils.isLengthGreaterThan(username, 40))
			response.addMessage("username", new LocalizableMessage(
					"validate.notLongerThan", 40));
		if (StringUtils.isLengthGreaterThan(email, 255))
			response.addMessage("email", new LocalizableMessage(
					"validate.notLongerThan", 255));
		if (StringUtils.isLengthGreaterThan(phone, 40))
			response.addMessage("phone", new LocalizableMessage(
					"validate.notLongerThan", 40));
	}

	//
	// /
	// / Serialization
	// /
	//
	@Override
	public void jsonDeserialize(JsonReader reader, JsonObject json) {
		// Note: data source permissions are explicitly deserialized by the
		// import/export because the data sources and
		// points need to be certain to exist before we can resolve the xids.
	}

	public void jsonDeserializePermissions(JsonReader reader, JsonObject json)
			throws JsonException {
		if (admin) {
			dataSourcePermissions.clear();
			dataPointPermissions.clear();
		} else {
			JsonArray jsonDataSources = json
					.getJsonArray("dataSourcePermissions");
			if (jsonDataSources != null) {
				dataSourcePermissions.clear();
				DataSourceDao dataSourceDao = new DataSourceDao();

				for (JsonValue jv : jsonDataSources.getElements()) {
					String xid = jv.toJsonString().getValue();
					DataSourceVO<?> ds = dataSourceDao.getDataSource(xid);
					if (ds == null)
						throw new LocalizableJsonException(
								"emport.error.missingSource", xid);
					dataSourcePermissions.add(ds.getId());
				}
			}

			JsonArray jsonPoints = json.getJsonArray("dataPointPermissions");
			if (jsonPoints != null) {
				// Get a list of points to which permission already exists due
				// to data source access.
				DataPointDao dataPointDao = new DataPointDao();
				List<Integer> permittedPoints = new ArrayList<Integer>();
				for (Integer dsId : dataSourcePermissions) {
					for (DataPointVO dp : dataPointDao
							.getDataPoints(dsId, null))
						permittedPoints.add(dp.getId());
				}

				dataPointPermissions.clear();

				for (JsonValue jv : jsonPoints.getElements()) {
					DataPointAccess access = reader.readPropertyValue(jv,
							DataPointAccess.class, null);
					if (!permittedPoints.contains(access.getDataPointId()))
						// The user doesn't already have access to the point.
						dataPointPermissions.add(access);
				}
			}
		}
	}

	@Override
	public void jsonSerialize(Map<String, Object> map) {
		if (!admin) {
			List<String> dsXids = new ArrayList<String>();
			DataSourceDao dataSourceDao = new DataSourceDao();
			for (Integer dsId : getDataSourcePermissions())
				dsXids.add(dataSourceDao.getDataSource(dsId).getXid());
			map.put("dataSourcePermissions", dsXids);

			map.put("dataPointPermissions", dataPointPermissions);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final User other = (User) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public void setUploadedProject(ZIPProjectManager uploadedProject) {
		this.uploadedProject = uploadedProject;
	}

	public ZIPProjectManager getUploadedProject() {
		return uploadedProject;
	}

	private ZIPProjectManager uploadedProject;

	public void setUserProfile(UsersProfileVO profile) {
		this.userProfile = profile.getId();
	}

	public int getUserProfile() {
		return userProfile;
	}

	public void resetUserProfile() {
		this.userProfile = Common.NEW_ID;
	}

	@Override
	public void pointSetComplete() {
		// TODO Auto-generated method stub

	}
}
