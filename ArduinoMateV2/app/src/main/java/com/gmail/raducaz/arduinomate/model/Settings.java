package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


public interface Settings {
    long getId();
    boolean getIsController();
    boolean getPermitRemoteControl();
    boolean getIsTestingMode();
}