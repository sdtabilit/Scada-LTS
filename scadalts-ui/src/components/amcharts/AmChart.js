import * as am4core from '@amcharts/amcharts4/core';
import * as am4charts from '@amcharts/amcharts4/charts';

import axios from 'axios';

export class AmChart {
	constructor(build) {
		this.chart = null;
		this.chartReference = build.chartReference;
		this.chartType = build.chartType;
		this.pointIds = build.pointIds;
		this.isExportId = build.isExportId || false;
		this.isStepLineChart = build.isStepLineChart;

		this.isSeparateAxis = build.isSeparateAxis;
		this.colorPallete = build.colorPallete;
		this.strokeWidth = build.strokeWidth || 1;
		this.legend = build.legend;
		this.cursor = build.cursor;
		this.bullets = build.bullets;
		this.tension = build.tension;
		this.exportMenu = build.exportMenu;

		this.jsonConfig = build.jsonConfig;

		this.startTime = build.startTimestamp;
		this.endTime = build.endTimestamp;
		this.groupCount = build.groupCount;
		this.aggregateApiSettings = build.aggregateApiSettings;

		this.refreshRate = build.refreshRate;
		this.isCompareMode = build.isCompareMode;
		this.lastUpdate = 0;
		this.liveUpdateInterval = null;
		this.liveUpdateIntervalRetries = 0;
		this.scrollbarX = build.scrollbarX || false;
	}

	/**
	 * Create and render Chart
	 *
	 * Crate chart based on the provided
	 * configuration during the definition.
	 */
	async createChart() {
		if (!!this.jsonConfig) {
			this.chart = am4core.createFromConfig(
				this.jsonConfig,
				this.chartReference,
				this.useChartType(this.chartType)
			);
		} else {
			this.chart = am4core.create(this.chartReference, this.useChartType(this.chartType));
			this.prepareColors();
			this.prepareAxisX();
			this.prepareAxisY();
			if (!!this.scrollbarX) {
				this.prepareScrollbar();
			}
			if (!!this.legend) {
				this.prepareLegend();
			}
			if (!!this.cursor) {
				this.prepareCursor();
			}
			if (!!this.exportMenu) {
				this.prepareExportMenu();
			}
			let requests = [];
			for (let i of this.pointIds.split(',')) {
				requests.push(this.addSeries(i));
			}
			await Promise.all(requests);
		}
		await this.getData();

		if (!!this.refreshRate && this.refreshRate > 1000) {
			this.startLiveUpdate();
		}
	}

	/**
	 * Start Live Update
	 *
	 * Enable live data refresh. Send a request every
	 * interval period defined during the chart creation.
	 */
	startLiveUpdate() {
		
		this.liveUpdateInterval = setInterval(async () => {
			let newTimestamp = new Date().getTime();
			try {
				this.chart.addData(await this.fetchPointValues(this.lastUpdate, newTimestamp));
				this.lastUpdate = newTimestamp;
			} catch (error) {
				if (this.liveUpdateIntervalRetries >= 5) {
					this.stopLiveUpdate();
					console.error('Server connection failed!\nStopping the live update.');
					console.error(error);
				} else {
					this.liveUpdateIntervalRetries++;
				}
			}
		}, this.refreshRate);
	}

	/**
	 * Stop Live Update
	 *
	 * Useful when user navigate between
	 * multiple charts or when some exception
	 * occurss.
	 */
	stopLiveUpdate() {
		clearInterval(this.liveUpdateInterval);
		this.liveUpdateIntervalRetries = 0;
	}

	/**
	 * Delete Chart
	 *
	 * Safe delete the current chart instance;
	 */
	disposeChart() {
		if (!!this.chart) {
			this.stopLiveUpdate();
			this.chart.dispose();
		}
	}

	/**
	 * @private
	 */
	async getData() {
		try {
			this.chart.data = await this.fetchPointValues(this.startTime, this.endTime);
			if (!this.chart.data[0].date) {
				throw new Error('No data from that range!');
			}
		} catch (e) {
			console.error('Could not get Initial Data');
			throw e;
		}
	}

