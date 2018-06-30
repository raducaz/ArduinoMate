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

import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;

import java.util.List;

@Dao
public interface FunctionDao {
    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FunctionEntity function);

    @Query("SELECT * FROM functions where deviceId = :deviceId")
    LiveData<List<FunctionEntity>> loadFunctions(int deviceId);

    @Query("SELECT * FROM functions where deviceId = :deviceId")
    List<FunctionEntity> loadFunctionsSync(int deviceId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FunctionEntity> functions);

    @Query("select * from functions where id = :functionId")
    LiveData<FunctionEntity> loadFunction(int functionId);

    @Query("select * from functions where id = :functionId")
    FunctionEntity loadFunctionSync(int functionId);
}

