package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.FunctionExecution;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.Date;

@Entity(tableName = "functionExecution",
        foreignKeys = {
                @ForeignKey(entity = FunctionEntity.class,
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
    private int callState;
    private int resultState;
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
    public int getCallState() {
        return callState;
    }
    public void setCallState(int callState) {
        this.callState = callState;
    }
    @Override
    public String getCallStateText() {
        return String.valueOf(FunctionCallStateEnum.forInt(callState));
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

    public FunctionExecutionEntity(long id, long functionId, String name, int callState, int resultState, Date startDate, Date endDate) {
        this.id = id;
        this.functionId = functionId;
        this.name = name;
        this.callState = callState;
        this.resultState = resultState;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public FunctionExecutionEntity(FunctionExecution execution) {
        this.id = execution.getId();
        this.functionId = execution.getFunctionId();
        this.name = execution.getName();
        this.callState = execution.getCallState();
        this.resultState = execution.getResultState();
        this.startDate = execution.getStartDate();
        this.endDate = execution.getEndDate();
    }
}
