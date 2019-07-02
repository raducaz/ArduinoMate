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

import java.util.Date;
import java.util.List;

@Dao
public interface ExecutionLogDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(ExecutionLogEntity executionLog);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ExecutionLogEntity executionLog);

    @Query("SELECT * FROM executionLog " +
            "where executionId = :executionId " +
            "ORDER BY Date DESC")
    LiveData<List<ExecutionLogEntity>> loadExecutionLogs(long executionId);

    @Query("SELECT * FROM executionLog where executionId = :executionId")
    List<ExecutionLogEntity> loadExecutionLogsSync(long executionId);

    @Query("SELECT * FROM executionLog " +
            "ORDER BY Date DESC")
    LiveData<List<ExecutionLogEntity>> loadAllExecutionLogs();

    @Query("DELETE FROM executionLog " +
            "WHERE executionId IN (" +
            "SELECT executionId FROM functionExecution " +
            "WHERE functionId = :functionId" +
            ")")
    void deleteFunctionExecutionLogs(long functionId);

    @Query("DELETE FROM executionLog " +
            "WHERE date < :toDate")
    void deleteFunctionExecutionLogsToDate(Date toDate);

    @Query("DELETE FROM executionLog ")
    void deleteAllFunctionExecutionLogs();
}

