package com.gmail.raducaz.arduinomate.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.ExecutionLog;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "executionLog",
        foreignKeys = {
                @ForeignKey(entity = FunctionExecutionEntity.class,
                        parentColumns = "id",
                        childColumns = "executionId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "executionId")
        })
public class ExecutionLogEntity implements ExecutionLog, Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long executionId;
    private Date date;
    private String log;
    private long functionId;
    private String functionName;

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getExecutionId() {
        return executionId;
    }
    public void setExecutionId(long executionId) {
        this.executionId = executionId;
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

    @Override
    public long getFunctionId() {
        return functionId;
    }
    public void setFunctionId(long functionId) {
        this.functionId = functionId;
    }
    @Override
    public String getFunctionName() {
        return functionName;
    }
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public ExecutionLogEntity() {
    }

    @Ignore
    public ExecutionLogEntity(long executionId, Date date, String log, String functionName) {
        this.executionId = executionId;
        this.date = date;
        this.log = log;
        this.functionName = functionName;
    }

    @Ignore
    public ExecutionLogEntity(ExecutionLog log) {
        this.executionId = log.getExecutionId();
        this.date = log.getDate();
        this.log = log.getLog();
        this.functionName = log.getFunctionName();
    }
}
