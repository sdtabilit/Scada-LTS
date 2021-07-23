<template>
	<div>
		<v-container fluid v-if="isUserRoleAdmin">
			<v-row>
				<v-col cols="12" md="9">
					<v-row dense>
						<v-col cols="12" class="d-flex flex-row justify-space-between">
							<h1>{{ $t('systemsettings.title') }}</h1>
							<v-spacer></v-spacer>
							<v-btn
								fab
								elevation="2"
								color="primary"
								@click="openModal = !openModal"
								v-if="componentsEdited.length > 0"
							>
								<v-icon>mdi-content-save</v-icon>
							</v-btn>
						</v-col>
						<v-col cols="12">
							<v-row dense>
								<AuditEventTypesComponent
									ref="auditEventTypesComponent"
									@changed="componentChanged"
								></AuditEventTypesComponent>
								<SystemEventTypesComponent
									ref="systemEventTypesComponent"
									@changed="componentChanged"
								></SystemEventTypesComponent>
								<EmailSettingsComponent
									ref="emailSettingsComponent"
									@changed="componentChanged"
								></EmailSettingsComponent>
								<MiscSettingsComponent
									ref="miscSettingsComponent"
									@changed="componentChanged"
								></MiscSettingsComponent>
								<HttpSettingsComponent
									ref="httpSettingsComponent"
									@changed="componentChanged"
								></HttpSettingsComponent>
								<DefaultLoggingTypeSettingsComponent
									ref="defaultLoggingTypeSettingsComponent"
									@changed="componentChanged"
								></DefaultLoggingTypeSettingsComponent>
								<SmsDomainSettingsComponent
									ref="smsDomainSettingsComponent"
									@changed="componentChanged"
								></SmsDomainSettingsComponent>
								<AmChartSettingsComponent
									ref="amChartSettingsComponent"
									@changed="componentChanged"
								></AmChartSettingsComponent>
								<ScadaConfigurationComponent></ScadaConfigurationComponent>
							</v-row>
						</v-col>
					</v-row>
				</v-col>
				<v-col cols="12" md="3">
					<v-row>
						<v-col cols="12">
							<IsAlive
								:plabel="$t('systemsettings.info.title')"
								:ptime-refresh="4000"
								:ptime-warning="9000"
								:ptime-error="11000"
							></IsAlive>
						</v-col>
					</v-row>

					<v-row v-if="systemRunningTime">
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.systemtime') }}</p>
						</v-col>
						<v-col cols="5">
							<p>{{ systemRunningTime }}</p>
						</v-col>
					</v-row>

					<v-row>
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.milestone') }}</p>
						</v-col>
						<v-col cols="5">
							<p>
								{{ $store.getters.appMilestone }} build
								{{ $store.getters.appBuild }}
							</p>
						</v-col>
					</v-row>

					<v-row>
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.branch') }}</p>
						</v-col>
						<v-col cols="5">
							<p>{{ $store.getters.appBranch }}</p>
						</v-col>
					</v-row>

					<v-row>
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.commit') }}</p>
						</v-col>
						<v-col cols="5">
							<p v-if="!$store.getters.appCommitLink">
								{{ $store.getters.appCommit }}
							</p>
							<a
								v-if="!!$store.getters.appCommitLink"
								:href="$store.getters.appCommitLink"
							>
								{{ $store.getters.appCommit }}
							</a>
						</v-col>
					</v-row>

					<v-row v-if="$store.getters.appPullRequestNumber !== 'false'">
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.pullrequest') }}</p>
						</v-col>
						<v-col cols="5">
							<p>{{ $store.getters.appPullRequestNumber }}</p>
							<br />
							<p>{{ $store.getters.appPullRequestBranch }}</p>
						</v-col>
					</v-row>

					<v-row>
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.tag') }}</p>
						</v-col>
						<v-col cols="5">
							<p>{{ $store.getters.appTag }}</p>
						</v-col>
					</v-row>

					<v-row>
						<v-col cols="7">
							<p>{{ $t('systemsettings.info.uiversion') }}</p>
						</v-col>
						<v-col cols="5">
							<p>{{ $store.getters.appVersion }}</p>
						</v-col>
					</v-row>

					<v-row v-if="!!systemInfoSettings">
						<v-col cols="12">
							<v-text-field
								v-model="systemInfoSettings.instanceDescription"
								:label="$t('systemsettings.info.instance')"
								@input="saveSystemInfoSettings()"
								dense
							></v-text-field>
						</v-col>

						<v-col cols="12">
							<v-select
								@change="saveSystemInfoSettings()"
								v-model="systemInfoSettings.language"
								:items="languageItems"
								item-value="value"
								item-text="text"
								:label="$t('systemsettings.info.language')"
								dense
							></v-select>
						</v-col>
					</v-row>

					<v-row v-if="databaseType">
						<v-col cols="12" class="d-flex flex-row justify-space-between">
							<h2>{{ $t('systemsettings.database.title') }}</h2>
							<v-spacer></v-spacer>
							<v-btn fab icon @click="initDatabaseSection()">
								<v-icon>mdi-refresh</v-icon>
							</v-btn>
						</v-col>

						<v-col cols="12">
							<v-row v-if="schemaVersion">
								<v-col cols="8">
									<p>{{ $t('systemsettings.database.schemaVersion') }}</p>
								</v-col>
								<v-col cols="4">
									<p>{{ schemaVersion }}</p>
								</v-col>

								<v-col cols="8">
									<p>{{ $t('systemsettings.database.size') }}</p>
								</v-col>
								<v-col cols="4">
									<p>{{ databaseInfo.databaseSize }}</p>
								</v-col>

								<v-col cols="8">
									<p>{{ $t('systemsettings.database.file') }}</p>
								</v-col>
								<v-col cols="4">
									<p>{{ databaseInfo.filedataCount }}</p>
								</v-col>

								<v-col cols="8">
									<p>{{ $t('systemsettings.database.total') }}</p>
								</v-col>
								<v-col cols="4">
									<p>{{ databaseInfo.filedataSize }}</p>
								</v-col>

								<v-col cols="8">
									<p>{{ $t('systemsettings.database.histroy') }}</p>
								</v-col>
								<v-col cols="4">
									<p>{{ databaseInfo.historyCount }}</p>
								</v-col>

								<v-col cols="8">
									<p>{{ $t('systemsettings.database.event') }}</p>
								</v-col>
								<v-col cols="4">
									<p>{{ databaseInfo.eventCount }}</p>
								</v-col>

								<v-col cols="12">
									<p>{{ $t('systemsettings.database.top') }}</p>
								</v-col>
								<v-col
									cols="12"
									v-for="point in databaseInfo.topPoints"
									v-bind:key="point.pointId"
								>
									<p>
										{{ point.pointName }}<br />
										(Point ID: {{ point.pointId }} - count: {{ point.count }})
									</p>
								</v-col>
							</v-row>
						</v-col>
					</v-row>
				</v-col>
			</v-row>
		</v-container>

		<v-dialog v-model="openModal" max-width="750">
			<v-card>
				<v-card-title>
					{{ $t('systemsettings.label.summary') }}
				</v-card-title>

				<v-card-text v-if="componentsEdited">
					<v-row v-for="component in componentsEdited" :key="component">
						<v-col cols="12" class="d-flex flex-row justify-space-between">
							<h4>{{ $t(component.title) }}</h4>
							<v-spacer></v-spacer>
							<div>
								<v-tooltip bottom>
									<template v-slot:activator="{ on, attrs }">
										<v-btn
											fab
											icon
											@click="restoreComponent(component.component)"
											v-bind="attrs"
											v-on="on"
										>
											<v-icon>mdi-redo-variant</v-icon>
										</v-btn>
									</template>
									<span>{{ $t('systemsettings.label.restore') }}</span>
								</v-tooltip>
								<v-tooltip bottom>
									<template v-slot:activator="{ on, attrs }">
										<v-btn
											fab
											icon
											@click="saveComponent(component.component)"
											v-bind="attrs"
											v-on="on"
										>
											<v-icon>mdi-content-save</v-icon>
										</v-btn>
									</template>
									<span>{{ $t('systemsettings.label.save') }}</span>
								</v-tooltip>
							</div>
						</v-col>
						<v-col cols="12" v-for="e in component.data" :key="e">
							<v-row>
								<v-col cols="4">{{ $t(e.label) }}</v-col>
								<v-col cols="4" class="red">{{ e.originalData | blank | convert }}</v-col>
								<v-col cols="4" class="green">{{
									e.changedData | blank | convert
								}}</v-col>
							</v-row>
						</v-col>
					</v-row>
				</v-card-text>

				<v-card-actions>
					<v-spacer></v-spacer>
					<v-btn text @click="openModal = false">{{ $t('uiv.modal.cancel') }}</v-btn>
					<v-btn text @click="restoreAllComponents()">{{
						$t('systemsettings.label.restoreall')
					}}</v-btn>
					<v-btn text color="primary" @click="saveAllComponents()">{{
						$t('systemsettings.label.saveall')
					}}</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>

		<v-container fluid v-if="!isUserRoleAdmin" class="d-flex justify-center">
			<v-spacer></v-spacer>
			<h2>Not allowed to see that page</h2>
			<v-spacer></v-spacer>
		</v-container>

		<v-snackbar v-model="response.status" :color="response.color">
			{{ response.message }}
		</v-snackbar>
	</div>
