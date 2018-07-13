package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

import com.gmail.raducaz.arduinomate.model.DeviceLog;
import com.gmail.raducaz.arduinomate.model.DeviceState;

import java.util.Date;

@Entity(tableName = "deviceLog",
        foreignKeys = {
                @ForeignKey(entity = DeviceEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class DeviceLogEntity implements DeviceLog {

    private long deviceId;
    private Date date;
    private String log;

    @Override
    public long getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }

    public DeviceLogEntity() {
    }

    public DeviceLogEntity(long deviceId, Date date, String log) {
        this.deviceId = deviceId;
        this.date = date;
        this.log = log;
    }

    public DeviceLogEntity(DeviceLog log) {
        this.deviceId = log.getDeviceId();
        this.date = log.getDate();
        this.log = log.getLog();
    }
}
