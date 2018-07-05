package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface DeviceState {

    long getDeviceId();
    int getState();
    Date getFromDate();
    Date getToDate();
}