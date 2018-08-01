package com.gmail.raducaz.arduinomate.service;

public enum DeviceStateEnum {
    NA          (-1),  //calls constructor with value -1
    READY       (0),
    EXECUTING   (1),
    ERROR       (2)
    ; // semicolon needed when fields / methods follow

    private final int id;

    DeviceStateEnum(int id) {
        this.id = id;
    }

    public static DeviceStateEnum forInt(int id) {
        for (DeviceStateEnum state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid State id: " + id);
    }
}
