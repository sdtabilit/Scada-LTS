import 'bootstrap/dist/css/bootstrap.min.css';

import Vue from 'vue';
import App from './apps/App.vue';
import router from './router';
import store from './store';

import VueCookie from 'vue-cookie';
import VueLogger from 'vuejs-logger';
import VueDayjs from 'vue-dayjs-plugin';

import Test from './components/Test';
import IsAlive from './components/graphical_views/IsAlive';
import CMP from './components/graphical_views/cmp/CMP';
import SimpleComponentSVG from './components/graphical_views/SimpleComponentSVG';
import ExportImportPointHierarchy from './components/point_hierarchy/ExportImportPointHierarchy';
import SleepAndReactivationDS from './components/forms/SleepAndReactivationDS';
import WatchListJsonChart from './components/watch_list/WatchListJsonChart';
import VueLodash from 'vue-lodash';

import LineChartComponent from './components/amcharts/LineChartComponent';
import RangeChartComponent from './components/amcharts/RangeChartComponent';

import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import {
	faCoffee,
	faTimes,
	faBars,
	faBell,
	faFileMedicalAlt,
	faInfo,
	faListAlt,
	faCogs,
} from '@fortawesome/free-solid-svg-icons';
import i18n from './i18n';
import LiveAlarms from './components/graphical_views/AlarmsComponent';
import RefreshView from '@/components/graphical_views/RefreshView';
import SMSDomain from '@/components/forms/SMSDomain';
import vuetify from './plugins/vuetify';
import 'roboto-fontface/css/roboto/roboto-fontface.css';
import '@mdi/font/css/materialdesignicons.css';
import * as uiv from 'uiv';
import svgJS from './plugins/svg';

library.add(
	faCoffee,
	faTimes,
	faBars,
	faBell,
	faFileMedicalAlt,
	faInfo,
	faListAlt,
	faCogs
);

Vue.component('font-awesome-icon', FontAwesomeIcon);

const isProduction = process.env.NODE_ENV === 'production';

const options = {
	isEnabled: true,
	logLevel: isProduction ? 'error' : 'debug',
	stringifyArguments: false,
	showLogLevel: true,
	showMethodName: true,
	separator: '|',
	showConsoleColors: true,
};

Vue.use(VueLogger, options);

const optionsLodash = { name: 'lodash' };

Vue.use(VueLodash, optionsLodash);

Vue.use(VueCookie);
Vue.use(VueDayjs);

Vue.use(svgJS);

Vue.config.devtools = true;

new Vue({
	router,
	store,
	i18n,
	vuetify,
	render: (h) => h(App),
}).$mount('#app');

Vue.use(uiv);

if (window.document.getElementById('app-test') != undefined) {
	new Vue({
		render: (h) =>
			h(Test, {
				props: {
					plabel: window.document.getElementById('app-test').getAttribute('plabel'),
				},
			}),
	}).$mount('#app-test');
}

if (window.document.getElementById('app-isalive') != undefined) {
	const isAliveDom = document.getElementById('app-isalive');
	new Vue({
		store,
		render: (h) =>
			h(IsAlive, {
				props: {
					plabel: isAliveDom.getAttribute('plabel'),
					ptimeWarning: isAliveDom.getAttribute('ptime-warning'),
					ptimeError: isAliveDom.getAttribute('ptime-error'),
					ptimeRefresh: isAliveDom.getAttribute('ptime-refresh'),
					feedbackUrl: isAliveDom.getAttribute('feedback-url'),
				},
			}),
	}).$mount('#app-isalive');
}

for (let i = 0; i < 20; i++) {
	const cmpId = `app-cmp-${i}`;
	if (window.document.getElementById(cmpId) != undefined) {
		new Vue({
			vuetify,
			render: (h) =>
				h(CMP, {
					store,
					props: {
						pLabel: window.document.getElementById(cmpId).getAttribute('plabel'),
						pTimeRefresh: window.document
							.getElementById(cmpId)
							.getAttribute('ptimeRefresh'),
						pConfig: window.document.getElementById(cmpId).getAttribute('pconfig'),
						pxIdViewAndIdCmp: window.document
							.getElementById(cmpId)
							.getAttribute('pxIdViewAndIdCmp'),
					},
				}),
		}).$mount('#' + cmpId);
	}
}

if (window.document.getElementById('simple-component-svg') != undefined) {
	new Vue({
		render: (h) =>
			h(SimpleComponentSVG, {
				props: {
					pxidPoint: window.document
						.getElementById('simple-component-svg')
						.getAttribute('pxidPoint'),
					ptimeRefresh: window.document
						.getElementById('simple-component-svg')
						.getAttribute('ptimeRefresh'),
					plabel: window.document
						.getElementById('simple-component-svg')
						.getAttribute('plabel'),
					pvalue: window.document
						.getElementById('simple-component-svg')
						.getAttribute('pvalue'),
				},
			}),
	}).$mount('#simple-component-svg');
}

