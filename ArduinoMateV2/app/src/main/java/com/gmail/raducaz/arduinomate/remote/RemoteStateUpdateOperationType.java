package com.gmail.raducaz.arduinomate.remote;

public enum RemoteStateUpdateOperationType {
    INSERT       (0),
    UPDATE   (1),
    DELETE       (2)
    ; // semicolon needed when fields / methods follow

    private final int id;

    RemoteStateUpdateOperationType(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
