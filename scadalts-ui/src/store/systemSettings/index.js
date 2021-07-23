/**
 * @author Radoslaw Jajko
 *
 */

import axios from 'axios';

const storeSystemSettings = {
	state: {
		systemSettingsApiUrl: './api/systemSettings',
		databaseType: undefined,
		databaseInfo: undefined,
		systemEventTypes: undefined,
		auditEventTypes: undefined,
		systemInfoSettings: undefined,
		emailSettings: undefined,
		httpSettings: undefined,
		miscSettings: undefined,
		smsDomainSettings: undefined,
		amchartsSettings: undefined,
		systemStartupTime: undefined,
		schemaVersion: undefined,
		scadaConfig: undefined,
		defaultLoggingType: undefined,
	},
	mutations: {
		setDatabaseType(state, databaseType) {
			state.databaseType = databaseType;
		},
		setDatabaseInfo(state, databaseInfo) {
			state.databaseInfo = databaseInfo;
			state.databaseInfo.topPoints = state.databaseInfo.topPoints.slice(0, 10);
		},
		setSystemEventTypes(state, systemEventTypes) {
			state.systemEventTypes = systemEventTypes;
		},
		setAuditEventTypes(state, auditEventTypes) {
			state.auditEventTypes = auditEventTypes;
		},
		setSystemInfoSettings(state, systemInfoSettings) {
			state.systemInfoSettings = systemInfoSettings;
		},
		setEmailSettings(state, emailSettings) {
			state.emailSettings = emailSettings;
		},
		setHttpSettings(state, httpSettings) {
			state.httpSettings = httpSettings;
		},
		setMiscSettings(state, miscSettings) {
			state.miscSettings = miscSettings;
		},
		setSmsDomainSettings(state, smsDomainSettings) {
			state.smsDomainSettings = smsDomainSettings;
		},
		setAmchartsSettings(state, amchartsSettings) {
			state.amchartsSettings = amchartsSettings;
		},
		setSystemStartupTime(state, startupTime) {
			state.systemStartupTime = new Date(Number(startupTime));
		},
		setSchemaVersion(state, schemaVersion) {
			state.schemaVersion = schemaVersion;
		},
		setScadaConfig(state, scadaConfig) {
			state.scadaConfig = scadaConfig;
		},
		setDefaultLoggingType(state, defaultLoggingType) {
			state.defaultLoggingType = defaultLoggingType;
		},
	},
	actions: {
		getDatabaseType({ commit, dispatch }) {
			return dispatch('requestGet', `/systemSettings/getDatabaseType`).then((r) => {
				commit('setDatabaseType', r);
				return r;
			});
		},

		saveDatabaseType(context, databaseType) {
			return new Promise((resolve, reject) => {
				axios
					.get(`${context.state.systemSettingsApiUrl}/saveDatabaseType/${databaseType}`)
					.then((response) => {
						if (response.status == 200) {
							resolve(true);
						}
						resolve(false);
					})
					.catch((err) => {
						reject();
					});
			});
		},
		getSchemaVersion({ commit, dispatch }) {
			return dispatch('requestGet', `/systemSettings/getSchemaVersion`).then((r) => {
				commit('setSchemaVersion', r.schemaVersion);
				return r;
			});
		},

		getDatabaseSize({ commit, dispatch }) {
			return dispatch('requestGet', `/systemSettings/getDatabaseSize`).then((r) => {
				commit('setDatabaseInfo', r);
				return r;
			});
		},

		getSystemInfoSettings({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getSystemInfo').then((r) => {
				commit('setSystemInfoSettings', r);
				return r;
			});
		},

		saveSystemInfoSettings({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveSystemInfo',
				data: state.systemInfoSettings,
			});
		},

		getSystemStartupTime({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getStartupTime').then((r) => {
				commit('setSystemStartupTime', r.startupTime);
				return r;
			});
		},

		getAuditEventTypes({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getAuditEventAlarmLevels').then(
				(r) => {
					commit('setAuditEventTypes', r);
					return r;
				}
			);
		},

		saveAuditEventTypes({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveAuditEventAlarmLevels',
				data: state.auditEventTypes,
			});
		},

		getSystemEventTypes({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getSystemEventAlarmLevels').then(
				(r) => {
					commit('setSystemEventTypes', r);
					return r;
				}
			);
		},

		saveSystemEventTypes({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveSystemEventAlarmLevels',
				data: state.systemEventTypes,
			});
		},

		getEmailSettings({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getEmail').then((r) => {
				commit('setEmailSettings', r);
				return r;
			});
		},

		saveEmailSettings({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveEmail',
				data: state.emailSettings,
			});
		},

		sendTestEmail({ dispatch }) {
			return dispatch('requestGet', '/systemSettings/sendTestEmail');
		},

		getHttpSettings({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getHttp').then((r) => {
				commit('setHttpSettings', r);
				return r;
			});
		},

		saveHttpSettings({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveHttp',
				data: state.httpSettings,
			});
		},

		getMiscSettings({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getMisc').then((r) => {
				commit('setMiscSettings', r);
				return r;
			});
		},

		saveMiscSettings({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveMisc',
				data: state.miscSettings,
			});
		},

		getSmsDomainSettings({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getSMSDomain').then((r) => {
				commit('setSmsDomainSettings', r);
				return r;
			});
		},

		saveSmsDomainSettings({ state, dispatch }) {
			return dispatch('requestPost', {
				url: `/systemSettings/saveSMSDomain`,
				data: {'domainName':state.smsDomainSettings},
			});
		},

		getAmchartsSettings({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getAggregateSettings').then((r) => {
				commit('setAmchartsSettings', r);
				return r;
			});
		},

		saveAmchartsSettings({ state, dispatch }) {
			return dispatch('requestPost', {
				url: '/systemSettings/saveAggregateSettings',
				data: state.amchartsSettings,
			});
		},

		getScadaConfiguration({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getScadaConfig').then((r) => {
				commit('setScadaConfig', r);
				return r;
			});
		},

		getDefaultLoggingType({ commit, dispatch }) {
			return dispatch('requestGet', '/systemSettings/getDefaultLoggingType').then((r) => {
				commit('setDefaultLoggingType', r.defaultLoggingType);
				return r.defaultLoggingType;
			});
			
		},

		saveDefaultLoggingType({ state, dispatch }) {
			return dispatch('requestPost', {
				url: `/systemSettings/saveDefaultLoggingType/${state.defaultLoggingType}`,
				data: null,
			});
		},

		purgeData(context) {
			return new Promise((resolve, reject) => {
				axios
					.get(`${context.state.systemSettingsApiUrl}/purgeData`, {
						timeout: 5000,
						useCredentials: true,
						credentials: 'same-origin',
					})
					.then((response) => {
						if (response.status == 200) {
							resolve(true);
						} else {
							reject(false);
						}
					})
					.catch((err) => {
						reject(err);
					});
			});
		},

		configurationEqual(ctx, objects) {
			return new Promise((resolve, reject) => {
				const keys1 = Object.keys(objects.object1);
				const keys2 = Object.keys(objects.object2);
				if (keys1.length !== keys2.length) {
					resolve(false);
				}
				for (let key of keys1) {
					if (objects.object1[key] !== objects.object2[key]) {
						resolve(false);
					}
				}
				resolve(true);
			});
		},
	},
	getters: {},
};
export default storeSystemSettings;
