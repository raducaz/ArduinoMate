package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import java.util.Date;

public interface Function {
    int getId();
    int getDeviceId();
    String getText();
    String getLog();
    void setLog(String log);
    Date getDateSample();
}
