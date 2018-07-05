package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface PinState {

    long getDeviceId();
    String getName();
    double getState();
    Date getFromDate();
    Date getToDate();
}