if (window.document.getElementById('sleep-reactivation-ds') != undefined) {
	new Vue({
		render: (h) => h(SleepAndReactivationDS),
	}).$mount('#sleep-reactivation-ds');
}

if (window.document.getElementById('sms-domain') != undefined) {
	new Vue({
		vuetify,
		render: (h) => h(SMSDomain),
	}).$mount('#sms-domain');
}

if (window.document.getElementById('export-import-ph') != undefined) {
	new Vue({
		render: (h) => h(ExportImportPointHierarchy),
	}).$mount('#export-import-ph');
}

if (window.document.getElementById('example-chart-cmp') != undefined) {
	new Vue({
		store,
		vuetify,
		i18n,
		render: (h) => h(WatchListJsonChart),
	}).$mount('#example-chart-cmp');
}

for (let x = 0; x < 10; x++) {
	const chartId = `chart-line-${x}`;
	const el = window.document.getElementById(chartId);
	if (el != undefined) {
		new Vue({
			render: (h) =>
				h(LineChartComponent, {
					props: {
						pointIds: el.getAttribute('point-ids'),
						useXid: el.getAttribute('use-xid') !== null,
						separateAxis: el.getAttribute('separate-axes') !== null,
						stepLine: el.getAttribute('step-line') !== null,
						startDate: el.getAttribute('start-date'),
						endDate: el.getAttribute('end-date'),
						refreshRate: el.getAttribute('refresh-rate'),
						width: el.getAttribute('width') !== null ? el.getAttribute('width') : '500',
						height: el.getAttribute('height') !== null ? el.getAttribute('height') : '400',
						color: el.getAttribute('color'),
						strokeWidth: Number(el.getAttribute('stroke-width')),
						aggregation: Number(el.getAttribute('aggregation')),
						showScrollbar: el.getAttribute('show-scrollbar') !== null,
						showLegend: el.getAttribute('show-legned') !== null,
						showBullets: el.getAttribute('show-bullets') !== null,
						showExportMenu: el.getAttribute('show-export-menu') !== null,
						smoothLine: Number(el.getAttribute('smooth-line')),
						serverValuesLimit: Number(el.getAttribute('server-values-limit')),
						serverLimitFactor: Number(el.getAttribute('server-limit-factor')),
					},
				}),
		}).$mount(`#${chartId}`);
	}
}

for (let x = 0; x < 10; x++) {
	const chartId = `chart-range-${x}`;
	const el = window.document.getElementById(chartId);
	if (el != undefined) {
		new Vue({
			store,
			vuetify,
			render: (h) =>
				h(RangeChartComponent, {
					props: {
						pointIds: el.getAttribute('point-ids'),
						useXid: el.getAttribute('use-xid') !== null,
						separateAxis: el.getAttribute('separate-axes') !== null,
						stepLine: el.getAttribute('step-line') !== null,
						aggregation: Number(el.getAttribute('aggregation')),
						strokeWidth: Number(el.getAttribute('stroke-width')),
						showBullets: el.getAttribute('show-bullets') !== null,
						showExportMenu: el.getAttribute('show-export-menu') !== null,
						smoothLine: Number(el.getAttribute('smooth-line')),
						width: el.getAttribute('width') !== null ? el.getAttribute('width') : '500',
						height: el.getAttribute('height') !== null ? el.getAttribute('height') : '400',
						color: el.getAttribute('color'),
						serverValuesLimit: Number(el.getAttribute('server-values-limit')),
						serverLimitFactor: Number(el.getAttribute('server-limit-factor')),
					},
				}),
		}).$mount(`#${chartId}`);
	}
}

if (window.document.getElementById('refresh-view') != undefined) {
	new Vue({
		store,
		render: (h) =>
			h(RefreshView, {
				props: {
					ptimeToCheckRefresh: window.document
						.getElementById('refresh-view')
						.getAttribute('ptimeToCheckRefresh'),
					pviewId: window.document.getElementById('refresh-view').getAttribute('pviewId'),
				},
			}),
	}).$mount('#refresh-view');
}

if (window.document.getElementById('live-alarms') != undefined) {
	console.log(
		`test+ ${window.document
			.getElementById('live-alarms')
			.getAttribute('show-acknowledge-btn')}`
	);

	new Vue({
		store,
		vuetify,
		render: (h) =>
			h(LiveAlarms, {
				props: {
					pShowAcknowledgeBtn: window.document
						.getElementById('live-alarms')
						.getAttribute('show-acknowledge-btn'),
					pShowMainToolbar: window.document
						.getElementById('live-alarms')
						.getAttribute('show-main-toolbar'),
					pShowSelectToAcknowledge: window.document
						.getElementById('live-alarms')
						.getAttribute('show-select-to-acknowledge'),
					pShowPagination: window.document
						.getElementById('live-alarms')
						.getAttribute('show-pagination'),
					pMaximumNumbersOfRows: window.document
						.getElementById('live-alarms')
						.getAttribute('max-number-of-rows'),
				},
			}),
	}).$mount('#live-alarms');
}
