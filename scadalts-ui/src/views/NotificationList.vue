<template>
  <div class="container-fluid">
    <h1>{{ $t("notificationlist.title") }}</h1>
    <div class="col-xs-12">
      <div class="col-xs-4">
        <select class="form-control" v-model="selectedMailingList" @change="changeMailingList()">
          <option v-for="ml in mailingList" :key="ml.id" :value="ml">
            #{{ml.id}} - {{ ml.name }}
          </option>
        </select>
      </div>
      <!-- <div class="col-xs-2">
          <p>
              {{$t("notificationlist.select.label")}}
          </p>
      </div> -->
      <div class="col-xs-6">
      </div>
      <div class="col-xs-2">
          <btn block @click="saveConfiguration()">Save</btn>
      </div>
    </div>
    <div v-if="selectedMailingList">
      <div class="col-xs-12 table-header">
        <div class="col-xs-8">{{$t("notificationlist.table.pointname")}}</div>
        <div class="col-xs-2">{{$t("notificationlist.table.mail")}}</div>
        <div class="col-xs-2">{{$t("notificationlist.table.sms")}}</div>
      </div>
      <div class="col-xs-12 table-row" v-for="dp in mailingListPoints" :key="dp.id">
        <div class="col-xs-8">{{ dp.name }}</div>
        <div class="col-xs-2">
          <input
            type="checkbox"
            v-model="dp.perEmail"
            @click="editMailingListPoint(dp)"
          />
        </div>
        <div class="col-xs-2">
          <input
            type="checkbox"
            v-model="dp.perSms"
            @click="editMailingListPoint(dp)"
          />
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import store from "../store";
import i18n from "../i18n";

export default {
  el: "#notificationlist",
  name: "notificationlist",
  data() {
    return {
      selectedMailingList: undefined,
      mailingList: undefined,
      mailingListPlcNotification: undefined,
      mailingListPoints: undefined,
      dataPointList: undefined,
    };
  },
  mounted() {
    this.$store.dispatch("getMailingLists").then((r) => {
      console.debug(r);
      this.mailingList = r;
    });
    this.getDataPointList();
  },
  methods: {
    changeMailingList() {
      this.$store
        .dispatch("getMailingListPlcNotification", this.selectedMailingList.id)
        .then((res) => {
          console.debug(res);
          this.mailingListPlcNotification = res;
          this.mailingListPoints = [];
          this.dataPointList.forEach((e) => {
            let index = this.mailingListPlcNotification.findIndex(
              (m) => m.dataPointId == e.id
            );
            if (index != -1) {
              e.perSms = this.mailingListPlcNotification[index].perSms;
              e.perEmail = this.mailingListPlcNotification[index].perEmail;
              e.status = 0;
            } else {
              e.perSms = false;
              e.perEmail = false;
              e.status = -1;
            }
            this.mailingListPoints.push(e);
            console.log(e);
          });
        });
    },
    editMailingListPoint(e) {
      if (e.status == 0) {
        e.status = 1;
      }
    },
    saveConfiguration() {
      this.mailingListPoints.forEach((e) => {
        console.log(e.status);
        if (e.status != 0) {
          let mailingListEntity = {
            mailingListId: this.selectedMailingList.id,
            dataPointId: e.id,
            perEmail: e.perEmail,
            perSms: e.perSms,
          };
          if (e.status == 1) {
            this.$store.dispatch(
              "updateMailingListPlcNotification",
              mailingListEntity
            );
          } else if (e.status == -1) {
            this.$store.dispatch(
              "postMailingListPlcNotification",
              mailingListEntity
            );
          }
        }
      });
      this.$notify({title: 'Saved!', type: 'success'});
    },
    async getDataPointList() {
      this.dataPointList = await this.$store.dispatch("getDataPointList");
    },
  },
};
</script>
<style scoped>
.table-header {
  border-bottom: solid 1px #ddd;
  margin-bottom: 5px;
  margin-top: 15px;
  font-weight: bold;
  line-height: 15px;
}
.table-row:nth-of-type(2n+1){
    background-color: #0000002e;
}
</style>
<style>
body > .alert {
  z-index: 2000;
}
</style>