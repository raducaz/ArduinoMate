package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import com.gmail.raducaz.arduinomate.model.JoinExecutionXExecutionLog;

import java.util.Date;

@Entity
public class JoinExecutionXLog implements JoinExecutionXExecutionLog {

    public JoinExecutionXLog() {
    }

//    @Embedded
//    FunctionExecution functionExecution;
//
//    @Embedded
//    ExecutionLog executionLog;

    private long id;
    private Date date;
    private String log;
    private String name;

//    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

//    @Override
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

//    @Override
    public String getLog() {
        return log;
    }
    public void setLog(String log) {
        this.log = log;
    }

//    @Override
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public JoinExecutionXLog(long id, Date date, String log, String name) {
        this.id = id;
        this.date = date;
        this.log = log;
        this.name = name;
    }

    public JoinExecutionXLog(JoinExecutionXLog log) {
        this.id = log.getId();
        this.date = log.getDate();
        this.log = log.getLog();
        this.name = log.getName();
    }

}
