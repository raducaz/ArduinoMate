package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface PinStateChange {

    long getId();
    long getDeviceId();
    String getDeviceIp();
    String getDeviceName();
    long getNo();
    String getName();
    double getState();
    double getOldState();
    Date getFromDate();
    Date getToDate();
    Date getLastUpdate();
    int getSecondsFromLastUpdate();
    long getStateLifeDuration();
}