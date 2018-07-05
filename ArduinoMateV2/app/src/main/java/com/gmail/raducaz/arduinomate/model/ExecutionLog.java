package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface ExecutionLog {

    long getId();
    long getExecutionId();
    Date getDate();
    String getLog();
}