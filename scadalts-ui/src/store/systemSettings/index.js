/**
 * @author Radoslaw Jajko
 * 
 */

import axios from "axios";

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
        systemStartupTime: undefined,
        schemaVersion: undefined,
        scadaConfig: undefined
    },
    mutations: {
        setDatabaseType(state, databaseType) {
            state.databaseType = databaseType;
        },
        setDatabaseInfo(state, databaseInfo) {
            state.databaseInfo = databaseInfo;
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
        setSystemStartupTime(state, startupTime) {
            state.systemStartupTime = new Date(Number(startupTime));
        },
        setSchemaVersion(state, schemaVersion) {
            state.schemaVersion = schemaVersion;
        },
        setScadaConfig(state, scadaConfig) {
            state.scadaConfig = scadaConfig;
        }
    },
    actions: {
        getDatabaseType() {
            return new Promise((resolve, reject) => {
                axios.get(`${this.systemSettingsApiUrl}/getDatabaseType`).then(response => {
                    console.debug(response)
                    resolve(response.data)
                }).catch(err => {
                    reject()
                })
            })
        },
        saveDatabaseType(context, databaseType) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/saveDatabaseType/${databaseType}`).then(response => {
                    if (response.status == 200) { resolve(true) }
                    resolve(false)
                }).catch(err => {
                    reject()
                })
            })
        },
        getSchemaVersion(context) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/getSchemaVersion`,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            context.commit('setSchemaVersion', response.data.schemaVersion);
                        }
                        resolve(true)
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        getSystemStartupTime(context) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/getStartupTime`,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            context.commit('setSystemStartupTime', response.data.startupTime);
                        }
                        resolve(true)
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        getAllSettings(context) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/getSettings`,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            context.commit('setDatabaseType', response.data.databaseType);
                            context.commit('setSystemEventTypes', response.data.systemEventTypes);
                            context.commit('setAuditEventTypes', response.data.auditEventTypes);
                            context.commit('setSystemInfoSettings', response.data.systemInfoSettings);
                            context.commit('setEmailSettings', response.data.emailSettings);
                            context.commit('setHttpSettings', response.data.httpSettings);
                            context.commit('setMiscSettings', response.data.miscSettings);
                            context.commit('setScadaConfig', response.data.scadaConfig);
                        }
                        resolve(true)
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        getDatabaseSize(context) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/getDatabaseSize`,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            context.commit('setDatabaseInfo', response.data);
                        }
                        resolve(true)
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        saveEmailSettings(context) {
            return new Promise((resolve, reject) => {
                axios.post(`${context.state.systemSettingsApiUrl}/saveEmail`, context.state.emailSettings,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        saveMiscSettings(context) {
            return new Promise((resolve, reject) => {
                axios.post(`${context.state.systemSettingsApiUrl}/saveMisc`, context.state.miscSettings,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        saveHttpSettings(context) {
            return new Promise((resolve, reject) => {
                axios.post(`${context.state.systemSettingsApiUrl}/saveHttp`, context.state.httpSettings,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        saveSystemEventTypes(context) {
            return new Promise((resolve, reject) => {
                axios.post(`${context.state.systemSettingsApiUrl}/saveSystemEventAlarmLevels`, context.state.systemEventTypes,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        saveAuditEventTypes(context) {
            return new Promise((resolve, reject) => {
                axios.post(`${context.state.systemSettingsApiUrl}/saveAuditEventAlarmLevels`, context.state.auditEventTypes,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        sendTestEmail(context) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/sendTestEmail`,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(response.data)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        purgeData(context) {
            return new Promise((resolve, reject) => {
                axios.get(`${context.state.systemSettingsApiUrl}/purgeData`,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        },
        saveSystemInfoSettings(context) {
            return new Promise((resolve, reject) => {
                axios.post(`${context.state.systemSettingsApiUrl}/saveSystemInfo`, context.state.systemInfoSettings,
                    { timeout: 5000, useCredentials: true, credentials: 'same-origin' }).then(response => {
                        if (response.status == 200) {
                            resolve(true)
                        } else {
                            reject(false)
                        }
                    }).catch(err => {
                        reject(err)
                    })
            })
        }
    },
    getters: {

    }
}
export default storeSystemSettings