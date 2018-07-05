package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

import com.gmail.raducaz.arduinomate.model.FunctionState;
import com.gmail.raducaz.arduinomate.model.PinState;

import java.util.Date;

@Entity(tableName = "pinState",
        foreignKeys = {
                @ForeignKey(entity = PinStateEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class PinStateEntity implements PinState {

    private long deviceId;
    private String name;
    private double state;
    private Date fromDate;
    private Date toDate;

    @Override
    public long getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getState() {
        return state;
    }
    public void setState(double state) {
        this.state = state;
    }

    @Override
    public Date getFromDate() {
        return fromDate;
    }
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public Date getToDate() {
        return toDate;
    }
    public void setState(Date toDate) {
        this.toDate = toDate;
    }

    public PinStateEntity() {
    }

    public PinStateEntity(long deviceId, String name, int state, Date fromDate, Date toDate) {
        this.deviceId = deviceId;
        this.name = name;
        this.state = state;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public PinStateEntity(FunctionState state) {
        this.deviceId = state.getDeviceId();
        this.name = state.getName();
        this.state = state.getState();
        this.fromDate = state.getFromDate();
        this.toDate = state.getToDate();
    }
}
