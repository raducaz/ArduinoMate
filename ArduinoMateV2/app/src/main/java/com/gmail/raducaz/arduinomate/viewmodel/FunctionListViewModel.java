package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;

import java.util.List;

public class FunctionListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<FunctionEntity>> mObservableFunctions;

    public FunctionListViewModel(Application application) {
        super(application);

        mObservableFunctions = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableFunctions.setValue(null);

        LiveData<List<FunctionEntity>> functions = ((ArduinoMateApp) application).getRepository()
                .getFunctions();

        // observe the changes of the function from the database and forward them
        mObservableFunctions.addSource(functions, mObservableFunctions::setValue);
    }

    /**
     * Expose the LiveData Devices query so the UI can observe it.
     */
    public LiveData<List<FunctionEntity>> getFunctions() {
        return mObservableFunctions;
    }
}
