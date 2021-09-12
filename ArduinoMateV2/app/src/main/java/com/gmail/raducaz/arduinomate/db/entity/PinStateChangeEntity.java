package com.gmail.raducaz.arduinomate.db.entity;

import android.graphics.Color;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.model.PinStateChange;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "pinState",
        foreignKeys = {
                @ForeignKey(entity = DeviceEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = {"deviceId", "name"},
                unique=true
        )})

public class PinStateChangeEntity implements PinStateChange, Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long deviceId;
    private String deviceIp;
    private String deviceName;

    private long no;
    private String name;
    private double state;
    private double oldState;
    private Date fromDate;
    private Date toDate;
    private Date lastUpdate;

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getDeviceIp() {
        return deviceIp;
    }
    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public long getNo() {
        return no;
    }
    public void setNo(long no) {
        this.no = no;
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
    public double getOldState() {
        return oldState;
    }
    public void setOldState(double oldState) {
        this.oldState = oldState;
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
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public int getSecondsFromLastUpdate() {
        Date now = DateConverter.toDate(System.currentTimeMillis());
        long diffInMillies = now.getTime() - lastUpdate.getTime();
        return Math.round(diffInMillies/1000);
    }

    @Override
    public long getStateLifeDuration()
    {
        Date now = DateConverter.toDate(System.currentTimeMillis());
        long diffInSec = Math.round((now.getTime() - fromDate.getTime())/1000);
        return diffInSec;
    }

    public PinStateChangeEntity() {
    }

    @Ignore
    public PinStateChangeEntity(long id, long deviceId, String deviceIp, String deviceName, String name, int state, Date fromDate, Date toDate) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceIp = deviceIp;
        this.deviceName = deviceName;
        this.name = name;
        this.state = state;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

}
