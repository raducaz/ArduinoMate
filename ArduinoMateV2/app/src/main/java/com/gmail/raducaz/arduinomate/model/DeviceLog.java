package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface DeviceLog {

    long getDeviceId();
    Date getDate();
    String getLog();
}