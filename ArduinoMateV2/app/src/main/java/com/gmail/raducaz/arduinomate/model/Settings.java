package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


public interface Settings {
    long getId();
    String getAmqUri();
    String getPhoneNumber();
    String getPhoneDeviceIp();
    boolean getIsController();
    boolean getPermitRemoteControl();
    boolean getIsTestingMode();
}