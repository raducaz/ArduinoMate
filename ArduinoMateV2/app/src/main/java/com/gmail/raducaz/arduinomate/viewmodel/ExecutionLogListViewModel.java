package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;

import java.util.List;

public class ExecutionLogListViewModel extends AndroidViewModel {

    public ExecutionLogListViewModel(Application application) {
        super(application);
    }

    /**
     * Expose the LiveData Devices query so the UI can observe it.
     */
    public LiveData<List<ExecutionLogEntity>> getExecutionLogs(long executionId) {
        // MediatorLiveData can observe other LiveData objects and react on their emissions.
        MediatorLiveData<List<ExecutionLogEntity>> mObservableExecutionLogs;

        mObservableExecutionLogs = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableExecutionLogs.setValue(null);

        LiveData<List<ExecutionLogEntity>> executionLogs =
                ((ArduinoMateApp) getApplication()).getRepository().loadExecutionLog(executionId);

        // observe the changes of the function from the database and forward them
        mObservableExecutionLogs.addSource(executionLogs, mObservableExecutionLogs::setValue);

        return mObservableExecutionLogs;
    }

    public LiveData<List<ExecutionLogEntity>> getAllExecutionLogs() {
        // MediatorLiveData can observe other LiveData objects and react on their emissions.
        MediatorLiveData<List<ExecutionLogEntity>> mObservableExecutionLogs;

        mObservableExecutionLogs = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableExecutionLogs.setValue(null);

        LiveData<List<ExecutionLogEntity>> executionLogs =
                ((ArduinoMateApp) getApplication()).getRepository().loadAllExecutionLog();

        // observe the changes of the function from the database and forward them
        mObservableExecutionLogs.addSource(executionLogs, mObservableExecutionLogs::setValue);

        return mObservableExecutionLogs;
    }
}