</template>
<script>
import store from '../../store';
import i18n from '../../i18n';
import IsAlive from '../../components/graphical_views/IsAlive';
import AuditEventTypesComponent from './AuditEventTypesComponent';
import SystemEventTypesComponent from './SystemEventTypesComponent';
import EmailSettingsComponent from './EmailSettingsComponent';
import HttpSettingsComponent from './HttpSettingsComponent';
import MiscSettingsComponent from './MiscSettingsComponent';
import DefaultLoggingTypeSettingsComponent from './DefaultLoggingTypeComponent';
import SmsDomainSettingsComponent from './SmsDomainSettingsComponent';
import ScadaConfigurationComponent from './ScadaConfigurationComponent';
import AmChartSettingsComponent from './AmChartSettingsComponent';

export default {
	el: '#systemsettings',
	name: 'systemsettings',
	components: {
		IsAlive,
		AuditEventTypesComponent,
		SystemEventTypesComponent,
		EmailSettingsComponent,
		HttpSettingsComponent,
		MiscSettingsComponent,
		DefaultLoggingTypeSettingsComponent,
		SmsDomainSettingsComponent,
		ScadaConfigurationComponent,
		AmChartSettingsComponent,
	},
	filters: {
		blank: function (value) {
			if (value === '') return '---';
			return value;
		},
		convert: function (value) {
			if (typeof value === 'boolean') {
				if (value) {
					return i18n.t('uiv.enable');
				} else {
					return i18n.t('uiv.disable');
				}
			}
			return value;
		},
	},
	data() {
		return {
			componentsEdited: [],
			isUserRoleAdmin: true,
			systemRunningTime: undefined,
			openModal: false,
			languageItems: [
				{ value: 'de', text: 'Deutsch' },
				{ value: 'en', text: 'English' },
			],
			response: {
				color: 'success',
				status: false,
				message: '',
			},
		};
	},
	mounted() {
		if (!this.isUserRoleAdmin) {
			this.getUserRole();
		}
		store.dispatch('getSystemInfoSettings');
		this.initDatabaseSection();
		this.loadClock();
	},
	methods: {
		async getUserRole() {
			this.isUserRoleAdmin = await store.dispatch('getUserRole');
		},
		initDatabaseSection() {
			store.dispatch('getDatabaseType');
			store.dispatch('getDatabaseSize');
			store.dispatch('getSchemaVersion');
		},

		saveSystemInfoSettings() {
			this.$store.dispatch('saveSystemInfoSettings').then((resp) => {
				if (resp) {
					i18n.locale = this.systemInfoSettings.language;
					this.generateNotification(
						'success',
						i18n.t('systemsettings.notification.save.systeminfo'),
					);
				} else {
					this.generateNotification('danger', i18n.t('systemsettings.notification.fail'));
				}
			});
		},
		saveDatabase(databaseType) {
			this.$store.dispatch('saveDatabaseType', databaseType).then((resp) => {
				if (resp) {
					this.generateNotification(
						'success',
						i18n.t('systemsettings.notification.save.database'),
					);
				} else {
					this.generateNotification('danger', i18n.t('systemsettings.notification.fail'));
				}
			});
		},
		generateNotification(type, content) {
			this.response = {
				status: true,
				message: content,
				color: type,
			};
			// this.$notify({
			// 	placement: 'bottom-right',
			// 	type,
			// 	content,
			// });
		},
		async loadClock() {
			let result = await store.dispatch('getSystemStartupTime');
			if (result) {
				this.runClock();
			}
		},
		runClock() {
			setInterval(() => {
				let now = new Date().getTime();
				let distance = now - this.systemStartupTime.getTime();
				let days = Math.floor(distance / (1000 * 60 * 60 * 24));
				let hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
				let minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
				let seconds = Math.floor((distance % (1000 * 60)) / 1000);
				this.systemRunningTime = `${days}d ${hours}h ${minutes}m ${seconds}s`;
			}, 1000);
		},

		async componentChanged(object) {
			console.log(object);
			let idx = this.componentsEdited.findIndex((x) => x.component == object.component);
			if (idx == -1 && object.changed) {
				this.componentsEdited.push(object);
			} else if (idx != -1 && !object.changed) {
				this.componentsEdited.splice(idx, 1);
			} else if (idx != -1 && object.changed) {
				this.componentsEdited[idx] = object;
			}
			console.log(this.componentsEdited);
		},

		saveComponent(component) {
			this.$refs[component].saveData();
			this.removeComponent(component);
		},

		saveAllComponents() {
			this.componentsEdited.forEach((e) => {
				this.$refs[e.component].saveData();
			});
			this.componentsEdited = [];
			this.openModal = false;
		},

		restoreComponent(component) {
			this.$refs[component].restoreData();
			this.removeComponent(component);
		},

		restoreAllComponents() {
			this.componentsEdited.forEach((e) => {
				this.$refs[e.component].restoreData();
			});
			this.componentsEdited = [];
			this.openModal = false;
		},

		removeComponent(component) {
			this.componentsEdited = this.componentsEdited.filter(
				(x) => x.component !== component,
			);
			if (this.componentsEdited.length == 0) this.openModal = false;
		},
	},
	computed: {
		databaseType() {
			return this.$store.state.systemSettings.databaseType;
		},
		databaseInfo() {
			return this.$store.state.systemSettings.databaseInfo;
		},
		systemInfoSettings() {
			return this.$store.state.systemSettings.systemInfoSettings;
		},
		systemStartupTime() {
			return this.$store.state.systemSettings.systemStartupTime;
		},
		schemaVersion() {
			return this.$store.state.systemSettings.schemaVersion;
		},
	},
};
</script>
<style>
body > .alert {
	z-index: 2000;
}
</style>
<style scoped>
.sidebar {
	background-color: #23232e;
	color: white;
	height: 98vh;
	overflow-y: auto;
}
.mainbar {
	height: 98vh;
	overflow-y: auto;
}
.red {
	background-color: rgba(255, 0, 0, 0.3);
}
.green {
	background-color: rgba(0, 255, 0, 0.3);
}

.floated-right {
	float: right;
	margin-top: 8px;
}
.alert {
	text-align: center;
	font-size: 2em;
	padding-top: 50px;
}
</style>
