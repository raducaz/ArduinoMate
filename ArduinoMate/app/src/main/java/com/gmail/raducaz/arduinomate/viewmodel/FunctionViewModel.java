package com.gmail.raducaz.arduinomate.viewmodel;


import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.model.Function;

import java.util.List;

public class FunctionViewModel extends AndroidViewModel {

    private DataRepository dataRepository;
    private final LiveData<FunctionEntity> mObservableFunction;

    public ObservableField<FunctionEntity> function = new ObservableField<>();

    private final int mFunctionId;

//    private final LiveData<List<FunctionEntity>> mObservableFunctions;

    public FunctionViewModel(@NonNull Application application, DataRepository repository,
                            final int functionId) {
        super(application);
        mFunctionId = functionId;
        dataRepository = repository;
//        mObservableFunctions = repository.loadFunctions(mDeviceId);

        mObservableFunction = repository.loadFunction(mFunctionId);
    }

//    /**
//     * Expose the LiveData Functions query so the UI can observe it.
//     */
//    public LiveData<List<FunctionEntity>> getFunctions() {
//        return mObservableFunctions;
//    }

    public LiveData<FunctionEntity> getObservableFunction() {
        return mObservableFunction;
    }

    public void setFunction(FunctionEntity function) {
        this.function.set(function);
    }

    public void updateFunction(FunctionEntity function)
    {
        dataRepository.updateFunction(function);
    }
    /**
     * A creator is used to inject the function ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the device ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mFunctionId;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, int functionId) {
            mApplication = application;
            mFunctionId = functionId;
            mRepository = ((ArduinoMateApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new FunctionViewModel(mApplication, mRepository, mFunctionId);
        }
    }
}