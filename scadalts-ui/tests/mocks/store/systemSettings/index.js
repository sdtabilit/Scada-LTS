/**
 * @author Radoslaw Jajko
 * 
 */
const storeSystemSettings = {
    state: {
        systemSettingsApiUrl: 'MOCK',
        databaseType: undefined,
        databaseInfo: undefined,
        systemEventTypes: undefined,
        auditEventTypes: undefined,
        systemInfoSettings: {
            instanceDescription: "Scada-LTS 2.3.3",
            language: "en",
            newVersionNotificationLevel: "0"
        },
        emailSettings: {
            auth: false,
            contentType: 1,
            from: "ScadaLTS",
            host: "",
            name: "",
            password: "",
            port: 25,
            tls: false,
            username: ""
        },
        httpSettings: {
            useProxy: false,
            port: 16,
            host: "",
            username: "",
            password: ""
        },
        miscSettings: {
            eventPurgePeriodType: 6,
            eventPurgePeriods: 2,
            futureDateLimitPeriodType: 3,
            futureDateLimitPeriods: 24,
            groveLogging: false,
            reportPurgePeriodType: 6,
            reportPurgePeriods: 2,
            uiPerformance: 2000
        }
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
        }
    },
    actions: {
        getDatabaseType() {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveDatabaseType(context, databaseType) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        getAllSettings(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        getDatabaseSize(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveEmailSettings(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveMiscSettings(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveHttpSettings(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveSystemEventTypes(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveAuditEventTypes(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        purgeData(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        },
        saveSystemInfoSettings(context) {
            return new Promise((resolve, reject) => {
                resolve()
            })
        }
    },
    getters: {

    }
}
export default storeSystemSettings