	/**
	 * @private
	 */
	prepareAxisX() {
		let axis = this.chart.xAxes.push(new am4charts.DateAxis());
		axis.dateFormats.setKey('second', 'HH:mm:ss');
		axis.dateFormats.setKey('minute', 'HH:mm:ss');
		axis.dateFormats.setKey('hour', 'HH:mm');
		axis.dateFormats.setKey('day', 'MMM dd');
		axis.periodChangeDateFormats.setKey('hour', '[bold]dd MMM HH:mm[/]');
		axis.periodChangeDateFormats.setKey('day', '[bold]MMM[/] dd');
		axis.periodChangeDateFormats.setKey('month', '[bold]yyyy[/] MMM');

		if (!!this.groupCount) {
			axis.groupData = true;
			axis.groupCount = this.groupCount;
		}

		axis.minZoomCount = 10;
	}

	/**
	 * @private
	 */
	prepareAxisY(opposite = false, axisId = "NumericAxis") {
		let axis = this.chart.yAxes.values.find(a => a.id === axisId);
		if(!axis) {
			axis = this.chart.yAxes.push(new am4charts.ValueAxis());
			axis.id = axisId
			axis.tooltip.disabled = false;
			if (opposite) {
				axis.renderer.opposite = opposite;
			}
			if(axisId === 'BinaryAxis') {
				axis.max = 1.4;
				axis.min = -0.2;
				axis.renderer.labels.template.disabled = true;
				axis.renderer.grid.template.disabled = true;
				let zeroRange = axis.axisRanges.create();
				zeroRange.value = 0;
				zeroRange.label.text = '0';
				let oneRange = axis.axisRanges.create();
				oneRange.value = 1;
				oneRange.label.text = '1';
			}
		}
		return axis;		
	}

	/**
	 * @private
	 */
	prepareScrollbar() {
		let scrollbarX = new am4charts.XYChartScrollbar();
		this.chart.scrollbarX = scrollbarX;
	}

	/**
	 * @private
	 */
	prepareExportMenu() {
		this.chart.exporting.menu = new am4core.ExportMenu();
		this.chart.exporting.menu.align = 'right';
		this.chart.exporting.menu.verticalAlign = 'top';
		this.chart.exporting.filePrefix = `${this.filePrefix}_${new Date().getTime()}`;
	}

	/**
	 * @private
	 */
	prepareLegend() {
		this.chart.legend = new am4charts.Legend();
	}

	/**
	 * @private
	 */
	prepareCursor() {
		this.chart.cursor = new am4charts.XYCursor();
		this.chart.cursor.behavior = 'panXY';
	}

	/**
	 * @private
	 */
	prepareColors() {
		this.chart.colors.list = this.colorPallete;
	}

	/**
	 * @private
	 */
	addSeries(valueY) {
		return new Promise(async (resolve, reject) => {
			let series = await this.prepareSeries(valueY).catch((e) => reject(e));
			series.dataFields.dateX = 'date';
			series.tooltipText = '{name}: [bold]{valueY}[/]';
			series.tooltip.background.cornerRadius = 20;
			series.tooltip.background.strokeOpacity = 0;
			series.tooltip.pointerOrientation = 'vertical';
			series.tooltip.label.minWidth = 40;
			series.tooltip.label.minHeight = 40;
			series.tooltip.label.textAlign = 'middle';
			series.tooltip.label.textValign = 'middle';
			series.strokeWidth = this.strokeWidth;
			if (!!this.tension) {
				series.tensionX = this.tension;
			}
			if (!!this.bullets) {
				let bullet = series.bullets.push(new am4charts.CircleBullet());
				bullet.circle.strokeWidth = 2;
				bullet.circle.radius = 5;
				bullet.circle.fill = am4core.color('#fff');
			}
			if (!!this.scrollbarX) {
				this.chart.scrollbarX.series.push(series);
			}

			resolve(true);
		});
	}

