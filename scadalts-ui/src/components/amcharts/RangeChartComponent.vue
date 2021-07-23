<template>
	<v-app relative id="chart-component" :style="{ 'width': `${width}px`, 'height': `${height}px` }">
		<v-row id="chart-settings">
			<v-col cols="12" v-if="!!errorMessage" class="error-message-bar">
				<v-alert
					:type="errorMessage.type"
					dismissible
					v-if="!!errorMessage"
					transition="scale-transition"
					dense
					@input="updatedAlert"
				>
					{{ errorMessage.message }}
				</v-alert>
			</v-col>

			<v-col cols="3">
				<v-menu ref="start-date-menu"
					:close-on-content-click="false"
					:nudge-right="40"
					transition="scale-transition"
					offset-y
					min-width="auto"
					attach
				>
					<template v-slot:activator="{ on, attrs }">
						<v-text-field 
							v-model="startDate"
							label="Start Date"
							prepend-icon="mdi-calendar"
							v-bind="attrs"
							v-on="on"
						></v-text-field>
					</template>
					<v-date-picker
          				v-model="startDate"
						first-day-of-week="1"
          				no-title
          				scrollable
        			></v-date-picker>
				</v-menu>
			</v-col>
			<v-col cols="2">
				<v-menu ref="start-time-menu"
					:close-on-content-click="false"
					:nudge-right="40"
					transition="scale-transition"
					offset-y
					max-width="290px"
					min-width="290px"
					attach
				>
					<template v-slot:activator="{ on, attrs }">
						<v-text-field 
							v-model="startTime"
							label="Start Time"
							prepend-icon="mdi-clock-time-four-outline"
							v-bind="attrs"
							v-on="on"
						></v-text-field>
					</template>
					<v-time-picker 
						v-model="startTime"
						format="24hr" 
						scrollable
					></v-time-picker>
				</v-menu>
			</v-col>

			<v-col cols="3">
				<v-menu ref="end-date-menu"
					:close-on-content-click="false"
					:nudge-right="40"
					transition="scale-transition"
					offset-y
					min-width="auto"
					attach
				>
					<template v-slot:activator="{ on, attrs }">
						<v-text-field 
							v-model="endDate"
							label="End Date"
							prepend-icon="mdi-calendar"
							v-bind="attrs"
							v-on="on"
						></v-text-field>
					</template>
					<v-date-picker
          				v-model="endDate"
						first-day-of-week="1"
          				no-title
          				scrollable
        			></v-date-picker>
				</v-menu>
			</v-col>
			<v-col cols="2">
				<v-menu ref="end-time-menu"
					:close-on-content-click="false"
					:nudge-right="40"
					transition="scale-transition"
					offset-y
					max-width="290px"
					min-width="290px"
					attach
				>
					<template v-slot:activator="{ on, attrs }">
						<v-text-field 
							v-model="endTime"
							label="End Time"
							prepend-icon="mdi-clock-time-four-outline"
							v-bind="attrs"
							v-on="on"
						></v-text-field>
					</template>
					<v-time-picker 
						v-model="endTime"
						format="24hr" 
						scrollable
					></v-time-picker>
				</v-menu>
			</v-col>
			<v-col cols="2">
				<v-btn icon @click="reload()">
					<v-icon>mdi-refresh</v-icon>
				</v-btn>
			</v-col>
		</v-row>
		<v-row id="chart-container">
			<v-col cols="12">
				<div ref="chartReference"></div>
			</v-col>
		</v-row>
	</v-app>
</template>
<script>
import AmCharts from './AmChart.js';

/**
 * Range Chart Component
 * @version 2.0.0
 * 
 * Display AmChart with Datepicker
 * 
 */
