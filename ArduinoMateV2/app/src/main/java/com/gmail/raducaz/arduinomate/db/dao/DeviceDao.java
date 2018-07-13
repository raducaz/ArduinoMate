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

import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

import java.util.List;

@Dao
public interface DeviceDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DeviceEntity device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceEntity device);

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

    @Query("select * from device where id = :deviceIp")
    DeviceEntity loadDeviceSync(String deviceIp);
}
