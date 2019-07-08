package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import java.util.Date;

public interface Function {
    long getId();
    long getDeviceId();
    String getName();
    String getDescription();
    String getLog();
    void setLog(String log);
    boolean getIsAutoEnabled();
    Date getDateSample();

    int getResultState();
    String getResultStateText();
    int getStateColor();
    int getCallState();
    String getCallStateText();
}
