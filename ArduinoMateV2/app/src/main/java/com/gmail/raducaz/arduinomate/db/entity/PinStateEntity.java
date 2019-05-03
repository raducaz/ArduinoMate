package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.icu.util.TimeUnit;

import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.model.FunctionState;
import com.gmail.raducaz.arduinomate.model.PinState;

import java.util.Date;

@Entity(tableName = "pinState",
        foreignKeys = {
                @ForeignKey(entity = DeviceEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class PinStateEntity implements PinState {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long deviceId;
    private String name;
    private double state;
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
    @Override
    public String getStateText() {
        return String.format("%.2f", state);
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
    public String getSecondsFromLastUpdateText() {
        long s = getSecondsFromLastUpdate();
        return String.valueOf(s>100?"[>100s]":"["+s+"s]");
    }

    public PinStateEntity() {
    }

    public PinStateEntity(long id, long deviceId, String name, int state, Date fromDate, Date toDate) {
        this.id = id;
        this.deviceId = deviceId;
        this.name = name;
        this.state = state;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public PinStateEntity(FunctionState state) {
        this.id = state.getId();
        this.deviceId = state.getDeviceId();
        this.name = state.getName();
        this.state = state.getState();
        this.fromDate = state.getFromDate();
        this.toDate = state.getToDate();
    }
}
