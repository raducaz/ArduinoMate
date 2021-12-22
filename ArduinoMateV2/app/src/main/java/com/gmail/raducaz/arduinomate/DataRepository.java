package com.gmail.raducaz.arduinomate;

/**
 * Created by Radu.Cazacu on 11/27/2017.
 */

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.db.AppDatabase;
import com.gmail.raducaz.arduinomate.db.converter.DateConverter;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.db.entity.MockPinStateEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateChangeEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.db.entity.RemoteQueueEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.ExecutionLog;
import com.gmail.raducaz.arduinomate.model.FunctionExecution;
import com.gmail.raducaz.arduinomate.model.RemoteQueue;
import com.gmail.raducaz.arduinomate.remote.RemoteStateUpdate;
import com.gmail.raducaz.arduinomate.remote.StateFromControllerPublisher;
import com.gmail.raducaz.arduinomate.service.FunctionCallStateEnum;
import com.gmail.raducaz.arduinomate.service.FunctionResultStateEnum;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.gmail.raducaz.arduinomate.ArduinoMateApp.AmqConnection;
import static com.gmail.raducaz.arduinomate.ArduinoMateApp.STATES_EXCHANGE;

/**
 * Repository handling the work with devices and functions.
 */
public class DataRepository {

    public static String getMqStateUpdateBuffer() {
        return mqStateUpdateBuffer;
    }

