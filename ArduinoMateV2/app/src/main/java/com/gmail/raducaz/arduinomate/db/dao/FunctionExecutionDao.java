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
    List<FunctionExecutionEntity> loadLastFunctionExecutionSync(long functionId);

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

}

