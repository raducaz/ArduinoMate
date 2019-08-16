package com.gmail.raducaz.arduinomate.db.dao;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import java.util.List;

@Dao
public interface MockPinStateDao {

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(MockPinStateEntity pinState);

    @Query(
            "UPDATE mockPinState SET " +
                    "state = :state " +
                    "WHERE id = :id"
    )
    void updateById(long id, double state);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MockPinStateEntity pinState);

    @Query("SELECT * FROM mockPinState " +
            "WHERE deviceName = :deviceName " +
            "ORDER BY number")
    List<MockPinStateEntity> loadDevicePinsStateSync(String deviceName);

    @Query("SELECT * FROM mockPinState " +
            "WHERE deviceName = :deviceName " +
            "AND number = :pinNo")
    MockPinStateEntity loadDevicePinStateSync(String deviceName, int pinNo);

    @Query("DELETE FROM mockPinState " +
            "WHERE deviceName = :deviceName ")
    void deletePinStatesByDevice(String deviceName);

    @Query("DELETE FROM mockPinState ")
    void deleteAllPinStates();
}

