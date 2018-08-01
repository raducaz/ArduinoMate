package com.gmail.raducaz.arduinomate.service;

public enum FunctionResultStateEnum {
    NA          (-1),  //calls constructor with value -1
    OFF         (0),
    ON          (1),
    ERROR       (2)
    ; // semicolon needed when fields / methods follow

    private final int id;

    FunctionResultStateEnum(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public static FunctionResultStateEnum forInt(int id) {
        for (FunctionResultStateEnum state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid State id: " + id);
    }
}
