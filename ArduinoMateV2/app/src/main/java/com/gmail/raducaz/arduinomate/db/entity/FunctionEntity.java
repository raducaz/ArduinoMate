package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.Function;

import java.util.Date;

@Entity(tableName = "functions",
        foreignKeys = {
                @ForeignKey(entity = FunctionEntity.class,
                        parentColumns = "id",
                        childColumns = "deviceId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "deviceId")
        })
public class FunctionEntity implements Function {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int deviceId;
    private String text;
    private String log;
    private Date dateSample;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
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
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public FunctionEntity(int id, int deviceId, String text, String log, Date dateSample) {
        this.id = id;
        this.deviceId = deviceId;
        this.text = text;
        this.log = log;
        this.dateSample = dateSample;
    }
}