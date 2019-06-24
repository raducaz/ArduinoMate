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

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;

import java.util.List;

@Dao
public interface FunctionDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FunctionEntity function);

    @Query(
            "UPDATE function SET " +
                    "callState = :callState," +
                    "resultState = :resultState " +
            "WHERE id = :id"
    )
    void updateStates(long id, int callState, int resultState);

    @Query(
            "UPDATE function SET " +
                    "callState = :callState," +
                    "resultState = :resultState ")
    void updateAllFunctionStates(int callState, int resultState);

    @Query(
            "UPDATE function SET " +
                    "isAutoEnabled = :isChecked " +
                    "WHERE id = :id"
    )
    void updateAutoEnabled(long id, boolean isChecked);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FunctionEntity function);

    @Query("SELECT * FROM function ")
    LiveData<List<FunctionEntity>> loadAllFunctions();

    @Query("SELECT * FROM function where deviceId = :deviceId")
    LiveData<List<FunctionEntity>> loadDeviceFunctions(long deviceId);

    @Query("SELECT * FROM function where deviceId = :deviceId")
    List<FunctionEntity> loadDeviceFunctionsSync(long deviceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FunctionEntity> functions);

    @Query("select * from function where id = :functionId")
    LiveData<FunctionEntity> loadFunction(long functionId);

    @Query("select * from function where id = :functionId")
    FunctionEntity loadFunctionSync(long functionId);

    @Query("select * from function where deviceId = :deviceId and name = :functionName")
    FunctionEntity loadFunctionSync(long deviceId, String functionName);
}

