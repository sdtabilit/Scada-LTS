import Vue from 'vue'
import Router from 'vue-router'
import Alarms from './views/Alarms'
import About from './views/About'
import HistoricalAlarms from "./views/HistoricalAlarms"
import SystemSettings from "./views/SystemSettings"

Vue.use(Router)

export default new Router({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'home',
      component: Alarms
    },
    {
      path: '/about',
      name: 'about',
      component: About
    },
    {
      path: '/alarms',
      name: 'alarms',
      component: Alarms
    },
    {
      path: '/historical-alarms',
      name: 'historical-alarms',
      component: HistoricalAlarms
    },
    {
      path: '/system-settings',
      name: 'system-settings',
      component: SystemSettings
    },
    {
      path: '/example-ph',
      name: 'example-ph',
      component: () => import(/* webpackChunkName: "ph" */ './views/components/ExampleExportImportPointHierarchy.vue')
    },
    {
      path: '/example-test',
      name: 'example-test',
      component: () => import(/* webpackChunkName: "test-component" */ './views/components/ExampleTest.vue')
    },
    {
      path: '/example-isalive',
      name: 'example-isalive',
      component: () => import(/* webpackChunkName: "isalive-component" */ './views/components/ExampleIsAlive.vue')
    },
    {
      path: '/example-simple-component-svg',
      name: 'example-simple-component-svg',
      component: () => import(/* webpackChunkName: "simple-component-svg" */ './views/components/ExampleSimpleComponentSVG.vue')
    },
    {
      path: '/example-sleep-and-reactivation-ds',
      name: 'example-sleep-and-reactivation-ds',
      component: () => import(/* webpackChunkName: "sleep-and-reactivation-ds-component" */ './views/components/ExampleSleepAndReactivationDS.vue')
    },
    {
      path: '/example-cmp',
      name: 'example-cmp',
      component: () => import(/* webpackChunkName: "cmp-component" */ './views/components/ExampleCmp.vue')
    },
    {
      path: '/example-chart-cmp',
      name: 'example-chart-cmp',
      component: () => import(/* webpackChunkName: "example-chart-cmp" */ './views/components/ExampleChartCmp.vue')
    },
    {
      path: '/example-step-line-chart-cmp',
      name: 'example-step-line-chart-cmp',
      component: () => import(/* webpackChunkName: "step-line-chart-component" */ './views/components/ExampleStepLineChartCmp.vue')
    },
    {
      path: '/example-live-alarms',
      name: 'example-live-alarms',
      component: () => import(/* webpackChunkName: "live-alarms-component" */ './views/components/ExampleLiveAlarms.vue')
    },
  ]
})
