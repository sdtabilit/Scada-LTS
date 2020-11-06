<template>
  <div class="container-fluid">
    <div class="col-md-9 mainbar" v-if="isUserRoleAdmin">
      <h1 class="col-xs-12">
        {{ $t("systemsettings.title") }}
        <btn
          type="primary"
          v-if="
            isEmailSettingsEdited ||
            isHttpSettingsEdited ||
            isMiscSettingsEdited ||
            isAuditEventEdited ||
            isSystemEventEdited
          "
          @click="openModal = !openModal"
          class="floated-right"
        >
          {{ $t("systemsettings.label.save") }} <i class="col-xs-2 glyphicon glyphicon-floppy-disk"></i>
        </btn>
      </h1>
      <div v-if="auditEventTypes" class="col-md-6">
        <div class="row align-items-center">
          <h2 class="col-xs-12">{{ $t("systemsettings.audit.title") }}<span v-if="isAuditEventEdited">*</span></h2>
        </div>

        <div>
          <div
            class="row"
            v-for="event in auditEventTypes"
            v-bind:key="event.i1"
          >
            <div class="col-xs-6">
              <p>{{ $t(event.translation) }}</p>
            </div>
            <div class="col-xs-5">
              <select
                class="form-control"
                v-model="event.i2"
                @change="editAuditEventWatch()"
                @input="editAuditEventWatch()"
              >
                <option v-bind:value="0">{{ $t("alarmlevels.none") }}</option>
                <option v-bind:value="1">
                  {{ $t("alarmlevels.information") }}
                </option>
                <option v-bind:value="2">{{ $t("alarmlevels.urgent") }}</option>
                <option v-bind:value="3">
                  {{ $t("alarmlevels.critical") }}
                </option>
                <option v-bind:value="4">
                  {{ $t("alarmlevels.lifesafety") }}
                </option>
              </select>
            </div>
            <div class="col-xs-1">
              <img
                v-if="event.i2 === 1"
                src="images/flag_blue.png"
                title="Information"
                alt="Information"
              />
              <img
                v-if="event.i2 === 2"
                src="images/flag_yellow.png"
                title="Urgent"
                alt="Urgent"
              />
              <img
                v-if="event.i2 === 3"
                src="images/flag_orange.png"
                title="Critical"
                alt="Critical"
              />
              <img
                v-if="event.i2 === 4"
                src="images/flag_red.png"
                title="Life Safety"
                alt="Life Safety"
              />
            </div>
          </div>
        </div>
      </div>
      <div v-if="systemEventTypes" class="col-md-6">
        <div class="row align-items-center">
          <h2 class="col-xs-12">{{ $t("systemsettings.event.title") }}<span v-if="isSystemEventEdited">*</span></h2>
        </div>
        <div
          class="row"
          v-for="event in systemEventTypes"
          v-bind:key="event.i1"
        >
          <div class="col-xs-6">
            <p>{{ $t(event.translation) }}</p>
          </div>
          <div class="col-xs-5">
            <select
              class="form-control"
              v-model="event.i2"
              @change="editSystemEventWatch()"
              @input="editSystemEventWatch()"
            >
              <option v-bind:value="0">{{ $t("alarmlevels.none") }}</option>
              <option v-bind:value="1">
                {{ $t("alarmlevels.information") }}
              </option>
              <option v-bind:value="2">{{ $t("alarmlevels.urgent") }}</option>
              <option v-bind:value="3">{{ $t("alarmlevels.critical") }}</option>
              <option v-bind:value="4">
                {{ $t("alarmlevels.lifesafety") }}
              </option>
            </select>
          </div>
          <div class="col-xs-1">
            <img
              v-if="event.i2 === 1"
              src="images/flag_blue.png"
              title="Information"
              alt="Information"
            />
            <img
              v-if="event.i2 === 2"
              src="images/flag_yellow.png"
              title="Urgent"
              alt="Urgent"
            />
            <img
              v-if="event.i2 === 3"
              src="images/flag_orange.png"
              title="Critical"
              alt="Critical"
            />
            <img
              v-if="event.i2 === 4"
              src="images/flag_red.png"
              title="Life Safety"
              alt="Life Safety"
            />
          </div>
        </div>
      </div>
      <div v-if="emailSettings" class="col-md-6">
        <div class="row align-items-center">
          <h2 class="col-xs-11">{{ $t("systemsettings.email.title") }}<span v-if="isEmailSettingsEdited">*</span></h2>
          <div class="col-xs-1">
            <btn size="lg" type="link" @click="sendTestEmail()" id="btn-test-email">
              <i class="glyphicon glyphicon-send"></i>
            </btn>
            <tooltip :text="$t('systemsettings.tooltip.sendtestemail')" target="#btn-test-email"/>
          </div>
        </div>

        <div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.email.host") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="text"
                v-model="emailSettings.host"
                @change="editEmailWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.email.port") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="number"
                v-model="emailSettings.port"
                @change="editEmailWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.email.address") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="text"
                v-model="emailSettings.from"
                @change="editEmailWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.email.name") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="text"
                v-model="emailSettings.name"
                @change="editEmailWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.email.contentType") }}</p>
            </div>
            <div class="col-xs-6">
              <select
                class="form-control"
                v-model="emailSettings.contentType"
                @input="editEmailWatch()"
              >
                <option v-bind:value="0">
                  {{ $t("systemsettings.email.contenttype.htmltext") }}
                </option>
                <option v-bind:value="1">
                  {{ $t("systemsettings.email.contenttype.html") }}
                </option>
                <option v-bind:value="2">
                  {{ $t("systemsettings.email.contenttype.text") }}
                </option>
              </select>
            </div>
          </div>
          <div class="row no-gutters">
            <div class="col-xs-12">
              <btn-group class="col-xs-12">
                <btn
                  input-type="radio"
                  :input-value="true"
                  v-model="emailSettings.auth"
                  class="col-xs-6"
                  @click="editEmailRadioWatch(true, 'auth')"
                  >{{ $t("systemsettings.email.auth.enable") }}</btn
                >
                <btn
                  input-type="radio"
                  :input-value="false"
                  v-model="emailSettings.auth"
                  class="col-xs-6"
                  @click="editEmailRadioWatch(false, 'auth')"
                  >{{ $t("systemsettings.email.auth.disable") }}</btn
                >
              </btn-group>
            </div>
          </div>
          <div v-if="emailSettings.auth">
            <div class="row">
              <div class="col-xs-6">
                <p>{{ $t("systemsettings.email.username") }}</p>
              </div>
              <div class="col-xs-6">
                <input
                  class="form-control"
                  type="text"
                  v-model="emailSettings.username"
                  @change="editEmailWatch()"
                />
              </div>
            </div>
            <div class="row">
              <div class="col-xs-6">
                <p>{{ $t("systemsettings.email.password") }}</p>
              </div>
              <div class="col-xs-6">
                <div class="col-xs-2">
                  <btn @click="togglePassword('emailPasswordInput')">
                    <i class="glyphicon glyphicon-eye-open"></i>
                  </btn>
                </div>
                <div class="col-xs-10">
                  <input
                    id="emailPasswordInput"
                    class="form-control"
                    type="password"
                    v-model="emailSettings.password"
                    @change="editEmailWatch()"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="row no-gutters">
            <div class="col-xs-12">
              <btn-group class="col-xs-12">
                <btn
                  input-type="radio"
                  :input-value="true"
                  v-model="emailSettings.tls"
                  class="col-xs-6"
                  @click="editEmailRadioWatch(true, 'tls')"
                  >{{ $t("systemsettings.email.tls.enable") }}</btn
                >
                <btn
                  input-type="radio"
                  :input-value="false"
                  v-model="emailSettings.tls"
                  class="col-xs-6"
                  @click="editEmailRadioWatch(false, 'tls')"
                  >{{ $t("systemsettings.email.tls.disable") }}</btn
                >
              </btn-group>
            </div>
          </div>
        </div>
      </div>
      <div v-if="httpSettings" class="col-md-6">
        <div class="row align-items-center">
          <h2 class="col-xs-12">{{ $t("systemsettings.http.title") }}<span v-if="isHttpSettingsEdited">*</span></h2>
        </div>
        <div>
          <div class="row no-gutters">
            <div class="col-xs-12 no-gutters">
              <btn-group class="col-xs-12 no-gutters">
                <btn
                  input-type="radio"
                  :input-value="true"
                  class="col-xs-6"
                  v-model="httpSettings.useProxy"
                  @click="editHttpRadioWatch(true)"
                  >{{ $t("systemsettings.http.proxy.enable") }}</btn
                >
                <btn
                  input-type="radio"
                  :input-value="false"
                  class="col-xs-6"
                  v-model="httpSettings.useProxy"
                  @click="editHttpRadioWatch(false)"
                  >{{ $t("systemsettings.http.proxy.disable") }}</btn
                >
              </btn-group>
            </div>
          </div>
        </div>
        <div v-if="httpSettings.useProxy">
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.http.proxy.host") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="text"
                v-model="httpSettings.host"
                @change="editHttpWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.http.proxy.port") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="number"
                v-model="httpSettings.port"
                @change="editHttpWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.http.proxy.username") }}</p>
            </div>
            <div class="col-xs-6">
              <input
                class="form-control"
                type="text"
                v-model="httpSettings.username"
                @change="editHttpWatch()"
              />
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.http.proxy.password") }}</p>
            </div>
            <div class="col-xs-6">
              <div class="col-xs-2">
                <btn @click="togglePassword('httpPasswordInput')">
                  <i class="glyphicon glyphicon-eye-open"></i>
                </btn>
              </div>
              <div class="col-xs-10">
                <input
                  id="httpPasswordInput"
                  class="form-control"
                  type="password"
                  v-model="httpSettings.password"
                  @change="editHttpWatch()"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="miscSettings" class="col-md-6">
        <div class="row align-items-center">
          <h2 class="col-xs-11">{{ $t("systemsettings.misc.title") }}<span v-if="isMiscSettingsEdited">*</span></h2>
          <div class="col-xs-1">
            <btn size="lg" type="link" @click="purgeData()" id="btn-purge-data">
              <i class="glyphicon glyphicon-trash"></i>
            </btn>
            <tooltip :text="$t('systemsettings.tooltip.purgedata')" target="#btn-purge-data"/>
          </div>
          
        </div>
        <div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.misc.uiPerformance") }}</p>
            </div>
            <div class="col-xs-6">
              <select
                class="form-control"
                v-model="miscSettings.uiPerformance"
                @input="editMiscWatch()"
              >
                <option v-bind:value="2000">
                  {{ $t("systemsettings.misc.performance.high") }}
                </option>
                <option v-bind:value="5000">
                  {{ $t("systemsettings.misc.performance.medium") }}
                </option>
                <option v-bind:value="10000">
                  {{ $t("systemsettings.misc.performance.low") }}
                </option>
              </select>
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.misc.purge.events") }}</p>
            </div>
            <div class="col-xs-3">
              <input
                class="form-control"
                type="number"
                v-model="miscSettings.eventPurgePeriods"
                @change="editMiscWatch()"
              />
            </div>
            <div class="col-xs-3">
              <select
                class="form-control"
                v-model="miscSettings.eventPurgePeriodType"
                @input="editMiscWatch()"
              >
                <option v-bind:value="4">{{ $t("timeperiod.days") }}</option>
                <option v-bind:value="5">{{ $t("timeperiod.weeks") }}</option>
                <option v-bind:value="6">{{ $t("timeperiod.months") }}</option>
                <option v-bind:value="7">{{ $t("timeperiod.years") }}</option>
              </select>
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.misc.purge.reports") }}</p>
            </div>
            <div class="col-xs-3">
              <input
                class="form-control"
                type="number"
                v-model="miscSettings.reportPurgePeriods"
                @change="editMiscWatch()"
              />
            </div>
            <div class="col-xs-3">
              <select
                class="form-control"
                v-model="miscSettings.reportPurgePeriodType"
                @input="editMiscWatch()"
              >
                <option v-bind:value="4">{{ $t("timeperiod.days") }}</option>
                <option v-bind:value="5">{{ $t("timeperiod.weeks") }}</option>
                <option v-bind:value="6">{{ $t("timeperiod.months") }}</option>
                <option v-bind:value="7">{{ $t("timeperiod.years") }}</option>
              </select>
            </div>
          </div>
          <div class="row">
            <div class="col-xs-6">
              <p>{{ $t("systemsettings.misc.purge.future") }}</p>
            </div>
            <div class="col-xs-3">
              <input
                class="form-control"
                type="number"
                v-model="miscSettings.futureDateLimitPeriods"
                @change="editMiscWatch()"
              />
            </div>
            <div class="col-xs-3">
              <select
                class="form-control"
                v-model="miscSettings.futureDateLimitPeriodType"
                @input="editMiscWatch()"
              >
                <option v-bind:value="2">{{ $t("timeperiod.minutes") }}</option>
                <option v-bind:value="3">{{ $t("timeperiod.hours") }}</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <div v-if="scadaConfig" class="col-md-6">
        <div class="row align-items-center">
          <h2 class="col-xs-12" id="title-scada-config">{{ $t("systemsettings.scadaconf.title") }}</h2>
          <tooltip :text="$t('systemsettings.tooltip.scadaconf')" target="#title-scada-config"/>
          <btn v-if="!showScadaConfig" type="link" class="col-xs-12" block @click="showScadaConfig = true"><i class="glyphicon glyphicon-menu-down"></i></btn>
          <btn v-if="showScadaConfig" type="link" class="col-xs-12" block @click="showScadaConfig = false"><i class="glyphicon glyphicon-menu-up"></i></btn>
        </div>
        <collapse v-model="showScadaConfig">

          <div class="row" v-for="key in Object.keys(scadaConfig)" v-bind:key="key">
            <div class="col-xs-8">
              <p>{{ $t(`systemsettings.scadaconf.${key}`) }}</p>
            </div>
            <div class="col-xs-4">
              <p>{{ scadaConfig[key] | convert }}</p>
            </div>
          </div>

        </collapse>
      </div>
    </div>
    <div class="col-md-3 sidebar" v-if="isUserRoleAdmin">
      <div v-if="systemInfoSettings" class="col-xs-12">
        <div class="row">
          <h2 class="col-xs-12">
            <IsAlive
              class="col-xs-12"
              :plabel="$t('systemsettings.info.title')"
              :ptime-refresh="4000"
              :ptime-warning="9000"
              :ptime-error="11000"
            ></IsAlive>
          </h2>
        </div>
        <div class="row" v-if="systemRunningTime">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.info.systemtime") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ systemRunningTime }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.info.milestone") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ $store.getters.appMilestone }} build
              {{ $store.getters.appBuild }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.info.branch") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ $store.getters.appBranch }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.info.tag") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ $store.getters.appTag }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.info.uiversion") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ $store.getters.appVersion }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-6">
            <p>{{ $t("systemsettings.info.instance") }}</p>
          </div>
          <div class="col-xs-6">
            <input
              class="form-control"
              type="text"
              v-model="systemInfoSettings.instanceDescription"
              @change="saveSystemInfoSettings()"
            />
          </div>
        </div>
        <div class="row">
          <div class="col-xs-6">
            <p>{{ $t("systemsettings.info.language") }}</p>
          </div>
          <div class="col-xs-6">
            <select
              class="form-control"
              v-model="systemInfoSettings.language"
              @input="saveSystemInfoSettings()"
            >
              <option value="de">Deutsch</option>
              <option value="en">English</option>
              <!-- <option value="ru">Руссиан</option>
            <option value="fi">Suomi</option>
            <option value="pl">Polski</option> -->
            </select>
          </div>
        </div>
      </div>
      <div v-if="databaseType" class="col-xs-12">
        <div class="row">
          <h2 class="col-xs-10">{{ $t("systemsettings.database.title") }}</h2>
          <btn
            class="col-xs-2"
            size="lg"
            type="link"
            @click="initDatabaseSection()"
          >
            <i class="glyphicon glyphicon-refresh"></i>
          </btn>
        </div>
        <div class="row">
          <div class="col-xs-4">
            <p>{{ $t("systemsettings.database") }}</p>
          </div>
          <div class="col-xs-8">
            <btn-group class="col-xs-12" justified>
              <btn
                input-type="radio"
                input-value="mysql"
                class="col-xs-6"
                v-model="databaseType"
                @click="saveDatabase('mysql')"
                >{{ $t("systemsettings.database.mysql") }}</btn
              >
              <btn
                disabled
                id="btn-db-derby"
                input-type="radio"
                input-value="derby"
                class="col-xs-6"
                v-model="databaseType"
                @click="saveDatabase('derby')"
                >{{ $t("systemsettings.database.derby") }}</btn
              >
            </btn-group>
            <tooltip :text="$t('systemsettings.tooltip.derbydatabase')" target="#btn-db-derby"/>
          </div>
        </div>
        <div class="row" v-if="schemaVersion">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.database.schemaVersion") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ schemaVersion }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.database.size") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ databaseInfo.databaseSize }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.database.file") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ databaseInfo.filedataCount }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.database.total") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ databaseInfo.filedataSize }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.database.histroy") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ databaseInfo.historyCount }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-8">
            <p>{{ $t("systemsettings.database.event") }}</p>
          </div>
          <div class="col-xs-4">
            <p>
              {{ databaseInfo.eventCount }}
            </p>
          </div>
        </div>
        <div class="row">
          <div class="col-xs-12">
            <p>{{ $t("systemsettings.database.top") }}</p>
          </div>
          <div
            class="col-xs-12"
            v-for="point in databaseInfo.topPoints"
            v-bind:key="point.pointId"
          >
            <p>
              {{ point.pointName }} (Point ID: {{ point.pointId }} - count:
              {{ point.count }})
            </p>
          </div>
        </div>
      </div>
    </div>
    <modal v-model="openModal" :title="$t('systemsettings.label.summary')" size="lg">
      <div v-if="isAuditEventEdited">
        <h3>{{ $t("systemsettings.audit.title") }}</h3>
        <div v-for="(element, index) in auditEventTypes" v-bind:key="index">
          <div
            class="col-xs-12"
            v-if="auditEventTypes[index].i2 !== storeAuditEventTypes[index].i2"
          >
            <p class="col-xs-4">
              {{ $t(element.translation) }}
            </p>
            <p class="col-xs-4 red">{{ storeAuditEventTypes[index].i2 | convertInfoLevel }}</p>
            <p class="col-xs-4 green">
              {{ element.i2 | convertInfoLevel }}
            </p>
          </div>
        </div>
        <div class="col-xs-12">
          <btn size="sm" @click="restoreAuditEventAlarms()"
            ><i class="glyphicon glyphicon-floppy-remove"></i> {{ $t("systemsettings.label.restore") }}</btn
          >
          <btn size="sm" @click="saveAuditEventAlarms()" type="primary"
            ><i class="glyphicon glyphicon-floppy-saved"></i> {{ $t("systemsettings.label.save") }}</btn
          >
        </div>
      </div>
      <div v-if="isSystemEventEdited">
        <h3>{{ $t("systemsettings.event.title") }}</h3>
        <div v-for="(element, index) in systemEventTypes" v-bind:key="index">
          <div
            class="col-xs-12"
            v-if="
              systemEventTypes[index].i2 !== storeSystemEventTypes[index].i2
            "
          >
            <p class="col-xs-4">
              {{ $t(element.translation) }}
            </p>
            <p class="col-xs-4 red">{{ storeSystemEventTypes[index].i2 | convertInfoLevel }}</p>
            <p class="col-xs-4 green">
              {{ element.i2 | convertInfoLevel }}
            </p>
          </div>
        </div>
        <div class="col-xs-12">
          <btn size="sm" @click="restoreSystemEventAlarms()"
            ><i class="glyphicon glyphicon-floppy-remove"></i> {{ $t("systemsettings.label.restore") }}</btn
          >
          <btn size="sm" @click="saveSystemEventAlarms()" type="primary"
            ><i class="glyphicon glyphicon-floppy-saved"></i> {{ $t("systemsettings.label.save") }}</btn
          >
        </div>
      </div>
      <div v-if="isEmailSettingsEdited">
        <h3>{{ $t("systemsettings.email.title") }}</h3>
        <div v-for="key in Object.keys(emailSettings)" v-bind:key="key">
          <div
            class="col-xs-12"
            v-if="emailSettings[key] !== storeEmailSettings[key]"
          >
            <p class="col-xs-4">{{ $t(`systemsettings.email.${key}`) }}</p>
            <p class="col-xs-4 red">
              {{ storeEmailSettings[key] | blank | convert }}
            </p>
            <p class="col-xs-4 green">
              {{ emailSettings[key] | blank | convert }}
            </p>
          </div>
        </div>
        <div class="col-xs-12">
          <btn size="sm" @click="restoreEmailSettings()"
            ><i class="glyphicon glyphicon-floppy-remove"></i> {{ $t("systemsettings.label.restore") }}</btn
          >
          <btn size="sm" @click="saveEmailSettings()" type="primary"
            ><i class="glyphicon glyphicon-floppy-saved"></i> {{ $t("systemsettings.label.save") }}</btn
          >
        </div>
      </div>
      <div v-if="isHttpSettingsEdited">
        <h3>{{ $t("systemsettings.http.title") }}</h3>
        <div v-for="key in Object.keys(httpSettings)" v-bind:key="key">
          <div
            class="col-xs-12"
            v-if="httpSettings[key] !== storeHttpSettings[key]"
          >
            <p class="col-xs-4">{{ $t(`systemsettings.http.proxy.${key}`) }}</p>
            <p class="col-xs-4 red">
              {{ storeHttpSettings[key] | blank | convert }}
            </p>
            <p class="col-xs-4 green">
              {{ httpSettings[key] | blank | convert }}
            </p>
          </div>
        </div>
        <div class="col-xs-12">
          <btn size="sm" @click="restoreHttpSettings()"
            ><i class="glyphicon glyphicon-floppy-remove"></i> {{ $t("systemsettings.label.restore") }}</btn
          >
          <btn size="sm" @click="saveHttpSettings()" type="primary"
            ><i class="glyphicon glyphicon-floppy-saved"></i> {{ $t("systemsettings.label.save") }}</btn
          >
        </div>
      </div>
      <div v-if="isMiscSettingsEdited">
        <h3>{{ $t("systemsettings.misc.title") }}</h3>
        <div v-for="key in Object.keys(miscSettings)" v-bind:key="key">
          <div
            class="col-xs-12"
            v-if="miscSettings[key] !== storeMiscSettings[key]"
          >
            <p class="col-xs-4">{{ $t(`systemsettings.misc.${key}`) }}</p>
            <p class="col-xs-4 red">
              {{
                storeMiscSettings[key]
                  | blank
                  | convert
                  | convertTimePeriod(key)
              }}
            </p>
            <p class="col-xs-4 green">
              {{ miscSettings[key] | blank | convert | convertTimePeriod(key) }}
            </p>
          </div>
        </div>
        <div class="col-xs-12">
          <btn size="sm" @click="restoreMiscSettings()"
            ><i class="glyphicon glyphicon-floppy-remove"></i> {{ $t("systemsettings.label.restore") }}</btn
          >
          <btn size="sm" @click="saveMiscSettings()" type="primary"
            ><i class="glyphicon glyphicon-floppy-saved"></i> {{ $t("systemsettings.label.save") }}</btn
          >
        </div>
      </div>
      <div slot="footer">
        <btn @click="openModal = false">{{$t("uiv.modal.cancel")}}</btn>
        <btn type="primary" @click="saveAllSettings()">{{ $t("systemsettings.label.saveall") }}</btn>
      </div>
    </modal>
    <div class="col-md-12"  v-if="!isUserRoleAdmin">
      <p class="alert">Not allowed to see that page</p>
    </div>
  </div>
