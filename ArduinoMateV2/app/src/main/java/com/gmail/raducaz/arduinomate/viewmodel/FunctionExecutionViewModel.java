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
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;

public class FunctionExecutionViewModel extends AndroidViewModel {

    private DataRepository dataRepository;
    private final LiveData<FunctionExecutionEntity> mObservableFunctionExecution;

    public ObservableField<FunctionExecutionEntity> functionExecution = new ObservableField<>();

    private final long mFunctionId;

//    private final LiveData<List<FunctionEntity>> mObservableFunctions;

    public FunctionExecutionViewModel(@NonNull Application application, DataRepository repository,
                                      final long functionId) {
        super(application);
        mFunctionId = functionId;
        dataRepository = repository;

        mObservableFunctionExecution = repository.loadLastFunctionExecution(mFunctionId);
    }

    public LiveData<FunctionExecutionEntity> getObservableFunctionExecution() {
        return mObservableFunctionExecution;
    }

    public void setFunctionExecution(FunctionExecutionEntity functionExecution) {
        this.functionExecution.set(functionExecution);
    }

    public void updateFunctionExecution(FunctionExecutionEntity functionExecution)
    {
        dataRepository.updateFunctionExecution(functionExecution);
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

        private final long mFunctionId;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, long functionId) {
            mApplication = application;
            mFunctionId = functionId;
            mRepository = ((ArduinoMateApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new FunctionExecutionViewModel(mApplication, mRepository, mFunctionId);
        }
    }
}