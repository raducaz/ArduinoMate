package com.gmail.raducaz.arduinomate.ui;

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
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.DeviceViewItemBinding;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.viewmodel.DeviceViewModel;

public class FragmentDeviceViewItem extends Fragment {

    private static final String KEY_DEVICE_ID = "device_id";

    private DeviceViewItemBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.device_view_item, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DeviceViewModel.Factory factory = new DeviceViewModel.Factory(
                getActivity().getApplication(), getArguments().getLong(KEY_DEVICE_ID));

        final DeviceViewModel model = ViewModelProviders.of(this, factory)
                .get(DeviceViewModel.class);

        mBinding.setDeviceViewModel(model);

        subscribeToModel(model);

        ImageButton button = mBinding.getRoot().findViewById(R.id.execute_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View b) {

//                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
//                TaskFunctionCaller functionCaller = new TaskFunctionCaller(application, model.device.get());
//                new FunctionCaller().execute(functionCaller);
            }
        });

    }

//    private class FunctionCaller extends AsyncTask<TaskFunctionCaller, Void, String>
//    {
//        protected String doInBackground(TaskFunctionCaller...caller)
//        {
//            // Start sending command to Arduino
//            caller[0].execute();
//
//            return "";
//        }
//    }

    private void subscribeToModel(final DeviceViewModel model) {

        // Observe function data
        model.getObservableDevice().observe(this, new Observer<DeviceEntity>() {
            @Override
            public void onChanged(@Nullable DeviceEntity deviceEntity) {
                model.setDevice(deviceEntity);
            }
        });
    }

    /** Creates function fragment for specific function ID */
    public static FragmentDeviceViewItem forDevice(long deviceId) {
        FragmentDeviceViewItem fragment = new FragmentDeviceViewItem();
        Bundle args = new Bundle();
        args.putLong(KEY_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

}
