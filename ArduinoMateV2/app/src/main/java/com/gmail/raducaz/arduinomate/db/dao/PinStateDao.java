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

import com.gmail.raducaz.arduinomate.db.entity.PinStateChangeEntity;
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
            "ORDER BY no")
    LiveData<List<PinStateEntity>> loadDeviceCurrentPinsState(long deviceId);

    // fromDate is updated when a pinState changes, lastUpdate is updated everytime a new state occurs (disrigard the value is identical)
    @Query("SELECT pinState.*, device.ip as deviceIp, device.name as deviceName FROM pinState " +
            "inner join device on pinState.deviceId = device.id " +
            "where fromDate = lastUpdate " +
            "ORDER BY deviceId")
    LiveData<List<PinStateChangeEntity>> loadChangedPinsState();

    // fromDate is updated when a pinState changes, lastUpdate is updated everytime a new state occurs (disrigard the value is identical)
    @Query("SELECT pinState.*, device.ip as deviceIp, device.name as deviceName FROM pinState " +
            "inner join device on pinState.deviceId = device.id " +
            "where fromDate = lastUpdate AND device.ip = :deviceIp " )
    LiveData<List<PinStateChangeEntity>> loadChangedPinsState(String deviceIp);

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
            "SET oldState = state," +
            "state = :pinState," +
            "fromDate = :stateFrom, " + // Reset fromDate as pinState changed
            "lastUpdate = :stateFrom " + // Also update last update date to current date
            "WHERE deviceId = :deviceId AND name = :pinName ")
    void updatePinState(long deviceId, String pinName, Double pinState, Date stateFrom); //date('now') is not working in Query

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

