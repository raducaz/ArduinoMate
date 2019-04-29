package com.gmail.raducaz.arduinomate.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.DeviceEditItemBinding;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.viewmodel.DeviceViewModel;

public class FragmentDeviceEditItem extends Fragment {

    private static final String KEY_DEVICE_ID = "device_id";

    private DeviceEditItemBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.device_edit_item, container, false);

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

        Button button = mBinding.getRoot().findViewById(R.id.save_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View b) {

                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
                DeviceEntity deviceEntity = mBinding.getDeviceViewModel().getObservableDevice().getValue();
                application.getDbExecutor().execute(new DbUpdater(deviceEntity));

                NavUtils.navigateUpFromSameTask(getActivity());
            }
        });

    }

    private class DbUpdater implements Runnable {
        DeviceEntity deviceEntity;
        DbUpdater(DeviceEntity deviceEntity)
        {
            this.deviceEntity = deviceEntity;
        }

        @Override
        public void run() {
            ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
            DataRepository repo = application.getRepository();
            repo.updateDevice(deviceEntity);
        }
    }

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
    public static FragmentDeviceEditItem forDevice(long deviceId) {
        FragmentDeviceEditItem fragment = new FragmentDeviceEditItem();
        Bundle args = new Bundle();
        args.putLong(KEY_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }

}
