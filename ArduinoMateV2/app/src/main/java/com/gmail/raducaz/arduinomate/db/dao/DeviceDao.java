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

import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

import java.util.List;

@Dao
public interface DeviceDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DeviceEntity device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DeviceEntity device);

    @Query("SELECT * FROM device")
    LiveData<List<DeviceEntity>> loadAllDevices();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DeviceEntity> devices);

    @Query("select * from device where id = :deviceId")
    LiveData<DeviceEntity> loadDevice(long deviceId);

    @Query("select * from device where id = :deviceId")
    DeviceEntity loadDeviceSync(long deviceId);

    @Query("select * from device where ip = :deviceIp")
    LiveData<DeviceEntity> loadDevice(String deviceIp);

    @Query("select * from device where ip = :deviceIp")
    DeviceEntity loadDeviceSync(String deviceIp);

    @Query("select * from device where name = :deviceName LIMIT 1")
    DeviceEntity loadDeviceByNameSync(String deviceName);
}
