package com.gmail.raducaz.arduinomate.service;

public enum FunctionCallStateEnum {
    NA          (-1),  //calls constructor with value -1
    READY       (0),
    EXECUTING   (1),
    ERROR       (2)
    ; // semicolon needed when fields / methods follow

    private final int id;

    FunctionCallStateEnum(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public static FunctionCallStateEnum forInt(int id) {
        for (FunctionCallStateEnum state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid State id: " + id);
    }
}
