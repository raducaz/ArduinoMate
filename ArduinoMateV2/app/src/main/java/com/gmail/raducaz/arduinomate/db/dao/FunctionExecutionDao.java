package com.gmail.raducaz.arduinomate.db.dao;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;

import java.util.List;

@Dao
public interface FunctionExecutionDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FunctionExecutionEntity functionExecution);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FunctionExecutionEntity functionExecution);

    @Query("SELECT * FROM functionExecution " +
            "where functionId = :functionId " +
            "ORDER BY startDate DESC LIMIT 1")
    LiveData<FunctionExecutionEntity> loadLastFunctionExecution(long functionId);

    @Query("SELECT * FROM functionExecution " +
            "where functionId = :functionId " +
            "ORDER BY startDate DESC LIMIT 1")
    FunctionExecutionEntity loadLastFunctionExecutionSync(long functionId);

    @Query("SELECT * FROM functionExecution " +
            "where functionId = :functionId and endDate is null " +
            "ORDER BY startDate DESC LIMIT 1")
    FunctionExecutionEntity loadLastUnfinishedFunctionExecutionSync(long functionId);

    @Query("SELECT * FROM functionExecution " +
            "where functionId = :functionId " +
            "ORDER BY startDate DESC")
    LiveData<List<FunctionExecutionEntity>> loadFunctionExecutions(long functionId);

    @Query("SELECT * FROM functionExecution " +
            "where functionId = :functionId " +
            "ORDER BY startDate DESC")
    List<FunctionExecutionEntity> loadFunctionExecutionsSync(long functionId);

    @Query("DELETE FROM functionExecution " +
            "where functionId = :functionId ")
    void deleteFunctionExecution(long functionId);

    @Query("DELETE FROM functionExecution ")
    void deleteAllFunctionExecution();

}

