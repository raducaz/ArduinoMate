package com.gmail.raducaz.arduinomate.ui;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.databinding.FunctionViewItemBinding;
import com.gmail.raducaz.arduinomate.service.ArduinoFunctionCaller;
import com.gmail.raducaz.arduinomate.service.FunctionChannelClientInboundHandler;
import com.gmail.raducaz.arduinomate.service.TcpClientService;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionViewModel;

import java.io.IOException;
import java.util.concurrent.Executor;

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

        ImageButton button = (ImageButton) mBinding.getRoot().findViewById(R.id.execute_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View b) {

                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
                ArduinoFunctionCaller functionCaller = new ArduinoFunctionCaller(application, model.function.get());
                new FunctionCaller().execute(functionCaller);
            }
        });

    }

    private class FunctionCaller extends AsyncTask<ArduinoFunctionCaller, Void, String>
    {
        protected String doInBackground(ArduinoFunctionCaller...caller)
        {
            // Start sending command to Arduino
            caller[0].execute();

            return "";
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
