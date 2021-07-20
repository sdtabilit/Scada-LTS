<template>
	<v-col cols="12" md="6">
		<v-card>
			<v-card-title>
				{{ $t('systemsettings.amcharts.title') }}
				<span v-if="isAmchartsEdited">*</span>
			</v-card-title>
			<v-card-text>
                <section id="amchart-aggregation">
                    <v-row>
                        <v-col cols="12">
                            <h5>{{$t('systemsettings.amcharts.aggregation.title')}}</h5>
                        </v-col>
                        <v-col cols="12">
                            <v-switch
							    v-model="amcharts.enabled"
							    :label="$t('systemsettings.amchart.enabled')"
							    @click="watchRadioDataChagne(amcharts.enabled)"
						    ></v-switch>
                        </v-col>
                        <v-col cols="6">
                            <v-text-field
							    v-model="amcharts.valuesLimit"
							    :label="$t('systemsettings.amchart.valuesLimit')"
							    @input="watchDataChange()"
								:disabled="!amcharts.enabled"
							    dense
						    ></v-text-field>
                        </v-col>
                        <v-col cols="6">
                            <v-text-field
							    v-model="amcharts.limitFactor"
							    :label="$t('systemsettings.amchart.limitFactor')"
							    @input="watchDataChange()"
								:disabled="!amcharts.enabled"
							    dense
						    ></v-text-field>
                        </v-col>
                    </v-row>
                </section>
			</v-card-text>
		</v-card>

		<v-snackbar v-model="response.status" :color="response.color">
			{{ response.message }}
		</v-snackbar>
	</v-col>
</template>
<script>

export default {
	name: 'AmChartSettingsComponent',

	data() {
		return {
			amcharts: undefined,
			amchartsStore: undefined,
			isAmchartsEdited: false,
			response: {
				color: 'success',
				status: false,
				message: '',
			},
		};
	},

	mounted() {
		this.fetchData();
	},

	methods: {
		async fetchData() {
			this.amcharts = await this.$store.dispatch('getAmchartsSettings');
			this.amchartsStore = this.copyDataFromStore();
		},

		saveData() {
			console.log('Saved!');
			this.$store.commit('setAmchartsSettings', this.amcharts);
			this.amchartsStore = this.copyDataFromStore();
			this.$store
				.dispatch('saveAmchartsSettings')
				.then((resp) => {
					if (resp) {
						this.restoreData();
						this.response = {
							status: true,
							message: this.$t('systemsettings.notification.save.amchart'),
							color: 'success',
						};
					}
				})
				.catch(() => {
					this.response = {
						status: true,
						message: this.$t('systemsettings.notification.fail'),
						color: 'danger',
					};
				});
		},

		restoreData() {
			this.fetchData();
			this.isAmchartsEdited = false;
		},

		copyDataFromStore() {
			return JSON.parse(JSON.stringify(this.$store.state.systemSettings.amchartsSettings));
		},

		watchRadioDataChagne(value) {
			this.amcharts.enabled = value;
			this.watchDataChange();
		},

		async watchDataChange() {
			this.isAmchartsEdited = await this.isDataChanged();
			this.emitData(this.isAmchartsEdited);
		},

		async isDataChanged() {
			return !(await this.$store.dispatch('configurationEqual', {
				object1: this.amcharts,
				object2: this.amchartsStore,
			}));
		},

		emitData(changed) {
			this.$emit('changed', {
				component: 'amChartSettingsComponent',
				title: 'systemsettings.amcharts.title',
				changed: changed,
				data: this.sumarizeDataChanges(),
			});
		},

		sumarizeDataChanges() {
			let data = [];
			for (let key in this.amcharts) {
				if (this.amcharts[key] !== this.amchartsStore[key]) {
					data.push({
						label: `systemsettings.amchart.${key}`,
						originalData: this.amchartsStore[key],
						changedData: this.amcharts[key],
					});
				}
			}
			return data;
		},
	},
};
</script>
<style></style>
