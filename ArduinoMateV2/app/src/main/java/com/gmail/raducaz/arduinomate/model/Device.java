package com.gmail.raducaz.arduinomate.model;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


public interface Device {
    int getId();
    String getIp();
    String getName();
    String getDescription();
    void setDescription(String description);
}