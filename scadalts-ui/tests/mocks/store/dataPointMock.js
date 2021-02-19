import { chartRenderersTemplates, eventRenderersTemplates } from '../../../src/store/dataPoint/templates';

export const dataPoint = {
	state: {
		chartRenderersTemplates: chartRenderersTemplates,
        eventRenderersTemplates: eventRenderersTemplates,

		textRenderesList: [
			{ id: 0, label: 'Analog' },
			{ id: 1, label: 'Binary' },
			{ id: 2, label: 'Multistate' },
			{ id: 3, label: 'Plain' },
			{ id: 4, label: 'Range' },
			{ id: 5, label: 'Time' },
		],

        loggingTypeList: [
			{ id: 1, type: 'ON_CHANGE', label: 'Change' },
            { id: 2, type: 'ALL', label: 'All data' },
            { id: 3, type: 'NONE', label: 'Do no log' },
            { id: 4, type: 'INTERVAL', label: 'Interval' },
            { id: 5, type: 'ON_TS_CHANGE', label: 'On Ts Change' },
		],
	},

	mutations: {},

	actions: {
		toggleDataPoint({ dispatch }, datapointId) {
			return new Promise((resolve, reject) => {
				resolve({ enabled: false });
			});
		},

        clearDataPointCache({ dispatch }, datapointId) {
			return new Promise((resolve, reject) => {
				resolve(true);
			});
		},
	},
};

export default dataPoint
