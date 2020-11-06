
/**
 * @author grzegorz.bylica@abilit.eu
 */

import axios from 'axios'

const storeRefreshViews = {
    state: {
        msgErr: '',
    },
    mutations: {
        ERR(state, msgErr) {
            state.msgErr = msgErr
        }
    },
    actions: {
        checkViewModificationTime({commit, state}, id) {
            return new Promise((resolve, reject) => {
                axios.get(`./api/view/getModificationTime/${id}`, {timeout: state.timeRefreshEpoch}).then(res => {
                    resolve(res)
                }).catch((err) => {
                    commit('ERR',err)
                    reject(err)
                })
            })
        }
    },
    getters: {
        msgErrorRefreshViews: state => state.msgErr
    }
}
export default storeRefreshViews
