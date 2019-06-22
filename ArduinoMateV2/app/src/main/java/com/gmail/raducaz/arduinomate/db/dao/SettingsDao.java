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
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.Settings;

import java.util.List;

@Dao
public interface SettingsDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(SettingsEntity settingsEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SettingsEntity settingsEntity);

    @Query("SELECT * FROM settings LIMIT 1")
    LiveData<SettingsEntity> getSettings();

    @Query("select * from settings LIMIT 1")
    SettingsEntity getSettingsSync();
}
