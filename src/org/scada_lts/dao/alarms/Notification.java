package org.scada_lts.dao.alarms;

import java.io.Serializable;
import java.sql.Timestamp;

public class Notification implements Serializable {

    private Long id;
    private boolean perEmail;
    private boolean perSms;
    private String mtime;

    public Notification() {}

    public Notification(Long id, boolean perEmail, boolean perSms, String mtime) {
        this.id = id;
        this.perEmail = perEmail;
        this.perSms = perSms;
        this.mtime = mtime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }
}
