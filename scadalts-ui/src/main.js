import 'bootstrap/dist/css/bootstrap.min.css'

import Vue from 'vue'
import App from './apps/App.vue'
import router from './router'
import store from './store'
import * as uiv from 'uiv'
import VueCookie from 'vue-cookie'
import VueLogger from 'vuejs-logger'

import Test from './components/Test'
import IsAlive from './components/graphical_views/IsAlive'
import CMP from './components/graphical_views/cmp/CMP'
import SimpleComponentSVG from './components/graphical_views/SimpleComponentSVG'
import ExportImportPointHierarchy from './components/point_hierarchy/ExportImportPointHierarchy'
import SleepAndReactivationDS from './components/forms/SleepAndReactivationDS'
import WatchListChartWidget from './components/watch_list/WatchListChartWidget'
import VueLodash from 'vue-lodash'

import StepLineChartComponent from './components/amcharts/StepLineChartComponent'
import LineChartComponent from './components/amcharts/LineChartComponent'

import Verte from 'verte';
import 'verte/dist/verte.css';

import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import {faCoffee, faTimes, faBars, faBell, faFileMedicalAlt, faInfo, faListAlt, faCogs} from '@fortawesome/free-solid-svg-icons'
import i18n from './i18n'
import LiveAlarms from './components/graphical_views/AlarmsComponent'
import RefreshView from "@/components/graphical_views/RefreshView"

library.add(faCoffee, faTimes, faBars, faBell, faFileMedicalAlt, faInfo, faListAlt, faCogs)

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.component(Verte.name, Verte);

const isProduction = process.env.NODE_ENV === 'production';

const options = {
  isEnabled: true,
  logLevel: isProduction ? 'error' : 'debug',
  stringifyArguments: false,
  showLogLevel: true,
  showMethodName: true,
  separator: '|',
  showConsoleColors: true
};

Vue.use(VueLogger, options);

const optionsLodash = { name: 'lodash' }

Vue.use(VueLodash, optionsLodash)

Vue.use(uiv)
Vue.use(VueCookie)

Vue.config.devtools = true

new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')

if (window.document.getElementById('app-test') != undefined) {
  new Vue({
    render: h => h(Test,
      {
        props:
          { plabel: window.document.getElementById('app-test').getAttribute('plabel') }
      })
  }).$mount('#app-test')
}

if (window.document.getElementById('app-isalive') != undefined) {
  new Vue({
    store,
    render: h => h(IsAlive,
      {
        props:
        {
          plabel: window.document.getElementById('app-isalive').getAttribute('plabel'),
          ptimeWarning: window.document.getElementById('app-isalive').getAttribute('ptime-warning'),
          ptimeError: window.document.getElementById('app-isalive').getAttribute('ptime-error'),
          ptimeRefresh: window.document.getElementById('app-isalive').getAttribute('ptime-refresh'),
        }
      })
  }).$mount('#app-isalive')
}

for (let i = 0; i < 20; i++) {
  const cmpId = `app-cmp-${i}`
  if (window.document.getElementById(cmpId) != undefined) {
    new Vue({
      render: h => h(CMP,
        {
          store,
          props:
          {
            pLabel: window.document.getElementById(cmpId).getAttribute('plabel'),
            pTimeRefresh: window.document.getElementById(cmpId).getAttribute('ptimeRefresh'),
            pConfig: window.document.getElementById(cmpId).getAttribute('pconfig'),
            pxIdViewAndIdCmp: window.document.getElementById(cmpId).getAttribute('pxIdViewAndIdCmp')
          }
        })
    }).$mount('#' + cmpId)
  }
}

if (window.document.getElementById('simple-component-svg') != undefined) {
  new Vue({
    render: h => h(SimpleComponentSVG, {
      props:
      {
        pxidPoint: window.document.getElementById('simple-component-svg').getAttribute('pxidPoint'),
        ptimeRefresh: window.document.getElementById('simple-component-svg').getAttribute('ptimeRefresh'),
        plabel: window.document.getElementById('simple-component-svg').getAttribute('plabel'),
        pvalue: window.document.getElementById('simple-component-svg').getAttribute('pvalue')
      }
    })
  }).$mount('#simple-component-svg')
}

if (window.document.getElementById('sleep-reactivation-ds') != undefined) {
  new Vue({
    render: h => h(SleepAndReactivationDS)
  }).$mount('#sleep-reactivation-ds')
}

if (window.document.getElementById('export-import-ph') != undefined) {
  new Vue({
    render: h => h(ExportImportPointHierarchy)
  }).$mount('#export-import-ph')
}

