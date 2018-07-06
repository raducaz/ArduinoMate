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
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.model.PinState;

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
    public LiveData<DeviceEntity> loadDevice(final long deviceId) {
        return mDatabase.deviceDao().loadDevice(deviceId);
    }
    public DeviceEntity loadDeviceSync(final long deviceId) {
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
    public LiveData<List<FunctionEntity>> loadFunctions(final long deviceId) {
        return mDatabase.functionDao().loadDeviceFunctions(deviceId);
    }
    public LiveData<FunctionEntity> loadFunction(final long functionId) {
        return mDatabase.functionDao().loadFunction(functionId);
    }
    public FunctionEntity loadFunctionSync(final long functionId) {
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
    public LiveData<List<FunctionExecutionEntity>> loadFunctionExecutions(final long functionId) {
        return mDatabase.functionExecutionDao().loadFunctionExecutions(functionId);
    }

    public LiveData<FunctionExecutionEntity> loadLastFunctionExecution(final long functionId) {
        return mDatabase.functionExecutionDao().loadLastFunctionExecution(functionId);
    }
    public long insertFunctionExecution(FunctionExecutionEntity execution) {
        return mDatabase.functionExecutionDao().insert(execution);
    }
    public void updateFunctionExecution(FunctionExecutionEntity execution) {
        mDatabase.functionExecutionDao().update(execution);
    }
    //endregion FunctionExecution

    //region ExecutionLog
    public LiveData<List<ExecutionLogEntity>> loadExecutionLog(final long executionId) {
        return mDatabase.executionLogDao().loadExecutionLogs(executionId);
    }
    public void insertExecutionLog(ExecutionLogEntity log) {
        mDatabase.executionLogDao().insert(log);
    }
    public void updateExecutionLog(ExecutionLogEntity log) {
        mDatabase.executionLogDao().update(log);
    }
    //endregion ExecutionLog

    //region PinState
    public LiveData<List<PinStateEntity>> loadDeviceCurrentPinsState(final long deviceId) {
        return mDatabase.pinStateDao().loadDeviceCurrentPinsState(deviceId);
    }
    public LiveData<List<PinStateEntity>> loadDevicePinsStateHistory(final long deviceId) {
        return mDatabase.pinStateDao().loadDevicePinsStateHistory(deviceId);
    }
    public LiveData<List<PinStateEntity>> loadDevicePinsStateHistory(final long deviceId, final String pinName) {
        return mDatabase.pinStateDao().loadDevicePinStateHistory(deviceId, pinName);
    }
    public LiveData<PinStateEntity> loadDeviceCurrentPinState(final long deviceId, final String pinName) {
        return mDatabase.pinStateDao().loadDeviceCurrentPinState(deviceId, pinName);
    }
    public void insertPinState(PinStateEntity pinState) {
        mDatabase.pinStateDao().insert(pinState);
    }
    public void updatePinState(PinStateEntity pinStateEntity) {
        mDatabase.pinStateDao().update(pinStateEntity);
    }
    //endregion PinState
}