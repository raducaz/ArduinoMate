package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


public interface Device {
    long getId();
    String getIp();
    String getMac();
    int getPort();
    String getPortText();
    String getName();
    String getDescription();
    void setDescription(String description);
}