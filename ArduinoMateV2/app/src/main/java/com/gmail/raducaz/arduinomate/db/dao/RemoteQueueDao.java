package com.gmail.raducaz.arduinomate.db.dao;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.gmail.raducaz.arduinomate.db.entity.RemoteQueueEntity;

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