</template>
<script>
import store from "../store";
import i18n from "../i18n";
import IsAlive from "../components/graphical_views/IsAlive";
import { keys } from "@amcharts/amcharts4/.internal/core/utils/Object";

export default {
  el: "#systemsettings",
  name: "systemsettings",
  components: {
    IsAlive,
  },
  filters: {
    blank: function (value) {
      if (value === "") return "---";
      return value;
    },
    convert: function (value) {
      if (typeof value === "boolean") {
        if (value) {
          return i18n.t("uiv.enable");
        } else {
          return i18n.t("uiv.disable");
        }
      }
      return value;
    },
    convertTimePeriod: function (value, key) {
      if (key.includes("Type")) {
        switch (Number(value)) {
          case 2:
            return i18n.t("timeperiod.minutes");
            break;
          case 3:
            return i18n.t("timeperiod.hours");
            break;
          case 4:
            return i18n.t("timeperiod.days");
            break;
          case 5:
            return i18n.t("timeperiod.weeks");
            break;
          case 6:
            return i18n.t("timeperiod.months");
            break;
          case 7:
            return i18n.t("timeperiod.years");
            break;
        }
      }
      return value;
    },
    convertInfoLevel: function (value) {
      switch (Number(value)) {
        case 0:
          return i18n.t("alarmlevels.none");
          break;
        case 1:
          return i18n.t("alarmlevels.information");
          break;
        case 2:
          return i18n.t("alarmlevels.urgent");
          break;
        case 3:
          return i18n.t("alarmlevels.critical");
          break;
        case 4:
          return i18n.t("alarmlevels.lifesafety");
          break;
      }
      return value;
    },
  },
  data() {
    return {
      isUserRoleAdmin: false,
      systemRunningTime: undefined,
      openModal: false,
      showScadaConfig: false,
      emailSettings: undefined,
      isEmailSettingsEdited: false,
      httpSettings: undefined,
      isHttpSettingsEdited: false,
      miscSettings: undefined,
      isMiscSettingsEdited: false,
      auditEventTypes: undefined,
      isAuditEventEdited: false,
      systemEventTypes: undefined,
      isSystemEventEdited: false,  
    };
  },
  mounted() {
    if(!this.isUserRoleAdmin) {
      this.getUserRole()
    }
    store.dispatch("getAllSettings").then(() => {
      this.emailSettings = JSON.parse(JSON.stringify(this.storeEmailSettings));
      this.httpSettings = JSON.parse(JSON.stringify(this.storeHttpSettings));
      this.miscSettings = JSON.parse(JSON.stringify(this.storeMiscSettings));
      this.auditEventTypes = JSON.parse(
        JSON.stringify(this.storeAuditEventTypes)
      );
      this.systemEventTypes = JSON.parse(
        JSON.stringify(this.storeSystemEventTypes)
      );
    });
    this.initDatabaseSection();
    this.loadClock();
  },
  methods: {
    async getUserRole() {
      this.isUserRoleAdmin = await store.dispatch("getUserRole")
    },
    initDatabaseSection() {
      store.dispatch("getDatabaseSize");
      store.dispatch("getSchemaVersion");
    },
    purgeData() {
      this.$confirm({
        title: i18n.t("systemsettings.alert.purgedata.title"),
        content: i18n.t("systemsettings.alert.purgedata"),
      }).then(() => {
        store.dispatch("purgeData").then((resp) => {
          if (resp === true) {
            this.getDatabaseSize();
            this.generateNotification(
              "success",
              i18n.t("systemsettings.notification.purgedata")
            );
          }
        });
      });
    },
    saveSystemInfoSettings() {
      this.$store.dispatch("saveSystemInfoSettings").then((resp) => {
        if (resp) {
          i18n.locale = this.systemInfoSettings.language;
          this.generateNotification(
            "success",
            i18n.t("systemsettings.notification.save.systeminfo")
          );
        } else {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        }
      });
    },
    editEmailWatch() {
      this.isEmailSettingsEdited = !this.configurationEqual(
        this.emailSettings,
        this.storeEmailSettings
      );
    },
    editEmailRadioWatch(value, parameter) {
      if(parameter == "auth") {
        this.emailSettings.auth = value;
      } else if (parameter == "tls") {
        this.emailSettings.tls = value
      }
      this.editEmailWatch();
    },
    restoreEmailSettings() {
      this.emailSettings = JSON.parse(JSON.stringify(this.storeEmailSettings));
      this.editEmailWatch();
    },
    saveEmailSettings() {
      this.$store.commit("setEmailSettings", this.emailSettings);
      this.$store
        .dispatch("saveEmailSettings")
        .then((resp) => {
          if (resp) {
            this.restoreEmailSettings();
            this.generateNotification(
              "success",
              i18n.t("systemsettings.notification.save.email")
            );
          }
        })
        .catch(() => {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        });
    },
    sendTestEmail() {
      this.$store
        .dispatch("sendTestEmail")
        .then((resp) => {
          if (resp) {
            this.generateNotification(
              "success",
              `${i18n.t("systemsettings.notification.send.email")} ${resp.recipient}`
            );
          }
        })
        .catch(() => {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        });

    },
    editHttpWatch() {
      this.isHttpSettingsEdited = !this.configurationEqual(
        this.httpSettings,
        this.storeHttpSettings
      );
    },
    editHttpRadioWatch(value) {
      this.httpSettings.useProxy = value
      this.editHttpWatch();
    },
    restoreHttpSettings() {
      this.httpSettings = JSON.parse(JSON.stringify(this.storeHttpSettings));
      this.editHttpWatch();
    },
    saveHttpSettings() {
      this.$store.commit("setHttpSettings", this.httpSettings);
      this.$store
        .dispatch("saveHttpSettings")
        .then((resp) => {
          if (resp) {
            this.restoreHttpSettings();
            this.generateNotification(
              "success",
              i18n.t("systemsettings.notification.save.http")
            );
          }
        })
        .catch(() => {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        });
    },
    editMiscWatch() {
      this.isMiscSettingsEdited = !this.configurationEqual(
        this.miscSettings,
        this.storeMiscSettings
      );
    },
    restoreMiscSettings() {
      this.miscSettings = JSON.parse(JSON.stringify(this.storeMiscSettings));
      this.editMiscWatch();
    },
    saveMiscSettings() {
      this.$store.commit("setMiscSettings", this.miscSettings);
      this.$store
        .dispatch("saveMiscSettings")
        .then((resp) => {
          if (resp) {
            this.restoreMiscSettings();
            this.generateNotification(
              "success",
              i18n.t("systemsettings.notification.save.misc")
            );
          }
        })
        .catch(() => {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        });
    },
    editSystemEventWatch() {
      for (let i = 0; i < this.auditEventTypes.length; i++) {
        this.isSystemEventEdited = !this.configurationEqual(
          this.systemEventTypes[i],
          this.storeSystemEventTypes[i]
        );
        if (this.isSystemEventEdited) {
          break;
        }
      }
    },
    restoreSystemEventAlarms() {
      this.systemEventTypes = JSON.parse(
        JSON.stringify(this.storeSystemEventTypes)
      );
      this.editSystemEventWatch();
    },
    saveSystemEventAlarms() {
      this.$store.commit("setSystemEventTypes", this.systemEventTypes);
      this.$store
        .dispatch("saveSystemEventTypes")
        .then((resp) => {
          if (resp) {
            this.restoreSystemEventAlarms();
            this.generateNotification(
              "success",
              i18n.t("systemsettings.notification.save.systemevent")
            );
          }
        })
        .catch(() => {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        });
    },
    editAuditEventWatch() {
      for (let i = 0; i < this.auditEventTypes.length; i++) {
        this.isAuditEventEdited = !this.configurationEqual(
          this.auditEventTypes[i],
          this.storeAuditEventTypes[i]
        );
        if (this.isAuditEventEdited) {
          break;
        }
      }
    },
    restoreAuditEventAlarms() {
      this.auditEventTypes = JSON.parse(
        JSON.stringify(this.storeAuditEventTypes)
      );
      this.editAuditEventWatch();
    },
    saveAuditEventAlarms() {
      this.$store.commit("setAuditEventTypes", this.auditEventTypes);
      this.$store
        .dispatch("saveAuditEventTypes")
        .then((resp) => {
          if (resp) {
            this.restoreAuditEventAlarms();
            this.generateNotification(
              "success",
              i18n.t("systemsettings.notification.save.auditevent")
            );
          }
        })
        .catch(() => {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        });
    },
    saveDatabase(databaseType) {
      this.$store.dispatch("saveDatabaseType", databaseType).then((resp) => {
        if (resp) {
          this.generateNotification(
            "success",
            i18n.t("systemsettings.notification.save.database")
          );
        } else {
          this.generateNotification(
            "danger",
            i18n.t("systemsettings.notification.fail")
          );
        }
      });
    },
    saveAllSettings() {
      if (this.isEmailSettingsEdited) {
        this.saveEmailSettings();
      }
      if (this.isHttpSettingsEdited) {
        this.saveHttpSettings();
      }
      if (this.isMiscSettingsEdited) {
        this.saveMiscSettings();
      }
      if (this.isAuditEventEdited) {
        this.saveAuditEventAlarms();
      }
      if (this.isSystemEventEdited) {
        this.saveSystemEventAlarms();
      }
      this.openModal = false;
    },
    generateNotification(type, content) {
      this.$notify({
        placement: "bottom-right",
        type,
        content,
      });
    },
    togglePassword(elementId) {
      let x = document.getElementById(elementId);
      if (x.type === "password") {
        x.type = "text";
      } else {
        x.type = "password";
      }
    },
    async loadClock() {
      let result = await store.dispatch("getSystemStartupTime");
      if (result) {
        this.runClock();
      }
    },
    runClock() {
      setInterval(() => {
        let now = new Date().getTime();
        let distance = now - this.systemStartupTime.getTime();
        let days = Math.floor(distance / (1000 * 60 * 60 * 24));
        let hours = Math.floor(
          (distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        );
        let minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        let seconds = Math.floor((distance % (1000 * 60)) / 1000);
        this.systemRunningTime = `${days}d ${hours}h ${minutes}m ${seconds}s`;
      }, 1000);
    },
    configurationEqual(object1, object2) {
      const keys1 = Object.keys(object1);
      const keys2 = Object.keys(object2);
      if (keys1.length !== keys2.length) {
        return false;
      }
      for (let key of keys1) {
        if (object1[key] !== object2[key]) {
          return false;
        }
      }
      return true;
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
    storeEmailSettings() {
      return this.$store.state.systemSettings.emailSettings;
    },
    storeHttpSettings() {
      return this.$store.state.systemSettings.httpSettings;
    },
    storeMiscSettings() {
      return this.$store.state.systemSettings.miscSettings;
    },
    storeAuditEventTypes() {
      return this.$store.state.systemSettings.auditEventTypes;
    },
    storeSystemEventTypes() {
      return this.$store.state.systemSettings.systemEventTypes;
    },
    systemStartupTime() {
      return this.$store.state.systemSettings.systemStartupTime;
    },
    schemaVersion() {
      return this.$store.state.systemSettings.schemaVersion;
    },
    scadaConfig() {
      return this.$store.state.systemSettings.scadaConfig;
    }
  },
};
</script>
<style scoped>
.saving {
  animation: fadeinfadeout 2s;
  animation-iteration-count: infinite;
}
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
.modal-footer{
  border: none;
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
@keyframes fadeinfadeout {
  0% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}
</style>