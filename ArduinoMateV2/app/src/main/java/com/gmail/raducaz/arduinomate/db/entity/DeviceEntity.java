package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.Device;

@Entity(tableName = "device")
public class DeviceEntity implements Device {
    @PrimaryKey
    private long id;
    private String ip;
    private int port;
    private String name;
    private String description;

    @Override
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DeviceEntity() {
    }

    public DeviceEntity(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public DeviceEntity(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.description = device.getDescription();
    }
}
