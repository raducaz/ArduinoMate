package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;

import java.util.List;

public class FunctionExecutionListViewModel extends AndroidViewModel {

    public FunctionExecutionListViewModel(Application application) {
        super(application);

    }

    /**
     * Expose the LiveData Devices query so the UI can observe it.
     */
    public LiveData<List<FunctionExecutionEntity>> getFunctionExecutions(long functionId) {
        // MediatorLiveData can observe other LiveData objects and react on their emissions.
        MediatorLiveData<List<FunctionExecutionEntity>> mObservableFunctionExecutions;

        mObservableFunctionExecutions = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableFunctionExecutions.setValue(null);

        LiveData<List<FunctionExecutionEntity>> functionExecutions =
                ((ArduinoMateApp) getApplication()).getRepository().loadFunctionExecutions(functionId);

        // observe the changes of the function from the database and forward them
        mObservableFunctionExecutions.addSource(functionExecutions, mObservableFunctionExecutions::setValue);

        return mObservableFunctionExecutions;
    }
}
