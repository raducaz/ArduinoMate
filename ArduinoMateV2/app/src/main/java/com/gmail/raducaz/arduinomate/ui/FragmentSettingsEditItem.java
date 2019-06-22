package com.gmail.raducaz.arduinomate.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.DeviceEditItemBinding;
import com.gmail.raducaz.arduinomate.databinding.SettingsEditItemBinding;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.db.entity.SettingsEntity;
import com.gmail.raducaz.arduinomate.model.Settings;
import com.gmail.raducaz.arduinomate.viewmodel.DeviceViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.SettingsViewModel;

public class FragmentSettingsEditItem extends Fragment {

    private SettingsEditItemBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.settings_edit_item, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettingsViewModel.Factory factory = new SettingsViewModel.Factory(
                getActivity().getApplication());

        final SettingsViewModel model = ViewModelProviders.of(this, factory)
                .get(SettingsViewModel.class);

        mBinding.setSettingsViewModel(model);

        subscribeToModel(model);

        Button button = mBinding.getRoot().findViewById(R.id.save_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View b) {

                ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
                SettingsEntity settingsEntity = mBinding.getSettingsViewModel().getObservableSettings().getValue();

                CheckBox isControllerChk = mBinding.getRoot().findViewById(R.id.controller_checkbox);
                settingsEntity.setIsController(isControllerChk.isChecked());

                CheckBox permitRemoteChk = mBinding.getRoot().findViewById(R.id.remote_checkbox);
                settingsEntity.setPermitRemoteControl(permitRemoteChk.isChecked());

                application.getDbExecutor().execute(new DbUpdater(settingsEntity));

                Snackbar.make(b, "Settings saved.", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private class DbUpdater implements Runnable {
        SettingsEntity settingsEntity;
        DbUpdater(SettingsEntity settingsEntity)
        {
            this.settingsEntity = settingsEntity;
        }

        @Override
        public void run() {
            ArduinoMateApp application = (ArduinoMateApp) getActivity().getApplication();
            DataRepository repo = application.getRepository();
            repo.updateSettings(settingsEntity);
        }
    }

    private void subscribeToModel(final SettingsViewModel model) {

        // Observe function data
        model.getObservableSettings().observe(this, new Observer<SettingsEntity>() {
            @Override
            public void onChanged(@Nullable SettingsEntity settingsEntity) {
                model.setSettings(settingsEntity);
            }
        });
    }

    /** Creates function fragment for specific function ID */
    public static FragmentSettingsEditItem getInstance() {
        FragmentSettingsEditItem fragment = new FragmentSettingsEditItem();
        return fragment;
    }

}
