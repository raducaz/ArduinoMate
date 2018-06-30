package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

import java.util.List;

public class DeviceListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<DeviceEntity>> mObservableDevices;

    public DeviceListViewModel(Application application) {
        super(application);

        mObservableDevices = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableDevices.setValue(null);

        LiveData<List<DeviceEntity>> devices = ((ArduinoMateApp) application).getRepository()
                .getDevices();

        // observe the changes of the devices from the database and forward them
        mObservableDevices.addSource(devices, mObservableDevices::setValue);
    }

    /**
     * Expose the LiveData Devices query so the UI can observe it.
     */
    public LiveData<List<DeviceEntity>> getDevices() {
        return mObservableDevices;
    }
}
