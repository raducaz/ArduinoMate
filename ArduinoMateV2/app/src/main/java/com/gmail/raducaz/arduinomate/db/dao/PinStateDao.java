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
            "where deviceId = :deviceId AND name = :pinName " +
            "LIMIT 1")
    PinStateEntity loadDeviceCurrentPinStateSync(long deviceId, String pinName);

    @Query("UPDATE pinState " +
            "SET state = :pinState " +
            "WHERE deviceId = :deviceId AND name = :pinName ")
    void updatePinState(long deviceId, String pinName, Double pinState);

    @Query("DELETE FROM pinState " +
            "WHERE deviceId IN (" +
            "SELECT deviceId FROM function " +
            "WHERE id = :functionId" +
            ")")
    void deletePinStatesByFunction(long functionId);

    @Query("DELETE FROM pinState " +
            "WHERE toDate < :toDate")
    void deletePinStatesToDate(Date toDate);

    @Query("DELETE FROM pinState ")
    void deleteAllPinStates();
}

