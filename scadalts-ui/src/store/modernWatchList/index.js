import Axios from "axios";

/**
 * Connected with WachListJsonChart.vue file
 * @author rjajko@softq.pl
 */
const modernWatchList = {
    state: {
        chartGroupData: true,
        chartGroupCount: 500,
        chartDefaultColors: [
            "#39B54A",
            "#69FF7D",
            "#166921",
            "#690C24",
            "#B53859",
            "#734FC1",
        ],
        chartActiveColor: 0,
        chartProperties: {
            type: "live",
            refreshRate: 10000,
            startDate: undefined,
            startTime: 1,
            startTimeMultiplier: "hour",
            endDate: undefined,
            endTime: new Date(),
            compareDataPoint: [null, null]
        },
        chartConfiguration: {
            legend: {},
            cursor: {},
            scrollbarX: { type: "Scrollbar" },
            xAxes: [{
                id: "dateAxis1",
                type: "DateAxis",
                dataFields: {
                    value: "date",
                },
                dateFormats: {
                    second: "HH:mm:ss",
                    minute: "HH:mm:ss",
                    hour: "HH:mm",
                    day: "MMM dd",
                },
                groupData: true,
                groupCount: 500,
            }, {
                id: "dateAxis2",
                type: "DateAxis",
                dataFields: {
                    value: "date2",
                },
                dateFormats: {
                    second: "HH:mm:ss",
                    minute: "HH:mm:ss",
                    hour: "HH:mm",
                    day: "MMM dd",
                },
                groupData: true,
                groupCount: 500,
            }],
            yAxes: [{
                id: "valueAxis1",
                type: "ValueAxis",
                dataFields: {
                    value: "01",
                },
            }, {
                id: "valueAxis2",
                type: "ValueAxis",
                syncWithAxis: "valueAxis1",
                dataFields: {
                    value: "01",
                },
            }, {
                id: "logAxis",
                type: "ValueAxis",
                logarithmic: true,
                dataFields: {
                    value: "01",
                },
            }, {
                id: "binAxis",
                type: "ValueAxis",
                dataFields: {
                    value: "01",
                },
                syncWithAxis: "valueAxis1",
                renderer: {
                    opposite: true,
                },
            }],
            series: [],
            exporting: {
                menu: {
                    align: "right",
                    verticalAlign: "top"
                },
                filePrefix: "Scada_Chart_" + String(new Date().getTime())
            },
        },
        chartSeriesTemplate: {
            id: undefined,
            name: undefined,
            type: undefined,
            stroke: undefined,
            fill: undefined,
            yAxis: undefined,
            tooltipText: undefined,
            strokeWidth: 3,
            fillOpacity: 0,
            xAxis: "dateAxis1",
            minBulletDistance: 15,
            tensionX: 1,
            startLocation: 0.5,
            dataFields: {
                dateX: "date",
                valueY: undefined
            },
            tooltip: {
                pointerOrientation: "vertical",
                background: {
                    fill: "#F00",
                    cornerRadius: 20,
                    strokeOpacity: 0,
                },
                label: {
                    minWidth: 40,
                    minHeight: 40,
                    textAlign: "middle",
                    textValign: "middle",
                }
            },
            bullets: [{
                type: "CircleBullet",
                circle: {
                    radius: 0,
                    strokeWidth: 2,
                }

            }]
        },
        chartSeriesConfiguration: [],
        datapointList: [],
        isChartLoaded: false,
        isDualAxis: false,
    },
    mutations: {
        /**
         * Setup Chart Group Settings
         * 
         * This allow to manage aggregation data on the chart.
         * This setting is enabled by default.
         * 
         * @param {*} state 
         * @param {Object} settings {chartGroupData, chartGroupCount}
         */
        setChartGroupSettings(state, settings) {
            console.debug("Vuex::Mutation::setChartGroupSettings")
            state.chartGroupData = settings.chartGroupData;
            state.chartGroupCount = settings.chartGroupCount;
        },
        saveChartGroupSettings(state) {
            console.debug("Vuex::Mutation::saveChartGroupSettings")
            let savedObject = {
                "chartGroupData": state.chartGroupData,
                "chartGroupCount": state.chartGroupCount,
            };
            localStorage.setItem(`MWL_ChartGroupConfig`, JSON.stringify(savedObject))
        },
        loadChartGroupSettings(state) {
            console.debug("Vuex::Mutation::loadChartGroupSettings")
            let loadedObject = JSON.parse(localStorage.getItem(`MWL_ChartGroupConfig`));
            if (loadedObject !== null) {
                state.chartGroupData = loadedObject.chartGroupData;
                state.chartGroupCount = loadedObject.chartGroupCount;
            }
        },
        /**
         * Chart Configuration Reset
         * 
         * Reset chart default values while creating a new chart or
         * when changing an active watchlist.
         * @param {*} state 
         */
        chartConfigurationReset(state) {
            console.debug("Vuex::Mutation::chartConfigurationReset")
            state.chartConfiguration = {
                legend: {},
                cursor: {},
                scrollbarX: { type: "Scrollbar" },
                xAxes: [{
                    id: "dateAxis1",
                    type: "DateAxis",
                    dataFields: {
                        value: "date",
                    },
                    dateFormats: {
                        second: "HH:mm:ss",
                        minute: "HH:mm:ss",
                        hour: "HH:mm",
                        day: "MMM dd",
                    },
                    groupData: state.chartGroupData,
                    groupCount: state.chartGroupCount,
                }, {
                    id: "dateAxis2",
                    type: "DateAxis",
                    dataFields: {
                        value: "date2",
                    },
                    dateFormats: {
                        second: "HH:mm:ss",
                        minute: "HH:mm:ss",
                        hour: "HH:mm",
                        day: "MMM dd",
                    },
                    groupData: state.chartGroupData,
                    groupCount: state.chartGroupCount,
                }],
                yAxes: [{
                    id: "valueAxis1",
                    type: "ValueAxis",
                    dataFields: {
                        value: "01",
                    },
                }, {
                    id: "valueAxis2",
                    type: "ValueAxis",
                    syncWithAxis: "valueAxis1",
                    dataFields: {
                        value: "01",
                    },
                }, {
                    id: "logAxis",
                    type: "ValueAxis",
                    logarithmic: true,
                    dataFields: {
                        value: "01",
                    },
                }, {
                    id: "binAxis",
                    type: "ValueAxis",
                    dataFields: {
                        value: "01",
                    },
                    syncWithAxis: "valueAxis1",
                    renderer: {
                        opposite: true,
                    },
                }],
                series: [],
                exporting: {
                    menu: {
                        align: "right",
                        verticalAlign: "top"
                    },
                    filePrefix: "Scada_Chart_" + String(new Date().getTime())
                },
            }
        },
        chartConfigurationSeriesPush(state, series) {
            console.debug("Vuex::Mutation::chartConfigurationSeriesPush")
            state.chartSeriesConfiguration.push(series);
        },
        /**
         * Chart Configuration Series Update
         * 
         * Update chart series configuration with a new ChartSeries object
         * 
         * @param {*} state 
         * @param {Object} series - new series configuration
         */
        chartConfigurationSeriesUpdate(state, series) {
            console.debug("Vuex::Mutation::chartConfigurationSeriesUpdate")
            state.chartSeriesConfiguration = series;
        },
        /**
         * Chart Configuration Series Apply
         * 
         * Apply chart series configuration to chart template to generate valid
         * chart series. This method inserts prepared series values to chart.
         * Use after ...seriesPush() or ...seriesUpdate() to apply this changest to chart.
         * 
         * @param {*} state 
         */
        chartConfigurationSeriesApply(state) {
            console.debug("Vuex::Mutation::chartConfigurationSeriesApply")
            state.chartConfiguration.series = JSON.parse(JSON.stringify(state.chartSeriesConfiguration));
        },
        /**
         * Chart Configuration Series Apply
         * 
         * Reset chart series configuration while creating a new chart or while
         * changing the active watchlist
         * 
         * @param {*} state 
         */
        chartConfigurationSeriesReset(state) {
            console.debug("Vuex::Mutation::chartConfigurationSeriesReset");
            state.chartSeriesConfiguration = [];
        },
        /**
         * Chart Save Configuration
         * 
         * Save specific chart configuration to local storage including
         * chart properties like chart type, start date, end date etc.
         * and chart series configuration for all datapionts included 
         * at selected watch list.
         * 
         * @param {*} state 
         * @param {*} watchlistName - name of the active watchlist to save configuration
         */
        chartSaveConfiguration(state, watchlistName) {
            console.debug("Vuex::Mutation::chartSaveConfiguration")
            let savedObject = {
                "chartProperties": state.chartProperties,
                "chartConfigurationSeries": state.chartSeriesConfiguration,
            };
            localStorage.setItem(`MWL_${watchlistName}`, JSON.stringify(savedObject))
        },
        /**
         * Chart Load Configuration
         * 
         * Load specific chart configuration from local storage including
         * chart properties like chart type, start date, end date etc.
         * and chart series configuration for all datapionts included 
         * at specified watch list.
         * 
         * @param {*} state 
         * @param {*} watchlistName - name of the watchlist for which load a configuration
         */
        chartLoadConfiguration(state, watchlistName) {
            console.debug("Vuex::Mutation::chartLoadConfiguration")
            let loadedObject = JSON.parse(localStorage.getItem(`MWL_${watchlistName}`));
            if (loadedObject !== null) {
                state.chartSeriesConfiguration = JSON.parse(localStorage.getItem(`MWL_${watchlistName}`)).chartConfigurationSeries;
                state.chartConfiguration.series = loadedObject.chartConfigurationSeries;
                state.chartProperties = loadedObject.chartProperties;
                state.isChartLoaded = true;
            } else {
                state.isChartLoaded = false;
                this.commit('chartConfigurationReset')
                this.commit('chartPropertiesReset')
            }
        },
        /**
         * Chart Properties Update
         * 
         * Set a new chart properties
         * 
         * @param {*} state 
         * @param {*} chartProperties - new chart properties
         */
        chartPropertiesUpdate(state, chartProperties) {
            console.debug("Vuex::Mutation::chartPropertiesUpdate")
            state.chartProperties = chartProperties;
        },
        /**
         * Chart Properties Reset
         * 
         * Reset chart properties to default values while creating a new chart or
         * when changing an active watchlist.
         * @param {*} state 
         */
        chartPropertiesReset(state) {
            console.debug("Vuex::Mutation::chartPropertiesReset")
            state.chartProperties = {
                type: "live",
                refreshRate: 10000,
                startDate: undefined,
                startTime: 1,
                startTimeMultiplier: "hour",
                endDate: undefined,
                endTime: new Date(),
                compareDataPoint: [null, null]
            };
        },
        /**
         * Chart Color Increment
         * 
         * Increment color to change a default color for next datapoint series
         * 
         * @param {*} state 
         */
        chartColorIncrement(state) {
            state.chartActiveColor++;
        },
        /**
         * Datapoints add point
         * 
         * Add a new datapoint to compare list.
         * 
         * @param {*} state 
         */
        datapointsAddPoint(state, point) {
            console.debug("Vuex::Mutation::datapointsAddPoint")
            state.datapointList.push(point);
        },
        /**
         * Datapoints Reset
         * 
         * Reset datapoints from last opened watchlist
         * 
         * @param {*} state 
         */
        datapointsReset(state) {
            console.debug("Vuex::Mutation::datapointsReset")
            state.datapointList = [];
        },
        toggleDualAxis(state) {
            console.debug("Vuex::Mutation::toggleDualAxis")
            state.isDualAxis = !state.isDualAxis;
        }
    },
    actions: {
        /**
         * Get Datapoint Info 
         * 
         * Load datapoint info from the server.
         * 
         * @param {*} context 
         * @param {*} pointId - PointID
         */
        getDatapointInfo(context, pointId) {
            console.debug("Vuex::Action::getDatapointInfo")
            return new Promise((resolve, reject) => {
                Axios.get(`./api/point_value/getValue/id/${pointId}`, {
                    timeout: 5000, usecredentials: true, credentials: "same-origin",
                }).then((resp) => {
                    let pointInfo = {
                        id: pointId,
                        xid: resp.data.xid,
                        name: resp.data.name,
                        type: resp.data.type,
                        startDate: null,
                        startTime: new Date(),
                        endDate: null,
                        endTime: new Date(),
                    };
                    context.commit('datapointsAddPoint', pointInfo)
                    resolve(true)
                }).catch(() => {
                    reject(false)
                })
            })
        },
        /**
         * Chart Initialize Datapoint Series
         * 
         * On the basis of datapoint info gathered from the server prepare
         * series configuration for that specific datapoint. 
         * 
         * @param {*} context 
         * @param {*} pointId PointID
         */
        chartInitDatapointSeries(context, pointId) {
            console.debug("Vuex::Action::chartInitDatapoint")
            return new Promise((resolve, reject) => {
                Axios.get(`./api/point_value/getValue/id/${pointId}`, {
                    timeout: 5000, usecredentials: true, credentials: "same-origin",
                }).then((resp) => {
                    let series = JSON.parse(JSON.stringify(context.state.chartSeriesTemplate));
                    series.id = `s${pointId}`;
                    series.stroke = context.state.chartDefaultColors[context.state.chartActiveColor % 6];
                    series.fill = context.state.chartDefaultColors[context.state.chartActiveColor % 6];
                    series.tooltipText = "{name}: [bold]{valueY}[/] " + resp.data.textRenderer.suffix;
                    series.name = resp.data.name;
                    series.dataFields.valueY = resp.data.name;
                    context.commit('chartColorIncrement');

                    if (resp.data.type == "NumericValue") {
                        series.yAxis = "valueAxis1";
                        series.type = "LineSeries";
                    } else if (resp.data.type == "BinaryValue") {
                        series.yAxis = "binAxis";
                        series.type = "StepLineSeries";
                    }
                    if (context.state.chartSeriesConfiguration.type == "compare" && !context.state.isDualAxis) {
                        context.commit('toggleDualAxis');
                        series.xAxis = "dateAxis2";
                        series.dataFields.dateX = "date2";
                    }

                    context.commit('chartConfigurationSeriesPush', JSON.parse(JSON.stringify(series)));
                    resolve("done");
                }).catch((e) => {
                    reject(e)
                })
            })
        }
    },
    getters: {
    }
}
export default modernWatchList
