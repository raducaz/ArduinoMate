package com.gmail.raducaz.arduinomate.db.entity;

import android.graphics.Color;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.model.FunctionState;
import com.gmail.raducaz.arduinomate.model.PinState;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

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

public class PinStateEntity implements PinState, Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long deviceId;
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
    @Override
    public String getStateText() {
        return String.format("%.2f", state);
    }
    public void setState(double state) {
        this.state = state;
    }

    @Override
    public double getOldState() {
        return oldState;
    }
    @Override
    public String getOldStateText() {
        return String.format("%.2f", oldState);
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
    public String getSecondsFromLastUpdateText() {
        long s = getSecondsFromLastUpdate();
        return String.valueOf(s>100?"[>100s]":"["+s+"s]");
    }

    @Override
    public long getStateLifeDuration()
    {
        Date now = DateConverter.toDate(System.currentTimeMillis());
        long diffInSec = Math.round((now.getTime() - fromDate.getTime())/1000);
        return diffInSec;
    }
    @Override
    public int getStateColor() {
        long diffInSec = getStateLifeDuration();

        if(diffInSec <= 2)
            return Color.argb(255, 0, 204, 0);
        else if(diffInSec <= 5)
            return Color.argb(255, 0, 255, 0);
        else if(diffInSec <= 10)
            return Color.argb(255, 51, 255, 153);
        else if(diffInSec <= 30)
            return Color.argb(255, 0, 204, 204);
        else if(diffInSec <= 60)
            return Color.argb(255, 0, 255, 255);
        else
            return Color.TRANSPARENT;
    }

    public PinStateEntity() {
    }

    @Ignore
    public PinStateEntity(long id, long deviceId, String name, int state, Date fromDate, Date toDate) {
        this.id = id;
        this.deviceId = deviceId;
        this.name = name;
        this.state = state;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Ignore
    public PinStateEntity(FunctionState state) {
        this.id = state.getId();
        this.deviceId = state.getDeviceId();
        this.name = state.getName();
        this.state = state.getState();
        this.fromDate = state.getFromDate();
        this.toDate = state.getToDate();
    }
}
