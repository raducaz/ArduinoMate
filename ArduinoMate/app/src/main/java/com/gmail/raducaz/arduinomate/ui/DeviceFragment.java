package com.gmail.raducaz.arduinomate.ui;

import com.gmail.raducaz.arduinomate.R;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.DataRepository;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.DeviceFragmentBinding;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.service.FunctionChannelClientInboundHandler;
import com.gmail.raducaz.arduinomate.service.TcpClientService;
import com.gmail.raducaz.arduinomate.viewmodel.DeviceViewModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

public class DeviceFragment extends Fragment {

    private static final String KEY_DEVICE_ID = "device_id";

    private DeviceFragmentBinding mBinding;

    private FunctionAdapter mFunctionAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

//        View v = inflater.inflate(R.layout.device_fragment, null);
//
//        // Adding Toolbar to Main screen
//        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
//        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
//
//        // Setting ViewPager for each Tabs
////        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
////        ((AppCompatActivity)getActivity()).setupViewPager(viewPager);
//
//        // Set Tabs inside Toolbar
//        TabLayout tabs = (TabLayout) v.findViewById(R.id.tabs);
//        tabs.addTab(tabs.newTab().setText("Tab 1"));
//        tabs.addTab(tabs.newTab().setText("Tab 2"));
//        tabs.addTab(tabs.newTab().setText("Tab 3"));

        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.device_fragment, container, false);

        // Create and set the adapter for the RecyclerView.
        mFunctionAdapter = new FunctionAdapter(mFunctionClickCallback);
        mBinding.functionList.setAdapter(mFunctionAdapter);

        return mBinding.getRoot();
    }

    private final FunctionClickCallback mFunctionClickCallback = new FunctionClickCallback() {
        @Override
        public void onClick(Function function) {

            if(function.getText().equals("ProgressFct")) {

                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    ((MainActivity) getActivity()).show(function);
                }
            }
            else {
                // Start sending command to Arduino
                DataRepository repository = ((ArduinoMateApp) getActivity().getApplication()).getRepository();
                Executor executor = ((ArduinoMateApp) getActivity().getApplication()).getNetworkExecutor();

                try {
                    executor.execute(new TcpClientService("", "",
                            new FunctionChannelClientInboundHandler((FunctionEntity) function, repository)
                    ));
                } catch (IOException exc) {
                    //TODO: do something here
                }
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        DeviceViewModel.Factory factory = new DeviceViewModel.Factory(
                getActivity().getApplication(), getArguments().getInt(KEY_DEVICE_ID));

        final DeviceViewModel model = ViewModelProviders.of(this, factory)
                .get(DeviceViewModel.class);

        mBinding.setDeviceViewModel(model);

        // Test Live Data
        Button button = (Button) mBinding.getRoot().findViewById(R.id.btn_test);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View b)
            {
                ((ArduinoMateApp)getActivity().getApplication()).getDbExecutor().execute(() -> {
                    model.updateDevice();
                });
            }
        });
        // Test Live Data

        subscribeToModel(model);
    }

    private void subscribeToModel(final DeviceViewModel model) {

        // Observe device data
        model.getObservableDevice().observe(this, new Observer<DeviceEntity>() {
            @Override
            public void onChanged(@Nullable DeviceEntity deviceEntity) {
                model.setDevice(deviceEntity);
            }
        });

        // Observe functions
        model.getFunctions().observe(this, new Observer<List<FunctionEntity>>() {
            @Override
            public void onChanged(@Nullable List<FunctionEntity> functionEntities) {
                if (functionEntities != null) {
                    mBinding.setIsLoading(false);
                    mFunctionAdapter.setFunctionList(functionEntities);
                } else {
                    mBinding.setIsLoading(true);
                }
            }
        });
    }

    /** Creates device fragment for specific device ID */
    public static DeviceFragment forDevice(int deviceId) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_DEVICE_ID, deviceId);
        fragment.setArguments(args);
        return fragment;
    }
}
