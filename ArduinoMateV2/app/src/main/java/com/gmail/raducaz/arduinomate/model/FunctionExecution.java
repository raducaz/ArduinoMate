package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface FunctionExecution {

    long getId();
    long getFunctionId();
    String getName();
    Date getStartDate();
    Date getEndDate();
    int getCallState();
    int getResultState();
}