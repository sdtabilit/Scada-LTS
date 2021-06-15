import i18n from '@/i18n';
import Axios from 'axios';
import { reject } from 'core-js/fn/promise';
import { datasourceApiMocks, datasourceDetailsMocks } from './mocks/datasourceapi';

/**
 * Data Source received form REST API
 * @typedef {Object} DataSourceAPI
 * @property {Number} 	id
 * @property {String} 	xid
 * @property {Boolean} 	enabled
 * @property {String} 	name
 * @property {Number} 	type
 * @property {String} 	connection
 * @property {String} 	description
 * @property {Number} 	activeEvents
 * @property {Boolean}	loaded
 * @property {Object}	datapoints
 */

const ds = {
	state: {
		datasourcesApiUrl: './api/datasource',
		requestOptions: {
			timeout: 5000,
			useCredentials: true,
			credentials: 'same-origin',
		},

		dataSources: new Map()
			.set(1,"virtualdatasource")
			.set(5, "snmpdatasource"),

	},
	mutations: {},
	actions: {

		/**
		 * Get All DataSources
		 * 
		 * @param {*} param0 
		 * @returns {Promise<DataSourceAPI>} DataSource JSON from API
		 */
		getDataSources({ dispatch }) {
			return new Promise((resolve, reject) => {
				setTimeout(() => {
					resolve(datasourceApiMocks)
				}, 2000);
				
			});
		},

		/**
		 * Get DataSource Details
		 * 
		 * Details are dependend on the DataSource Type. 
		 * The logic to parse that data should be written 
		 * in speficic datasource component.
		 * 
		 * @param {*} param0 
		 * @param {Number} dataSourceId - ID number of DataSource
		 * @returns 
		 */
		fetchDataSourceDetails({dispatch}, dataSourceId) {
			return new Promise((resolve) => {
				setTimeout(() => {
					resolve(datasourceDetailsMocks[dataSourceId]);
				}, 1000);
			})
		},

		fetchDataPointsForDS({dispatch}, dataSourceId) {
			return new Promise((resolve, reject) => {
				setTimeout(() => {
					const data = [
						{
							enabled: false,
							name: 'DP-01',
							type: 'Binary',
							desc: 'Additional Descr',
							xid: 'DP_0121323',
						},
						{
							enabled: true,
							name: 'DP-02',
							type: 'Numeric',
							desc: 'Descr',
							xid: 'DP_0121314',
						},
					];
					resolve(data);
				}, 2000)
			});
		},

		/**
		 * Create Data Source
		 * 
		 * Send a POST request to the Core aplication REST API to create a new
		 * DataSource. Based on the typeID of datasource it should create a 
		 * valid DS Type and as a response sould be received DataSourceAPI object.
		 * It sould contain a new generated ID.
		 * 
		 * --- MOCKED ---
		 * 
		 * @param {*} param0 
		 * @param {Object} datasource - DataSource object from Creator component.
		 * @returns {Promise<DataSourceAPI>} DataSource JSON from API
		 */
		createDataSource({dispatch}, datasource) {
			/* Mocking TODO: real method*/
			return new Promise((resolve, reject) => {
				setTimeout(() => {
					const response = {
						id: 10,
						xid: datasource.xid,
						enabled: false,
						name: datasource.name,
						type: datasource.type,
						connection: `${datasource.updatePeriod} ${datasource.updatePeriodType}`,
						description: '',
						activeEvents: null,
						loaded: false,
						datapoints: [],
					}
					resolve(response);
				}, 3000);
			})
		},


		/**
		 * Update Data Source
		 * 
		 * Send a PUT request to the Core aplication REST API to update existing
		 * DataSource. 
		 * 
		 * --- MOCKED ---
		 * 
		 * @param {*} param0 
		 * @param {Object} datasource - DataSource object from Creator component.
		 * @returns 
		 */
		 updateDataSource({dispatch}, datasource) {
			/* Mocking TODO: real method*/
			return new Promise((resolve, reject) => {
				setTimeout(() => {
					console.log(datasource);
					const response = {
						status: "OK",
					}
					resolve(response);
				}, 1000);
			})
		},

		getAllDataSources(context) {
			return new Promise((resolve, reject) => {
				Axios.get(
					`${context.state.datasourcesApiUrl}/getAll`,
					context.state.requestOptions
				)
					.then((r) => {
						if (r.status === 200) {
							resolve(r.data);
						} else {
							reject(false);
						}
					})
					.catch((error) => {
						console.error(error);
						reject(false);
					});
			});
		},

		getAllPlcDataSources(context) {
			return new Promise((resolve, reject) => {
				Axios.get(
					`${context.state.datasourcesApiUrl}/getAllPlc`,
					context.state.requestOptions
				)
					.then((r) => {
						if (r.status === 200) {
							resolve(r.data);
						} else {
							reject(false);
						}
					})
					.catch((error) => {
						console.error(error);
						reject(false);
					});
			});
		},
	},

	getters: {
		dataSourceList(state) {
			let datasources = [];
			state.dataSources.forEach(dsType => {
                datasources.push({
                    value: `${dsType}`,
                    text: i18n.t(`datasource.type.${dsType}`)
                });
            });
            return datasources;
		},

		dataSourceTypeId:(state) => (datasourceType) => {
			for (let [key, value] of state.dataSources.entries()) {
				if(value === datasourceType) {
					return key;
				}
			}
			return -1;
		}
	},
};
export default ds;
