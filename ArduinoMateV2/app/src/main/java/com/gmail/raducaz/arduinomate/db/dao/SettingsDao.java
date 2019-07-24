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

import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;

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
