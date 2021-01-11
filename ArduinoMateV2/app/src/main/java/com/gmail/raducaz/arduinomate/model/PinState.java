package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface PinState {

    long getId();
    long getDeviceId();
    long getNo();
    String getName();
    double getState();
    String getStateText();
    double getOldState();
    String getOldStateText();
    Date getFromDate();
    Date getToDate();
    Date getLastUpdate();
    int getSecondsFromLastUpdate();
    String getSecondsFromLastUpdateText();
    long getStateLifeDuration();
    int getStateColor();
}