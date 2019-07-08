package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.Device;
import com.gmail.raducaz.arduinomate.model.Settings;

import java.io.Serializable;

@Entity(tableName = "settings")
public class SettingsEntity implements Settings, Serializable {
    @PrimaryKey
    private long id;

    private String amqUri;
    private boolean isController;
    private boolean isTestingMode;
    private boolean permitRemoteControl;

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getAmqUri() {
        return amqUri;
    }
    public void setAmqUri(String amqUri) {
        this.amqUri = amqUri;
    }

    @Override
    public boolean getIsTestingMode() {
        return isTestingMode;
    }
    public void setIsTestingMode(boolean isTestingMode) {
        this.isTestingMode = isTestingMode;
    }

    @Override
    public boolean getIsController() {
        return isController;
    }
    public void setIsController(boolean isController) {
        this.isController = isController;
    }

    @Override
    public boolean getPermitRemoteControl() {
        return permitRemoteControl;
    }
    public void setPermitRemoteControl(boolean permitRemoteControl) {
        this.permitRemoteControl = permitRemoteControl;
    }


    public SettingsEntity() {
    }
}
