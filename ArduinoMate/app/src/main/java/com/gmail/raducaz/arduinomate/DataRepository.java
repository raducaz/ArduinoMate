package com.gmail.raducaz.arduinomate;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

import java.util.List;

/**
 * Repository handling the work with devices and functions.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<DeviceEntity>> mObservableDevices;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableDevices = new MediatorLiveData<>();

        mObservableDevices.addSource(mDatabase.deviceDao().loadAllDevices(),
                deviceEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableDevices.postValue(deviceEntities);
                    }
                });
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of devices from the database and get notified when the data changes.
     */
    public LiveData<List<DeviceEntity>> getDevices() {
        return mObservableDevices;
    }

    public LiveData<DeviceEntity> loadDevice(final int deviceId) {
        return mDatabase.deviceDao().loadDevice(deviceId);
    }
    public DeviceEntity loadDeviceSync(final int deviceId) {
        return mDatabase.deviceDao().loadDeviceSync(deviceId);
    }
    public void insertDevice(DeviceEntity device) {
        mDatabase.deviceDao().insert(device);
    }
    public void updateDevice(DeviceEntity device) {
        mDatabase.deviceDao().update(device);
    }

    public LiveData<List<FunctionEntity>> loadFunctions(final int deviceId) {
        return mDatabase.functionDao().loadFunctions(deviceId);
    }

    public LiveData<FunctionEntity> loadFunction(final int functionId) {
        return mDatabase.functionDao().loadFunction(functionId);
    }
    public void updateFunction(FunctionEntity function) {
        mDatabase.functionDao().update(function);
    }
}