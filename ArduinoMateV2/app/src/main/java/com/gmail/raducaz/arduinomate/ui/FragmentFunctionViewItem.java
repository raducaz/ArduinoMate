package com.gmail.raducaz.arduinomate.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.databinding.FunctionViewItemBinding;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.service.TaskFunctionReset;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionViewModel;

public class FragmentFunctionViewItem extends Fragment {

    private static final String KEY_FUNCTION_ID = "function_id";

    private FunctionViewItemBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.function_view_item, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FunctionViewModel.Factory factory = new FunctionViewModel.Factory(
                getActivity().getApplication(), getArguments().getLong(KEY_FUNCTION_ID));

        final FunctionViewModel model = ViewModelProviders.of(this, factory)
                .get(FunctionViewModel.class);

        mBinding.setFunctionViewModel(model);

        subscribeToModel(model);

        Button buttonExecute = mBinding.getRoot().findViewById(R.id.execute_button);
        buttonExecute.setOnClickListener(new OnClickListener() {
            public void onClick(View b) {

                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
                TaskFunctionCaller functionCaller = new TaskFunctionCaller(application.getRepository(), model.function.get());
                new TaskExecutor().execute(functionCaller);
            }
        });

        Button buttonReset = mBinding.getRoot().findViewById(R.id.reset_button);
        buttonReset.setOnClickListener(new OnClickListener() {
            public void onClick(View b) {

                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
                TaskFunctionReset functionReset = new TaskFunctionReset(application, model.function.get());
                new TaskExecutor().execute(functionReset);
            }
        });

        CheckBox autoCheckBox = mBinding.getRoot().findViewById(R.id.auto_checkbox);
        autoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
                FunctionEntity functionEntity = mBinding.getFunctionViewModel().getObservableFunction().getValue();
                functionEntity.setIsAutoEnabled(isChecked);
                application.getDbExecutor().execute(new DbUpdater(functionEntity));

//                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
//                DataRepository repository = application.getRepository();
//                repository.updateFunctionAutoEnabled(model.functionId, isChecked);


            }
        });

    }

    private class DbUpdater implements Runnable {
        FunctionEntity functionEntity;
        DbUpdater(FunctionEntity functionEntity)
        {
            this.functionEntity = functionEntity;
        }

        @Override
        public void run() {
            ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
            DataRepository repo = application.getRepository();
            repo.updateFunction(functionEntity);
        }
    }

    private void subscribeToModel(final FunctionViewModel model) {

        // Observe function data
        model.getObservableFunction().observe(this, new Observer<FunctionEntity>() {
            @Override
            public void onChanged(@Nullable FunctionEntity functionEntity) {
                model.setFunction(functionEntity);
            }
        });
        // Observe function execution data
        model.getObservableFunctionExecution().observe(this, new Observer<FunctionExecutionEntity>() {
            @Override
            public void onChanged(@Nullable FunctionExecutionEntity functionExecutionEntity) {
                model.setFunctionExecution(functionExecutionEntity);
            }
        });
    }

    /** Creates function fragment for specific function ID */
    public static FragmentFunctionViewItem forFunction(long functionId) {
        FragmentFunctionViewItem fragment = new FragmentFunctionViewItem();
        Bundle args = new Bundle();
        args.putLong(KEY_FUNCTION_ID, functionId);
        fragment.setArguments(args);
        return fragment;
    }

}