if (window.document.getElementById('example-chart-cmp') != undefined) {
  new Vue({
    store,
    render: h => h(WatchListChartWidget)
  }).$mount('#example-chart-cmp')
}

for (let x = 0; x < 10; x++) {
  const chartId = `chart-step-line-${x}`
  if (window.document.getElementById(chartId) != undefined) {
    new Vue({
      render: h => h(StepLineChartComponent, {
        props:
        {
          pointId: window.document.getElementById(chartId).getAttribute('point-id'),
          pointXid: window.document.getElementById(chartId).getAttribute('point-xid'),
          color: window.document.getElementById(chartId).getAttribute('color'),
          label: window.document.getElementById(chartId).getAttribute('label'),
          startDate: window.document.getElementById(chartId).getAttribute('start-date'),
          endDate: window.document.getElementById(chartId).getAttribute('end-date'),
          refreshRate: window.document.getElementById(chartId).getAttribute('refresh-rate'),
          width: window.document.getElementById(chartId).getAttribute('width'),
          height: window.document.getElementById(chartId).getAttribute('height'),
          polylineStep: window.document.getElementById(chartId).getAttribute('polyline-step'),
          rangeValue: window.document.getElementById(chartId).getAttribute('range-value'),
          rangeColor: window.document.getElementById(chartId).getAttribute('range-color'),
          rangeLabel: window.document.getElementById(chartId).getAttribute('range-label'),
          showScrollbarX: window.document.getElementById(chartId).getAttribute('show-scrollbar-x'),
          showScrollbarY: window.document.getElementById(chartId).getAttribute('show-scrollbar-y'),
          showLegend: window.document.getElementById(chartId).getAttribute('show-legned'),
        }
      })
    }).$mount(`#${chartId}`)
  }
}

for (let x = 0; x < 10; x++) {
  const chartId = `chart-line-${x}`
  if (window.document.getElementById(chartId) != undefined) {
    new Vue({
      render: h => h(LineChartComponent, {
        props:
        {
          pointId: window.document.getElementById(chartId).getAttribute('point-id'),
          pointXid: window.document.getElementById(chartId).getAttribute('point-xid'),
          color: window.document.getElementById(chartId).getAttribute('color'),
          label: window.document.getElementById(chartId).getAttribute('label'),
          startDate: window.document.getElementById(chartId).getAttribute('start-date'),
          endDate: window.document.getElementById(chartId).getAttribute('end-date'),
          refreshRate: window.document.getElementById(chartId).getAttribute('refresh-rate'),
          width: window.document.getElementById(chartId).getAttribute('width'),
          height: window.document.getElementById(chartId).getAttribute('height'),
          polylineStep: window.document.getElementById(chartId).getAttribute('polyline-step'),
          rangeValue: window.document.getElementById(chartId).getAttribute('range-value'),
          rangeColor: window.document.getElementById(chartId).getAttribute('range-color'),
          rangeLabel: window.document.getElementById(chartId).getAttribute('range-label'),
          showScrollbarX: window.document.getElementById(chartId).getAttribute('show-scrollbar-x'),
          showScrollbarY: window.document.getElementById(chartId).getAttribute('show-scrollbar-y'),
          showLegend: window.document.getElementById(chartId).getAttribute('show-legned'),
        }
      })
    }).$mount(`#${chartId}`)
  }
}

if (window.document.getElementById('refresh-view') != undefined) {
  new Vue({
    store,
    render: h => h(RefreshView, {
      props: {
        ptimeToCheckRefresh: window.document.getElementById('refresh-view').getAttribute('ptimeToCheckRefresh'),
        pviewId: window.document.getElementById('refresh-view').getAttribute('pviewId')
      }
    })
  }).$mount('#refresh-view')
}

if (window.document.getElementById('live-alarms') != undefined) {

  console.log(`test+ ${window.document.getElementById('live-alarms').getAttribute('show-acknowledge-btn')}`)

  new Vue({
    store,
    render: h => h(LiveAlarms, {
      props:
          {
            pShowAcknowledgeBtn: window.document.getElementById('live-alarms').getAttribute('show-acknowledge-btn'),
            pShowMainToolbar: window.document.getElementById('live-alarms').getAttribute('show-main-toolbar'),
            pShowSelectToAcknowledge: window.document.getElementById('live-alarms').getAttribute('show-select-to-acknowledge'),
            pShowPagination: window.document.getElementById('live-alarms').getAttribute('show-pagination'),
            pMaximumNumbersOfRows: window.document.getElementById('live-alarms').getAttribute('max-number-of-rows')
          }
    })
  }).$mount('#live-alarms')

}

