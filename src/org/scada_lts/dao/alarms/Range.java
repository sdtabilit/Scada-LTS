package org.scada_lts.dao.alarms;

import java.io.Serializable;

public class Range implements Serializable {

    private Long id;
    private int hourStart;
    private int hourStop;
    private String description;

    public Range() {
    }

    public Range(Long id, int hourStart, int hourStop, String description) {
        this.id = id;
        this.hourStart = hourStart;
        this.hourStop = hourStop;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getHourStart() {
        return hourStart;
    }

    public void setHourStart(int hourStart) {
        this.hourStart = hourStart;
    }

    public int getHourStop() {
        return hourStop;
    }

    public void setHourStop(int hourStop) {
        this.hourStop = hourStop;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
