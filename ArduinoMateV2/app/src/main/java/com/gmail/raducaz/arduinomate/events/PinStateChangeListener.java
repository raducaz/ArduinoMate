package com.gmail.raducaz.arduinomate.events;

public interface PinStateChangeListener extends java.util.EventListener {
    void pinStateChanged(PinStateChangeEvent e);
}
