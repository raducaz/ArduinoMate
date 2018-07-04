package com.gmail.raducaz.arduinomate;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.model.Function;

import java.util.List;

/**
 * Repository handling the work with devices and functions.
 */
public class DataRepository {

    private static DataRepository sInstance;
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

    private final AppDatabase mDatabase;
    private DataRepository(final AppDatabase database) {
        mDatabase = database;

    }

    //region Device
    /**
     * Get the list of devices from the database and get notified when the data changes.
     */
    public MediatorLiveData<List<DeviceEntity>> getDevices() {
        MediatorLiveData<List<DeviceEntity>> mObservableDevices;
        mObservableDevices = new MediatorLiveData<>();

        mObservableDevices.addSource(mDatabase.deviceDao().loadAllDevices(),
                deviceEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableDevices.postValue(deviceEntities);
                    }
                });

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
    //endregion Device

    //region Function
    /**
     * Get the list of devices from the database and get notified when the data changes.
     */
    public MediatorLiveData<List<FunctionEntity>> getFunctions() {
        MediatorLiveData<List<FunctionEntity>> mObservableFunctions;
        mObservableFunctions = new MediatorLiveData<>();

        mObservableFunctions.addSource(mDatabase.functionDao().loadAllFunctions(),
                functionEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableFunctions.postValue(functionEntities);
                    }
                });

        return mObservableFunctions;
    }
    public LiveData<List<FunctionEntity>> loadFunctions(final int deviceId) {
        return mDatabase.functionDao().loadDeviceFunctions(deviceId);
    }
    public LiveData<FunctionEntity> loadFunction(final int functionId) {
        return mDatabase.functionDao().loadFunction(functionId);
    }
    public FunctionEntity loadFunctionSync(final int functionId) {
        return mDatabase.functionDao().loadFunctionSync(functionId);
    }
    public void insertFunction(FunctionEntity function) {
        mDatabase.functionDao().insert(function);
    }
    public void updateFunction(FunctionEntity function) {
        mDatabase.functionDao().update(function);
    }
    //endregion Function

    //region FunctionExecution
    public LiveData<List<FunctionExecutionEntity>> loadFunctionExecutions(final int deviceId, String functionName) {
        return mDatabase.functionExecutionDao().loadFunctionExecutions(deviceId, functionName);
    }

    public LiveData<FunctionExecutionEntity> loadLastFunctionExecution(final int deviceId, String functionName) {
        return mDatabase.functionExecutionDao().loadLastFunctionExecution(deviceId, functionName);
    }
    public long insertFunctionExecution(FunctionExecutionEntity execution) {
        return mDatabase.functionExecutionDao().insert(execution);
    }
    public void updateFunctionExecution(FunctionExecutionEntity execution) {
        mDatabase.functionExecutionDao().update(execution);
    }
    //endregion FunctionExecution

    //region ExecutionLog
    public LiveData<List<ExecutionLogEntity>> loadExecutionLog(final int executionId) {
        return mDatabase.executionLogDao().loadExecutionLogs(executionId);
    }
    public void insertExecutionLog(ExecutionLogEntity log) {
        mDatabase.executionLogDao().insert(log);
    }
    public void updateExecutionLog(ExecutionLogEntity log) {
        mDatabase.executionLogDao().update(log);
    }
    //endregion ExecutionLog
}