import axios from "axios";

const storeNotificationList = {
    state: {

    },
    mutations: {

    },
    actions: {
        getMailingLists() {
            return new Promise((resolve, reject) => {
                axios.get(`./api/mailingList/getMailingLists`).then(r => {
                    resolve(r.data);
                }).catch(e => {
                    reject(e);
                })
            })
        },
        getMailingListPlcNotification({commit}, mailingListId) {
            return new Promise((resolve, reject) => {
                axios.get(`./api/alarms/notification/getMailingList/${mailingListId}`).then(r => {
                    resolve(r.data);
                }).catch(e => {
                    reject(e);
                })
            })
        },
        postMailingListPlcNotification({commit}, mailingList) {
            return new Promise((resolve, reject) => {
                axios.post(`./api/alarms/notification/setMailingList`, mailingList).then(r => {
                    resolve(r.data);
                }).catch(e => {
                    reject(e);
                })
            })
        },
        updateMailingListPlcNotification({commit}, mailingList) {
            return new Promise((resolve, reject) => {
                axios.put(`./api/alarms/notification/updateMailingList`, mailingList).then(r => {
                    resolve(r.data);
                }).catch(e => {
                    reject(e);
                })
            })
        },
        getDataPointList() {
            return new Promise((resolve, reject) => {
                axios.get(`./api/datapoint/getAll`).then(r => {
                    resolve(r.data);
                }).catch(e => {
                    reject(e);
                })
            })
        },
    },
    getters: {

    }
}
export default storeNotificationList;