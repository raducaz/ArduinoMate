package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface PinState {

    long getId();
    long getDeviceId();
    String getName();
    double getState();
    String getStateText();
    Date getFromDate();
    Date getToDate();
    Date getLastUpdate();
    int getSecondsFromLastUpdate();
    String getSecondsFromLastUpdateText();
}