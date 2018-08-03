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
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;

import java.util.Date;
import java.util.List;

@Dao
public interface PinStateDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(PinStateEntity pinState);

    @Query(
            "UPDATE pinState SET " +
                    "toDate = :toDate, " +
                    "lastUpdate = :toDate " +
                    "WHERE id = :id"
    )
    void updateToDate(long id, Date toDate);
    @Query(
            "UPDATE pinState SET " +
                    "lastUpdate = :lastUpdate " +
                    "WHERE id = :id"
    )
    void updateLastUpdate(long id, Date lastUpdate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PinStateEntity pinState);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId " +
            "ORDER BY name, fromDate")
    LiveData<List<PinStateEntity>> loadDevicePinsStateHistory(long deviceId);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId " +
            "ORDER BY name, fromDate")
    List<PinStateEntity> loadDevicePinsStateHistorySync(long deviceId);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId AND name = :pinName " +
            "ORDER BY fromDate DESC")
    LiveData<List<PinStateEntity>> loadDevicePinStateHistory(long deviceId, String pinName);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId AND name = :pinName " +
            "ORDER BY fromDate DESC")
    List<PinStateEntity> loadDevicePinStateHistorySync(long deviceId, String pinName);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId AND toDate IS NULL " +
            "ORDER BY name")
    LiveData<List<PinStateEntity>> loadDeviceCurrentPinsState(long deviceId);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId AND toDate IS NULL " +
            "ORDER BY name")
    List<PinStateEntity> loadDeviceCurrentPinsStateSync(long deviceId);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId AND name = :pinName AND toDate IS NULL " +
            "LIMIT 1")
    LiveData<PinStateEntity> loadDeviceCurrentPinState(long deviceId, String pinName);

    @Query("SELECT * FROM pinState " +
            "where deviceId = :deviceId AND name = :pinName AND toDate IS NULL " +
            "LIMIT 1")
    PinStateEntity loadDeviceCurrentPinStateSync(long deviceId, String pinName);

    @Query("DELETE FROM pinState " +
            "WHERE deviceId IN (" +
            "SELECT deviceId FROM function " +
            "WHERE id = :functionId" +
            ")")
    void deletePinStatesByFunction(long functionId);
}

