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
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;

public class SettingsViewModel extends AndroidViewModel {

    private DataRepository dataRepository;
    private final LiveData<SettingsEntity> mObservableSettings;

    public ObservableField<SettingsEntity> settings = new ObservableField<>();

    public SettingsViewModel(@NonNull Application application, DataRepository repository) {
        super(application);
        this.dataRepository = repository;

        mObservableSettings = repository.getSettings();
    }

    /**
     * Expose the LiveData Functions query so the UI can observe it.
     */
//    public LiveData<List<FunctionEntity>> getFunctions() {
//        return mObservableFunctions;
//    }

    public LiveData<SettingsEntity> getObservableSettings() {
        return mObservableSettings;
    }

    public void setSettings(SettingsEntity settings) {
        this.settings.set(settings);
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

        private final DataRepository mRepository;

        public Factory(@NonNull Application application) {
            mApplication = application;
            mRepository = ((ArduinoMateApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new SettingsViewModel(mApplication, mRepository);
        }
    }
}