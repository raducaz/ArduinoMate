package com.gmail.raducaz.arduinomate.db.dao;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.gmail.raducaz.arduinomate.db.entity.JoinExecutionXLog;
import com.gmail.raducaz.arduinomate.model.JoinExecutionXExecutionLog;

import java.util.List;

@Dao
public interface JoinExecutionXExecutionLogDao {

    @Query("SELECT f.name, l.id, l.date, l.log FROM executionLog l " +
            "LEFT JOIN functionExecution f on l.executionId=f.id " +
            "ORDER BY Date DESC")
    LiveData<List<JoinExecutionXExecutionLog>> loadAllExecutionLogs();

}

