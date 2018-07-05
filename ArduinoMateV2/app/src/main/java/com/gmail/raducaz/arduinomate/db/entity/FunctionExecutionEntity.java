package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.FunctionExecution;

import java.util.Date;

@Entity(tableName = "functionExecution",
        foreignKeys = {
                @ForeignKey(entity = FunctionExecutionEntity.class,
                        parentColumns = "id",
                        childColumns = "functionId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "functionId")
        })
public class FunctionExecutionEntity implements FunctionExecution {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long functionId;
    private String name;
    private int state;
    private Date startDate;
    private Date endDate;

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getFunctionId() {
        return functionId;
    }
    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }

    @Override
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public FunctionExecutionEntity() {
    }

    public FunctionExecutionEntity(long id, long functionId, String name, int state, Date startDate, Date endDate) {
        this.id = id;
        this.functionId = functionId;
        this.name = name;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public FunctionExecutionEntity(FunctionExecution execution) {
        this.id = execution.getId();
        this.functionId = execution.getFunctionId();
        this.name = execution.getName();
        this.state = execution.getState();
        this.startDate = execution.getStartDate();
        this.endDate = execution.getEndDate();
    }
}
