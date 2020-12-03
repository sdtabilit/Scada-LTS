package org.scada_lts.dao.alarms;

import java.io.Serializable;

public class MailingListPlcNotification implements Serializable {

    private Long mailingListId;
    private Long dataPointId;
    private boolean perEmail;
    private boolean perSms;

    public MailingListPlcNotification() {
    }

    public MailingListPlcNotification(Long mailingListId, Long dataPointId, boolean perEmail, boolean perSms) {
        this.mailingListId = mailingListId;
        this.dataPointId = dataPointId;
        this.perEmail = perEmail;
        this.perSms = perSms;
    }

    public Long getMailingListId() {
        return mailingListId;
    }

    public void setMailingListId(Long mailingListId) {
        this.mailingListId = mailingListId;
    }

    public Long getDataPointId() {
        return dataPointId;
    }

    public void setDataPointId(Long dataPointId) {
        this.dataPointId = dataPointId;
    }

    public boolean isPerEmail() {
        return perEmail;
    }

    public void setPerEmail(boolean perEmail) {
        this.perEmail = perEmail;
    }

    public boolean isPerSms() {
        return perSms;
    }

    public void setPerSms(boolean perSms) {
        this.perSms = perSms;
    }
}
