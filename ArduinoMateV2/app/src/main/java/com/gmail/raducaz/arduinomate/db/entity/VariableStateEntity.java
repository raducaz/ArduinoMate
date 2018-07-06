package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.VariableState;

import java.util.Date;

@Entity(tableName = "variableState",
        foreignKeys = {
                @ForeignKey(entity = VariableStateEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class VariableStateEntity implements VariableState {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long deviceId;
    private String name;
    private String state;
    private Date fromDate;
    private Date toDate;

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
    public String getState() {
        return state;
    }
    public void setState(String state) {
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

    public VariableStateEntity() {
    }

    public VariableStateEntity(long id, long deviceId, String name, String state, Date fromDate, Date toDate) {
        this.id = id;
        this.deviceId = deviceId;
        this.name = name;
        this.state = state;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public VariableStateEntity(VariableState state) {
        this.id = state.getId();
        this.deviceId = state.getDeviceId();
        this.name = state.getName();
        this.state = state.getState();
        this.fromDate = state.getFromDate();
        this.toDate = state.getToDate();
    }
}