	/**
	 * Prepare chart series
	 * @private
	 *
	 * Fetch DataPoint details to prepare chart series to
	 * render data properly.
	 *
	 * @param {string} seriesId - Series ID - it could be an XID or ID number.
	 * @returns am4charts Series object
	 */
	async prepareSeries(seriesId) {
		let series;

		let pointDetails = await this.fetchDataPointDetails(seriesId);
		switch (pointDetails.type) {
			case 'BinaryValue':
			case 'MultistateValue':
				series = this.chart.series.push(new am4charts.StepLineSeries());
				if(!this.isSeparateAxis) {
					if(pointDetails.type === 'BinaryValue') {
						series.yAxis = this.prepareAxisY(true, "BinaryAxis");
					} else {
						series.yAxis = this.prepareAxisY(true, "MultistateAxis");
					}
				}
				series.startLocation = 0.5;
				break;
			default:
				let type = !!this.isStepLineChart
					? new am4charts.StepLineSeries()
					: new am4charts.LineSeries();
				series = this.chart.series.push(type);
		}
		if (!!this.isExportId) {
			series.dataFields.valueY = `${pointDetails.id}`;
		} else {
			series.dataFields.valueY = seriesId;
		}
		if(!!this.isSeparateAxis) {
			series.yAxis = this.prepareAxisY(false, `Axis${seriesId}`);
		}
		series.name = pointDetails.name;
		return series;
	}

	/**
	 * @private
	 * @param {*} startTs
	 * @param {*} endTs
	 * @returns
	 */
	fetchPointValues(startTs, endTs) {
		if (typeof startTs === 'string' || typeof endTs === 'string') {
			throw new Error('Start and End date must be a number!');
		}
		if (!!startTs && !!endTs && startTs > endTs) {
			throw new Error('Start date is greater than End date!');
		}
		if (!endTs) {
			endTs = new Date().getTime();
		}
		if (!startTs) {
			startTs = endTs - 3600000;
		}
		this.lastUpdate = endTs;
		let requestUrl = `./api/amcharts`;
		if (!!this.isExportId) {
			requestUrl += `/by-xid`;
		} else {
			requestUrl += `/by-id`;
		}
		requestUrl += `?startTs=${startTs}&endTs=${endTs}&ids=${this.pointIds}`;
		if (!!this.isCompareMode) {
			requestUrl += '&cmp=true';
		}
		if (!!this.aggregateApiSettings) {
			requestUrl += '&configFromSystem=false';
			requestUrl += '&enabled=true';
			requestUrl += `&valuesLimit=${this.aggregateApiSettings.valuesLimit}`;
			requestUrl += `&limitFactor=${this.aggregateApiSettings.limitFactor}`;
		}

		return new Promise((resolve, reject) => {
			axios
				.get(requestUrl)
				.then((resp) => {
					resolve(resp.data);
				})
				.catch((error) => {
					console.error(error);
					reject(error);
				});
		});
	}

	/**
	 * @private
	 * @param {Number} datapointId
	 * @returns
	 */
	fetchDataPointDetails(datapointId) {
		let requestUrl = !!this.isExportId
			? `./api/point_value/getValue/${datapointId}`
			: `./api/point_value/getValue/id/${datapointId}`;

		return new Promise((resolve, reject) => {
			axios
				.get(requestUrl)
				.then((resp) => {
					resolve(resp.data);
				})
				.catch((error) => {
					console.error(error);
					reject(new Error('Failed to load Data Point details'));
				});
		});
	}

	/**
	 * Use Chart Type
	 * @private
	 *
	 * Based on the provided chartType
	 * render a specific amChart object;
	 *
	 * @param {string} chartType
	 * @returns am4charts chart Class
	 */
	useChartType(chartType) {
		switch (chartType.toLowerCase()) {
			case 'xychart':
				return am4charts.XYChart;
			case 'piechart':
				return am4charts.PieChart;
			case 'gaugechart':
				return am4charts.GaugeChart;
			default:
				console.error(`Unsupported Chart Type! (${chartType})`);
		}
	}
}

export class AmChartBuilder {
	/**
	 * Define AmChart instance object
	 * using prepared Builder class.
	 *
	 * @param {string | HTMLElement} reference - HTML reference where to render
	 * @param {string} type - Type of am4chart class
	 * @param {string} pointIds - String Data Points separeted with comma char to be displayed.
	 */
	constructor(reference, type, pointIds) {
		this.chartReference = reference;
		this.chartType = type;
		this.pointIds = pointIds;
		this.colorPallete = [
			am4core.color('#39B54A'),
			am4core.color('#69FF7D'),
			am4core.color('#166921'),
			am4core.color('#690C24'),
			am4core.color('#B53859'),
			am4core.color('#734FC1'),
			am4core.color('#824F1B'),
			am4core.color('#69421B'),
		];
	}

