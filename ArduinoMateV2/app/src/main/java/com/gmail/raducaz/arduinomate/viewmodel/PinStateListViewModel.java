package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;

import java.util.List;

public class PinStateListViewModel extends AndroidViewModel {

    public PinStateListViewModel(Application application) {
        super(application);
    }

    /**
     * Expose the LiveData Devices query so the UI can observe it.
     */
    public LiveData<List<PinStateEntity>> getDeviceCurrentPinStates(long deviceId) {
        // MediatorLiveData can observe other LiveData objects and react on their emissions.
        MediatorLiveData<List<PinStateEntity>> mObservableCurrentPinStates;

        mObservableCurrentPinStates = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableCurrentPinStates.setValue(null);

        LiveData<List<PinStateEntity>> pinStates =
                ((ArduinoMateApp) getApplication()).getRepository().loadDeviceCurrentPinsState(deviceId);

        // observe the changes of the function from the database and forward them
        mObservableCurrentPinStates.addSource(pinStates, mObservableCurrentPinStates::setValue);

        return mObservableCurrentPinStates;
    }
}