export default {
	name: 'RangeChartComponent',

	props: {
		pointIds: { type: String, required: true },
		useXid: { type: Boolean },
		separateAxis: {type: Boolean},
		stepLine: { type: Boolean },
		width: { type: String, default: '500' },
		height: { type: String, default: '400' },
		color: { type: String },
		strokeWidth: {type: Number},
		aggregation: { type: Number },
		showBullets: { type: Boolean },
		showExportMenu: { type: String },
		smoothLine: { type: Number },
		serverValuesLimit: { type: Number },
		serverLimitFactor: { type: Number }
	},

	data() {
		return {
			chartClass: undefined,
			errorMessage: undefined,
			viewId: 0,
			startDate: '',
			endDate: '',
			startTime: '',
			endTime: '',
		};
	},

	mounted() {
		this.initConfiguration();
		this.initChart();
	},
	methods: {
		initChart() {
			this.chartClass = new AmCharts(this.$refs.chartReference, 'xychart', this.pointIds)
				.showCursor()
				.startTime(`${this.startDate} ${this.startTime}`)
				.endTime(`${this.endDate} ${this.endTime}`)
				.setStrokeWidth(this.strokeWidth)
				.showScrollbar()
				.showLegend();

			if(!!this.serverValuesLimit) {
				this.chartClass.setApiAggregation(this.serverValuesLimit, this.serverLimitFactor);
			}

			if (!!this.useXid) {
				this.chartClass.xid();
			}
			if (!!this.separateAxis) {
				this.chartClass.separateAxis();
			}
			if (!!this.stepLine) {
				this.chartClass.stepLine();
			}
			if (!!this.aggregation) {
				if (this.aggregation === 0) {
					this.chartClass.useAggregation();
				} else {
					this.chartClass.useAggregation(this.aggregation);
				}
			}
			if (!!this.color) {
				this.chartClass.useColors(this.color)
			}
			if (!!this.showBullets) {
				this.chartClass.showBullets();
			}
			if (!!this.showExportMenu) {
				this.chartClass.showExportMenu(this.showExportMenu);
			}
			if (!!this.smoothLine) {
				this.chartClass.smoothLine(this.smoothLine);
			}
			this.chartClass = this.chartClass.build();

			this.chartClass.createChart().catch((e) => {
				if (e.message === 'No data from that range!') {
					this.errorMessage = {
						type: 'warning',
						message: e.message,
					};
				} else {
					this.errorMessage = {
						type: 'error',
						message: `Failed to load chart!: ${e.message}`,
					};
				}
			});
		},

		reload() {
			if(!!this.chartClass) {
				this.chartClass.disposeChart();
			}
			this.saveToLocalStorage();
			this.errorMessage = null;
			this.initChart();
		},

		/**
		 * Get Graphical View ID
		 * Util method for saving unique chart data.
		 */
		getViewId() {
			const href = window.location.href.split('?')[1];
			if (!!href) {
				this.viewId = href.match(/\d+/)[0];
			}
		},

		initConfiguration() {
			this.getViewId();
			let data = this.loadFromLocalStorage();
			if (!!data[0] && !!data[1]) {
				this.startDate = data[0];
				this.endDate = data[1];
				this.startTime = data[2] || '';
				this.endTime = data[3] || '';
			} else {
				this.initialTime();
			}
		},

		saveToLocalStorage() {
			let baseKey = `GVRC_${this.viewId}`;
			localStorage.setItem(`${baseKey}-start-date`, this.startDate);
			localStorage.setItem(`${baseKey}-end-date`, this.endDate);
			localStorage.setItem(`${baseKey}-start-time`, this.startTime);
			localStorage.setItem(`${baseKey}-end-time`, this.endTime);
		},

		loadFromLocalStorage() {
			let baseKey = `GVRC_${this.viewId}`;
			let startDate = localStorage.getItem(`${baseKey}-start-date`);
			let endDate = localStorage.getItem(`${baseKey}-end-date`);
			let startTime = localStorage.getItem(`${baseKey}-start-time`);
			let endTime = localStorage.getItem(`${baseKey}-end-time`);
			return [startDate, endDate, startTime, endTime];
		},

		initialTime() {
			let today = new Date();
			this.endDate = today.toISOString().slice(0, 10);
			this.endTime = "12:00";
			today.setDate(today.getDate() - 1);
			this.startDate = today.toISOString().slice(0, 10);
			this.startTime = "12:00";
		},

		updatedAlert(event) {
			if (!event) {
				this.errorMessage = null;
			}
		},
	},
};
</script>
<style scoped>
#chart-settings {
	flex: none;
}
#chart-container > * > div {
	min-width: 650px;
	height: 500px;
}
#chart-component {
	position: relative;
}
.error-message-bar {
	position: absolute;
    top: 50px;
}
</style>
