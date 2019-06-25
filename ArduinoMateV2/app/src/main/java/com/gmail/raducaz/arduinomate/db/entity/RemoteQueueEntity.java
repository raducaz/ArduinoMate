package com.gmail.raducaz.arduinomate.db.entity;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.gmail.raducaz.arduinomate.model.RemoteQueue;
import com.gmail.raducaz.arduinomate.model.Settings;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "remoteQueue")
public class RemoteQueueEntity implements RemoteQueue, Serializable {
    @PrimaryKey
    private long id;

    private String name;
    private Date date;

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
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }


    public RemoteQueueEntity() {
    }
}
