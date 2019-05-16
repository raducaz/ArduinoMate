package com.gmail.raducaz.arduinomate.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.model.FunctionState;
import com.gmail.raducaz.arduinomate.model.MockPinState;
import com.gmail.raducaz.arduinomate.model.PinState;

import java.util.Date;

@Entity(tableName = "mockPinState")
public class MockPinStateEntity implements MockPinState {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int number;
    private String deviceName;
    private double state;

    @Override
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public double getState() {
        return state;
    }
    public void setState(double state) {
        this.state = state;
    }

    public MockPinStateEntity() {
    }

    @Ignore
    public MockPinStateEntity(long id, int number, String deviceName, int state) {
        this.id = id;
        this.number = number;
        this.deviceName = deviceName;
        this.state = state;
    }

    @Ignore
    public MockPinStateEntity(MockPinState state) {
        this.id = state.getId();
        this.number = state.getNumber();
        this.deviceName = state.getDeviceName();
        this.state = state.getState();
    }
}
