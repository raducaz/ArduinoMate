package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.Date;

@Entity(tableName = "function",
        foreignKeys = {
                @ForeignKey(entity = DeviceEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class FunctionEntity implements Function {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long deviceId;
    private String name;
    private String description;
    private String log;
    private int resultState;
    private int callState;
    private Date dateSample;

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
    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getResultState() {
        return resultState;
    }
    public void setResultState(int resultState) {
        this.resultState = resultState;
    }
    @Override
    public String getResultStateText() {
        return String.valueOf(FunctionResultStateEnum.forInt(resultState));
    }
    @Override
    public int getCallState() {
        return callState;
    }
    public void setCallState(int callState) {
        this.resultState = callState;
    }
    @Override
    public String getCallStateText() {
        return String.valueOf(FunctionCallStateEnum.forInt(callState));
    }

    @Override
    public Date getDateSample() {
        return dateSample;
    }

    public void setDateSample(Date dateSample) {
        this.dateSample = dateSample;
    }

    public FunctionEntity() {
    }

    public FunctionEntity(long id, long deviceId, String name, String log, int resultState,int callState, Date dateSample) {
        this.id = id;
        this.deviceId = deviceId;
        this.name = name;
        this.log = log;
        this.resultState = resultState;
        this.callState = callState;
        this.dateSample = dateSample;
    }

    public FunctionEntity(Function function) {
        this.id = function.getId();
        this.deviceId = function.getDeviceId();
        this.name = function.getName();
        this.description = function.getDescription();
        this.resultState = function.getResultState();
        this.callState = function.getCallState();
    }
}