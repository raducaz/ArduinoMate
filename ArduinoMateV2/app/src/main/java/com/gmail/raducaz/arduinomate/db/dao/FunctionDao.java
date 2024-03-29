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
                    "resultState = :resultState " +
                    "WHERE deviceId = :deviceId"
    )
    void updateDeviceFunctionsStates(long deviceId, int callState, int resultState);

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
    @Query(
            "UPDATE function SET " +
                    "log = :log " +
                    "WHERE id = :id"
    )
    void updateLog(long id, String log);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FunctionEntity function);

    @Query("SELECT * FROM function ")
    LiveData<List<FunctionEntity>> loadAllFunctions();

    @Query("SELECT * FROM function where deviceId = :deviceId")
    LiveData<List<FunctionEntity>> loadDeviceFunctions(long deviceId);

    @Query("SELECT * FROM function where deviceId = :deviceId")
    List<FunctionEntity> loadDeviceFunctionsSync(long deviceId);

    @Query("SELECT * FROM function ")
    List<FunctionEntity> loadAllFunctionsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FunctionEntity> functions);

    @Query("select * from function where id = :functionId")
    LiveData<FunctionEntity> loadFunction(long functionId);

    @Query("select * from function where id = :functionId")
    FunctionEntity loadFunctionSync(long functionId);

    @Query("select function.* from function " +
            "inner join device on function.deviceId = deviceId " +
            "where device.name = :deviceName and function.name = :functionName")
    FunctionEntity loadDeviceFunctionSync(String deviceName, String functionName);

    @Query("select * from function where name = :functionName")
    FunctionEntity loadFunctionByNameSync(String functionName);

    @Query("select * from function where  deviceId = :deviceId and name = :functionName")
    FunctionEntity loadFunctionSync(long deviceId, String functionName);
}

