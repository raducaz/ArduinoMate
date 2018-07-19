package com.gmail.raducaz.arduinomate.db.dao;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;

import java.util.List;

@Dao
public interface ExecutionLogDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ExecutionLogEntity function);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExecutionLogEntity function);

    @Query("SELECT * FROM executionLog " +
            "where executionId = :executionId")
    LiveData<List<ExecutionLogEntity>> loadExecutionLogs(long executionId);

    @Query("SELECT * FROM executionLog where executionId = :executionId")
    List<ExecutionLogEntity> loadExecutionLogsSync(long executionId);
}

