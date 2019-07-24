package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;

public class DeviceViewModel extends AndroidViewModel {

    private DataRepository dataRepository;
    private final LiveData<DeviceEntity> mObservableDevice;

    public ObservableField<DeviceEntity> device = new ObservableField<>();

    private final long mDeviceId;

//    private final LiveData<List<FunctionEntity>> mObservableFunctions;

    public DeviceViewModel(@NonNull Application application, DataRepository repository,
                           final long deviceId) {
        super(application);
        this.dataRepository = repository;

        mDeviceId = deviceId;

//        mObservableFunctions = repository.loadFunctions(mDeviceId);
        mObservableDevice = repository.loadDevice(mDeviceId);
    }

    /**
     * Expose the LiveData Functions query so the UI can observe it.
     */
//    public LiveData<List<FunctionEntity>> getFunctions() {
//        return mObservableFunctions;
//    }

    public LiveData<DeviceEntity> getObservableDevice() {
        return mObservableDevice;
    }

    public void setDevice(DeviceEntity device) {
        this.device.set(device);
    }

    public void testInsertDevice()
    {
        DeviceEntity p = new DeviceEntity();
        p.setId(2);

        p.setName("Test inser");
        p.setDescription("Test description");
        dataRepository.insertDevice(p);
    }

    public void testUpdateDevice()
    {
        DeviceEntity p = mObservableDevice.getValue();
        p.setDescription("Test update desc");
        dataRepository.updateDevice(p);
    }

    /**
     * A creator is used to inject the device ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the device ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final long mDeviceId;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, long deviceId) {
            mApplication = application;
            mDeviceId = deviceId;
            mRepository = ((ArduinoMateApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new DeviceViewModel(mApplication, mRepository, mDeviceId);
        }
    }
}