    private static String mqStateUpdateBuffer = "";
    public static void appendMqStateUpdateBuffer(String mqStateUpdateBuffer) {
        DataRepository.mqStateUpdateBuffer += "\r\n" + mqStateUpdateBuffer;
    }
    public static void clearMqStateUpdateBuffer() {
        DataRepository.mqStateUpdateBuffer = "";
    }

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
    StateFromControllerPublisher remoteStateSender;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
    }

    private void SendStateToRemoteClients(RemoteStateUpdate stateUpdate)
    {
        SettingsEntity settings = this.getSettingsSync();
        if(settings.getIsController() && settings.getPermitRemoteControl()) {
            StateFromControllerPublisher.SendState(this, AmqConnection, STATES_EXCHANGE, stateUpdate);
        }
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
    public DeviceEntity loadDeviceByNameSync(final String deviceName) {
        return mDatabase.deviceDao().loadDeviceByNameSync(deviceName);
    }
    public LiveData<DeviceEntity> loadDeviceByIp(final String deviceIp) {
        return mDatabase.deviceDao().loadDevice(deviceIp);
    }
    public DeviceEntity loadDeviceSync(final String deviceIp) {
        return mDatabase.deviceDao().loadDeviceSync(deviceIp);
    }

    public void insertDevice(DeviceEntity device) {
        long id = mDatabase.deviceDao().insert(device);
        device.setId(id);

        SendStateToRemoteClients(new RemoteStateUpdate(device, "insertDevice"));
    }
    public void updateDevice(DeviceEntity device) {
        mDatabase.deviceDao().update(device);

        SendStateToRemoteClients(new RemoteStateUpdate(device, "updateDevice"));
    }
    //endregion Device

    //region Settings
    public LiveData<SettingsEntity> getSettings() {
        return mDatabase.settingsDao().getSettings();
    }
    public SettingsEntity getSettingsSync() {
        return mDatabase.settingsDao().getSettingsSync();
    }
    public void updateSettings(SettingsEntity settings) {
        mDatabase.settingsDao().update(settings);
    }
    //endregion Settings

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
    public List<FunctionEntity> loadFunctionsSync(final long deviceId) {
        return mDatabase.functionDao().loadDeviceFunctionsSync(deviceId);
    }
    public List<FunctionEntity> loadAllFunctionsSync() {
        return mDatabase.functionDao().loadAllFunctionsSync();
    }
    public LiveData<FunctionEntity> loadFunction(final long functionId) {
        return mDatabase.functionDao().loadFunction(functionId);
    }
    public FunctionEntity loadFunctionSync(final long functionId) {
        return mDatabase.functionDao().loadFunctionSync(functionId);
    }
    public FunctionEntity loadFunctionSync(final long deviceId, final String functionName) {
        return mDatabase.functionDao().loadFunctionSync(deviceId, functionName);
    }
    public FunctionEntity loadDeviceFunctionSync(final String deviceName, final String functionName) {
        return mDatabase.functionDao().loadDeviceFunctionSync(deviceName, functionName);
    }
    public void insertFunction(FunctionEntity function) {
        long id = mDatabase.functionDao().insert(function);
        function.setId(id);

        SendStateToRemoteClients(new RemoteStateUpdate(function, "insertFunction"));
    }
    public void updateFunction(FunctionEntity function) {
        mDatabase.functionDao().update(function);

        SendStateToRemoteClients(new RemoteStateUpdate(function, "updateFunction"));
    }
    public void updateDeviceFunctionsStates(long deviceId, int callState, int resultState) {

        FunctionEntity functionEntity = new FunctionEntity();
        functionEntity.setDeviceId(deviceId);
        functionEntity.setResultState(resultState);
        functionEntity.setCallState(callState);
        mDatabase.functionDao().updateDeviceFunctionsStates(deviceId,callState,resultState);

        SendStateToRemoteClients(new RemoteStateUpdate(functionEntity, "updateDeviceFunctionsStates"));
    }

    public void updateAllFunctionStates(int callState, int resultState)
    {
        mDatabase.functionDao().updateAllFunctionStates(callState, resultState);

        FunctionEntity function = new FunctionEntity();
        function.setCallState(callState);
        function.setResultState(resultState);
        SendStateToRemoteClients(new RemoteStateUpdate(function, "updateAllFunctionStates"));
    }
    public void updateFunctionAutoEnabled(final long functionId, boolean isChecked) {
        mDatabase.functionDao().updateAutoEnabled(functionId, isChecked);
    }
    public void updateFunctionLog(final long functionId, String log) {
        mDatabase.functionDao().updateLog(functionId, log);
    }
    //endregion Function

    //region FunctionExecution
    public LiveData<List<FunctionExecutionEntity>> loadFunctionExecutions(final long functionId) {
        return mDatabase.functionExecutionDao().loadFunctionExecutions(functionId);
    }

    public LiveData<FunctionExecutionEntity> loadLastFunctionExecution(final long functionId) {
        return mDatabase.functionExecutionDao().loadLastFunctionExecution(functionId);
    }
    public FunctionExecutionEntity loadLastFunctionExecutionSync(final long functionId) {
        return mDatabase.functionExecutionDao().loadLastFunctionExecutionSync(functionId);
    }
    public FunctionExecutionEntity loadLastUnfinishedFunctionExecution(final long functionId) {
        return mDatabase.functionExecutionDao().loadLastUnfinishedFunctionExecutionSync(functionId);
    }

    public void deleteFunctionExecutions(final long functionId) {
        mDatabase.functionExecutionDao().deleteFunctionExecution(functionId);

        FunctionEntity fe = new FunctionEntity();
        fe.setId(functionId);
        SendStateToRemoteClients(new RemoteStateUpdate(fe, "deleteFunctionExecutions"));
    }
    public void deleteAllFunctionExecutions() {
        mDatabase.functionExecutionDao().deleteAllFunctionExecution();

        SendStateToRemoteClients(new RemoteStateUpdate(null, "deleteAllFunctionExecutions"));
    }
    public long insertFunctionExecution(FunctionExecutionEntity execution) {
        long functionId = execution.getFunctionId();
        int callState = execution.getCallState();
        int resultState = execution.getResultState();

        mDatabase.functionDao().updateStates(functionId, callState, resultState);

        long id = mDatabase.functionExecutionDao().insert(execution);
        execution.setId(id);

        SendStateToRemoteClients(new RemoteStateUpdate(execution, "insertFunctionExecution"));

        return  id;
    }
    public void updateFunctionExecution(FunctionExecutionEntity execution) {
        long functionId = execution.getFunctionId();
        int callState = execution.getCallState();
        int resultState = execution.getResultState();

        mDatabase.functionDao().updateStates(functionId, callState, resultState);

        mDatabase.functionExecutionDao().update(execution);

        SendStateToRemoteClients(new RemoteStateUpdate(execution, "updateFunctionExecution"));
    }
    //endregion FunctionExecution

    //region ExecutionLog
    public LiveData<List<ExecutionLogEntity>> loadExecutionLog(final long executionId) {
        return mDatabase.executionLogDao().loadExecutionLogs(executionId);
    }
    public List<ExecutionLogEntity> loadExecutionLogSync(final long executionId) {
        return mDatabase.executionLogDao().loadExecutionLogsSync(executionId);
    }
    public LiveData<List<ExecutionLogEntity>> loadAllExecutionLog() {
        return mDatabase.executionLogDao().loadAllExecutionLogs();
    }
    public long insertExecutionLog(ExecutionLogEntity log) {
        long id = mDatabase.executionLogDao().insert(log);
        log.setId(id);

        SendStateToRemoteClients(new RemoteStateUpdate(log, "insertExecutionLog"));

        return  id;
    }
    public long insertExecutionLogOnLastFunctionExecution(long functionId, String msg) {
        FunctionEntity f = loadFunctionSync(functionId);
        String functionName = f.getName();

        ExecutionLogEntity log = new ExecutionLogEntity();
        log.setFunctionId(functionId);
        log.setFunctionName(functionName);
        log.setLog(msg);
        log.setDate(DateConverter.toDate(System.currentTimeMillis()));

        return insertExecutionLogOnLastFunctionExecution(functionId, functionName, log);
    }
    public long insertExecutionLogOnLastFunctionExecution(long functionId, String functionName, ExecutionLogEntity log) {

        FunctionExecutionEntity functionExecution = loadLastFunctionExecutionSync(functionId);
        if (functionExecution == null) {
            // Get a date in past
            Date date = new GregorianCalendar(2000, Calendar.JANUARY, 1).getTime();
            functionExecution = new FunctionExecutionEntity(0, functionId, functionName,
                    FunctionCallStateEnum.READY.getId(), FunctionResultStateEnum.NA.getId(),
                    date, date);
            long id = insertFunctionExecution(functionExecution);
            functionExecution.setId(id);
        }

        log.setExecutionId(functionExecution.getId());
        long id = insertExecutionLog(log);

        SendStateToRemoteClients(new RemoteStateUpdate(log, "insertExecutionLog"));

        return id;
    }
    public void updateExecutionLog(ExecutionLogEntity log) {
        mDatabase.executionLogDao().update(log);
    }
    public void deleteExecutionLogs(final long functionId) {
        mDatabase.executionLogDao().deleteFunctionExecutionLogs(functionId);

        FunctionEntity fe = new FunctionEntity();
        fe.setId(functionId);
        SendStateToRemoteClients(new RemoteStateUpdate(fe, "deleteExecutionLogs"));
    }
    public void deleteAllExecutionLogs() {
        mDatabase.executionLogDao().deleteAllFunctionExecutionLogs();

        SendStateToRemoteClients(new RemoteStateUpdate(null, "deleteAllExecutionLogs"));
    }
    public void deleteExecutionLogsToDate(Date toDate) {
        mDatabase.executionLogDao().deleteFunctionExecutionLogsToDate(toDate);

        ExecutionLogEntity executionLogEntity = new ExecutionLogEntity();
        executionLogEntity.setDate(toDate);
        SendStateToRemoteClients(new RemoteStateUpdate(executionLogEntity, "deleteExecutionLogsToDate"));
    }
    //endregion ExecutionLog

    //region PinState
    public LiveData<List<PinStateEntity>> loadDeviceCurrentPinsState(final long deviceId) {
        return mDatabase.pinStateDao().loadDeviceCurrentPinsState(deviceId);
    }
    public LiveData<List<PinStateChangeEntity>> loadChangedPinsState() {
        return mDatabase.pinStateDao().loadChangedPinsState();
    }
    public LiveData<List<PinStateChangeEntity>> loadChangedDevicePinsState(String deviceIp) {
        return mDatabase.pinStateDao().loadChangedPinsState(deviceIp);
    }
    public List<PinStateEntity> loadDeviceCurrentPinsStateSync(final long deviceId) {
        return mDatabase.pinStateDao().loadDeviceCurrentPinsStateSync(deviceId);
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
    public PinStateEntity loadDeviceCurrentPinStateSync(final long deviceId, final String pinName) {
        return mDatabase.pinStateDao().loadDeviceCurrentPinStateSync(deviceId, pinName);
    }
    public void updatePinState(final long deviceId, final String pinName, Double pinState) {
        Date now = DateConverter.toDate(System.currentTimeMillis());
        mDatabase.pinStateDao().updatePinState(deviceId, pinName, pinState, now);
    }
    public void insertPinState(PinStateEntity pinState) {
        long id = mDatabase.pinStateDao().insert(pinState);
        pinState.setId(id);

        // To often - there is 1M limit of messages
//        SendStateToRemoteClients(new RemoteStateUpdate(pinState, "insertPinState"));
    }
    public void updatePinState(PinStateEntity pinStateEntity) {
        mDatabase.pinStateDao().update(pinStateEntity);
    }
    public void updatePinStateToDate(long id) {
        mDatabase.pinStateDao().updateToDate(id, DateConverter.toDate(System.currentTimeMillis()));

        // To often - there is 1M limit of messages
//        PinStateEntity pinState = new PinStateEntity();
//        pinState.setId(id);
//        SendStateToRemoteClients(new RemoteStateUpdate(pinState, "updatePinStateToDate"));

    }
    public void updatePinStateLastUpdate(long id) {
        mDatabase.pinStateDao().updateLastUpdate(id, DateConverter.toDate(System.currentTimeMillis()));

        // To often - there is 1M limit of messages
//        PinStateEntity pinState = new PinStateEntity();
//        pinState.setId(id);
//        SendStateToRemoteClients(new RemoteStateUpdate(pinState, "updatePinStateLastUpdate"));

    }
    public void deletePinStatesByFunction(long functionId)
    {
        mDatabase.pinStateDao().deletePinStatesByFunction(functionId);

        FunctionEntity fe = new FunctionEntity();
        fe.setId(functionId);
        SendStateToRemoteClients(new RemoteStateUpdate(fe, "deletePinStatesByFunction"));

    }
    public void deleteAllPinStates()
    {
        mDatabase.pinStateDao().deleteAllPinStates();

        SendStateToRemoteClients(new RemoteStateUpdate(null, "deleteAllPinStates"));
    }

    public void deletePinStatesToDate(Date toDate) {
        mDatabase.pinStateDao().deletePinStatesToDate(toDate);

        PinStateEntity e = new PinStateEntity();
        e.setToDate(toDate);
        SendStateToRemoteClients(new RemoteStateUpdate(e, "deletePinStatesToDate"));
    }
    //endregion PinState

    //region MockPinState

    public List<MockPinStateEntity> loadMockDevicePinsStateSync(final String deviceName) {
        return mDatabase.mockPinStateDao().loadDevicePinsStateSync(deviceName);
    }
    public MockPinStateEntity loadMockDevicePinStateSync(final String deviceName, final int pinNo) {
        return mDatabase.mockPinStateDao().loadDevicePinStateSync(deviceName, pinNo);
    }

    public void insertMockPinState(MockPinStateEntity pinState) {
        mDatabase.mockPinStateDao().insert(pinState);
    }
    public void updateMockPinState(MockPinStateEntity pinStateEntity) {
        mDatabase.mockPinStateDao().update(pinStateEntity);
    }
    public void updateMockPinStateById(long id, double state) {
        mDatabase.mockPinStateDao().updateById(id, state);
    }
    public void deleteMockPinStatesByDevice(String deviceName)
    {
        mDatabase.mockPinStateDao().deletePinStatesByDevice(deviceName);
    }
    public void deleteAllMockPinStates()
    {
        mDatabase.mockPinStateDao().deleteAllPinStates();
    }
    //endregion MockPinState

    //region RemoteQueue
    public LiveData<List<RemoteQueueEntity>> getRemoteQueues() {
        return mDatabase.remoteQueueDao().getRemoteQueues();
    }

    public List<RemoteQueueEntity> getRemoteQueuesSync() {
        return mDatabase.remoteQueueDao().getRemoteQueuesSync();
    }
    public int getNumberOfAllRemoteQueues() {
        return mDatabase.remoteQueueDao().getNumberOfAllRemoteQueues();
    }
    public void getRemoteQueue(String name)
    {
        mDatabase.remoteQueueDao().getRemoteQueueSync(name);
    }
    public void insertRemoteQueue(String name)
    {
        RemoteQueueEntity entity = new RemoteQueueEntity();
        entity.setName(name);
        entity.setDate(DateConverter.toDate(System.currentTimeMillis()));
        mDatabase.remoteQueueDao().insert(entity);
    }
    public void deleteRemoteQueue(String name)
    {
        mDatabase.remoteQueueDao().deleteRemoteQueueSync(name);
    }
    //endregion RemoteQueue

}