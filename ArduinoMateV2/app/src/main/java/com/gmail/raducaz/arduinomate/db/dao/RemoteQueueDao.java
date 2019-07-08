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

import com.gmail.raducaz.arduinomate.db.entity.RemoteQueueEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.RemoteQueue;

import java.util.List;

@Dao
public interface RemoteQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RemoteQueueEntity remoteQueueEntity);

    @Query("SELECT * FROM remotequeue")
    LiveData<List<RemoteQueueEntity>> getRemoteQueues();

    @Query("SELECT * FROM remotequeue")
    List<RemoteQueueEntity> getRemoteQueuesSync();

    @Query("SELECT Count(*) FROM remotequeue")
    int getNumberOfAllRemoteQueues();

    @Query("select * from remotequeue WHERE name = :name")
    RemoteQueueEntity getRemoteQueueSync(String name);

    @Query("delete from remotequeue WHERE name = :name")
    void deleteRemoteQueueSync(String name);
}
