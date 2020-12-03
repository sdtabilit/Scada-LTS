package org.scada_lts.dao.alarms;

public class SchedulerUserData extends Scheduler {

    private Long dataPointId;
    private String userEmail;
    private String userPhone;

    public SchedulerUserData() {
    }

    public SchedulerUserData(Long id, boolean per_mail, boolean per_sms, int hour_start, int hour_stop, String description, String mtime, Long dataPointId, String userEmail, String userPhone) {
        super(id, per_mail, per_sms, hour_start, hour_stop, description, mtime);
        this.dataPointId = dataPointId;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
    }

    public Long getDataPointId() {
        return dataPointId;
    }

    public void setDataPointId(Long dataPointId) {
        this.dataPointId = dataPointId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