	makeFromConfig(config) {
		this.jsonConfig = config;
		return this;
	}

	build() {
		return new AmChart(this);
	}

	/**
	 * Switch to ExportID mode
	 *
	 * Instead of providing the dataPointIds
	 * user can provide the DataPoint ExportIDs
	 * during the chart definition.
	 */
	xid() {
		this.isExportId = true;
		return this;
	}

	/**
	 * Separate Y Axes
	 * 
	 * Generate sepearate Y-axes for all
	 * datapoint series that will be rendered
	 * on a chart.
	 * @returns 
	 */
	separateAxis() {
		this.isSeparateAxis = true;
		return this;
	}
	
	/**
	 * Enable compare Mode
	 * 
	 * Get two data series from API
	 * to render them in a seperate
	 * date and value axes that can
	 * present data from different
	 * time ranges. Usefull to provide
	 * a series comparison.
	 * @returns 
	 */
	compare() {
		this.isCompareMode = true;
		return this;
	}

	/**
	 * Enable Step Line mode
	 *
	 * Render all series as Step line type
	 * instead of classic line series.
	 */
	stepLine() {
		this.isStepLineChart = true;
		return this;
	}

	/**
	 * Set up Start Time
	 *
	 * Start time could be static or dynamic.
	 * For example static time is a specific
	 * date from the past (ex. 2020-06-23).
	 * Dynamic time is relative definition
	 * like for example "1-day" or "2-months"
	 * and so on. The date is validated
	 * and converted to valid form.
	 *
	 * @param {Object} startTimestamp
	 * @returns
	 */
	startTime(startTimestamp) {
		this.startTimestamp = this.getValidDate(startTimestamp);
		return this;
	}

	/**
	 * Set up End Time
	 *
	 * End time could be static or dynamic.
	 * For example static time is a specific
	 * date from the past (ex. 2020-06-23).
	 * Dynamic time is relative definition
	 * like for example "1-day" or "2-months"
	 * and so on. The date is validated
	 * and converted to valid form.
	 *
	 * @param {Object} endTimestamp
	 * @returns
	 */
	endTime(endTimestamp) {
		this.endTimestamp = this.getValidDate(endTimestamp);
		return this;
	}

	/**
	 * Aggregate Data
	 *
	 * Increase chart performance and make an data
	 * aggregation to reduce the displayed data.
	 *
	 * @param {Number} aggregation
	 * @returns
	 */
	useAggregation(aggregation = 200) {
		this.groupCount = aggregation;
		return this;
	}

	/**
	 * Use custom Color Set
	 *
	 * Provide additional color set to render
	 * chart series in a different theme.
	 *
	 * @param {string} colors - Hexadecimal Color references separated with comma.
	 * @returns
	 */
	useColors(colors) {
		let colorsArray = colors.split(',');
		if (colorsArray.length > 0) {
			for (let i = colorsArray.length - 1; i >= 0; i--) {
				this.colorPallete.unshift(am4core.color(colorsArray[i].trim()));
			}
		}
		return this;
	}

	/**
	 * Set Stroke Width for Series Lines
	 * 
	 * @param {Number} width - [1,10] Width of the series line
	 */
	setStrokeWidth(width) {
		if(!!width && width > 0 && width <= 10) {
			this.strokeWidth = width;
		}
		return this;
	}

	/**
	 * Show Scrollbar
	 *
	 * Render Scrollbar for chart.
	 * That will allow user to zoom in
	 * and to zoom out within the chart instance
	 * @returns
	 */
	showScrollbar() {
		this.scrollbarX = true;
		return this;
	}

	/**
	 * Show series Legend
	 *
	 * Render Legend below the chart instance.
	 * If it is active user can show or hide
	 * specific series from chart.
	 * @returns
	 */
	showLegend() {
		this.legend = true;
		return this;
	}

	/**
	 * Show cursor over chart
	 *
	 * Render cursor that will display the value
	 * of specific series over the chart.
	 * @returns
	 */
	showCursor() {
		this.cursor = true;
		return this;
	}

