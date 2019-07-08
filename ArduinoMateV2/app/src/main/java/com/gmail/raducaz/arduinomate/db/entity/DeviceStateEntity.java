package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

import java.io.Serializable;
import java.util.Date;

import com.gmail.raducaz.arduinomate.model.DeviceState;

@Entity(tableName = "deviceState",
        foreignKeys = {
                @ForeignKey(entity = DeviceEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class DeviceStateEntity implements DeviceState, Serializable {

    private long deviceId;
    private int state;
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
    public int getState() {
        return state;
    }
    public void setState(int state) {
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

    public DeviceStateEntity() {
    }

    public DeviceStateEntity(long deviceId, int state, Date fromDate, Date toDate) {
        this.deviceId = deviceId;
        this.state = state;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public DeviceStateEntity(DeviceState state) {
        this.deviceId = state.getDeviceId();
        this.state = state.getState();
        this.fromDate = state.getFromDate();
        this.toDate = state.getToDate();
    }
}
