package com.gmail.raducaz.arduinomate.model;

import java.util.Date;

public interface VariableState {

    long getDeviceId();
    String getName();
    //TODO: This should be enhanced by separating variables by type so formulas can easlybe defined on Android
    String getState();
    Date getFromDate();
    Date getToDate();
}