	/**
	 * Show bullets on series
	 *
	 * Render bullets on the chart where the mesurement
	 * was taken. It is making the chart more readable
	 * but it reduce the performance of the chart.
	 * @returns
	 */
	showBullets() {
		this.bullets = true;
		return this;
	}

	/**
	 * Show export menu
	 *
	 * This menu allow user to save the current chart
	 * values to csv, excel, json and other text files.
	 * Another option is to make a screenshoot of the
	 * current chart state to easily create a graphical
	 * report.
	 *
	 * @param {string} filePrefix - [default: Scada_Chart] Prefix for created files
	 * @returns
	 */
	showExportMenu(filePrefix = 'Scada_Chart') {
		this.exportMenu = filePrefix;
		return this;
	}

	/**
	 * Set smooth line for Chart
	 *
	 * Set horizonal tension parameter for Chart that
	 * will make the series lines more smoother.
	 *
	 * @param {Number} tension - Smooth parameter from [0-1] range
	 * @returns
	 */
	smoothLine(tension) {
		if (tension >= 0 && tension <= 1) {
			this.tension = tension;
		}
		return this;
	}

	/**
	 * Enable Live Data Mode
	 *
	 * Set-up the refresh rate that
	 * will send a request to the API
	 * for latest Point Values data.
	 *
	 * @param {Number} refreshRate - Time in ms when send a get new data request.
	 * @returns
	 */
	withLiveUpdate(refreshRate) {
		this.refreshRate = refreshRate;
		return this;
	}

	/**
	 * Set Point Values Agregation on Backend
	 * 
	 * Set dedicated agregation for specific chart.
	 * Override the SystemSettings settings.
	 * 
	 *@param {Number} valuesLimit - Limit the number of values that will be displayed on the chart
	 *@param {Number} limitFactor - Factor that will detect when the aggegation should be activated.
	 *@returns
	 */
	setApiAggregation(valuesLimit, limitFactor = 1) {
		this.aggregateApiSettings = {
			enabled: true,
			valuesLimit: valuesLimit,
			limitFactor: limitFactor
		}
		return this;
	}

	/**
	 * Validate and conver Data
	 * @private
	 *
	 * AmChart class require a numeric timestamp
	 * to send a GET request so every date provided
	 * by user must be converted into valid timestamp
	 * value that will mach the further operations.
	 *
	 * @param {Object} date - Date to be converted
	 * @returns {Number} valid Date timestamp
	 */
	getValidDate(date) {
		if (typeof date === 'string') {
			const regex = /\d+-((day)|(month)|(year)|(week)|(hour)|(minute))/g;
			if (!!date.match(regex)) {
				return this.convertDate(date);
			} else {
				date = new Date(date);
				if (!isNaN(date)) {
					return date.getTime();
				} else {
					throw new Error('Not valid date!');
				}
			}
		} else {
			if (date instanceof Date) {
				return date.getTime();
			} else {
				return date;
			}
		}
	}

	/**
	 * Convert String Data
	 * @private
	 *
	 * Convert dynamic string start date
	 * to specific timestamp.
	 *
	 * @param {string} dateString
	 * @returns
	 */
	convertDate(dateString) {
		let date = dateString.split('-');
		if (date.length === 2) {
			let now = new Date();
			let multiplier = 1000;
			switch (date[1]) {
				case 'minute':
				case 'minutes':
					multiplier = multiplier * 60;
					break;
				case 'day':
				case 'days':
					multiplier = multiplier * 60 * 60 * 24;
					break;
				case 'week':
				case 'weeks':
					multiplier = multiplier * 60 * 60 * 24 * 7;
					break;
				case 'month':
				case 'months':
					multiplier = multiplier * 60 * 60 * 24 * 7 * 4;
					break;
				case 'year':
				case 'years':
					multiplier = multiplier * 60 * 60 * 24 * 7 * 4 * 12;
					break;
				default:
					multiplier = multiplier * 60 * 60;
			}
			return now.getTime() - Number(date[0]) * multiplier;
		} else {
			throw new Error('Not valid date format! [Use for example: "1-day"]');
		}
	}
}

export default AmChartBuilder;
