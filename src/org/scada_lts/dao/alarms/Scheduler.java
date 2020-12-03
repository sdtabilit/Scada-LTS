package org.scada_lts.dao.alarms;

import java.io.Serializable;

public class Scheduler implements Serializable {

    private Long id;
    private boolean perMail;
    private boolean perSms;
    private int hourStart;
    private int hourStop;
    private String description;
    private String mtime;

    public Scheduler() {
    }

    public Scheduler(Long id, boolean per_mail, boolean per_sms, int hour_start, int hour_stop, String description, String mtime) {
        this.id = id;
        this.perMail = per_mail;
        this.perSms = per_sms;
        this.hourStart = hour_start;
        this.hourStop = hour_stop;
        this.description = description;
        this.mtime = mtime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getPerMail() {
        return perMail;
    }

    public void setPerMail(boolean per_mail) {
        this.perMail = per_mail;
    }

    public boolean getPerSms() {
        return perSms;
    }

    public void setPerSms(boolean per_sms) {
        this.perSms = per_sms;
    }

    public int getHourStart() {
        return hourStart;
    }

    public void setHourStart(int hour_start) {
        this.hourStart = hour_start;
    }

    public int getHourStop() {
        return hourStop;
    }

    public void setHourStop(int hour_stop) {
        this.hourStop = hour_stop;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMtime() {
        return mtime;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }
}

