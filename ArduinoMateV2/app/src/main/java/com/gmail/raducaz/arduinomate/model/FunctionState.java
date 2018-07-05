package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface FunctionState {

    long getDeviceId();
    String getName();
    int getState();
    Date getFromDate();
    Date getToDate();
}