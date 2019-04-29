package com.gmail.raducaz.arduinomate.events;

public class PinStateChangeEvent extends java.util.EventObject {
    String pinName;
    Double pinState;
    public PinStateChangeEvent(Object source, String pinName, Double pinState) {
        super(source);
        this.pinName = pinName;
        this.pinState = pinState;
    }

    public String getPinName() {
        return pinName;
    }
    public Double getPinState() {
        return pinState;
    }
}
