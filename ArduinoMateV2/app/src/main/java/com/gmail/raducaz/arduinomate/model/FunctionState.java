package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface FunctionState {

    long getId();
    long getDeviceId();
    String getName();
    int getState();
    Date getFromDate();
    Date getToDate();
}