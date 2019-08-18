package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.RemoteQueue;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "remoteQueue")
public class RemoteQueueEntity implements RemoteQueue, Serializable {
    @PrimaryKey
    @NonNull
    private String name;
    private Date date;

    @Override
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }


    public RemoteQueueEntity() {
    }
}
