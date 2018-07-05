package com.gmail.raducaz.arduinomate.ui;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.RecyclerViewBinding;
import com.gmail.raducaz.arduinomate.db.entity.DeviceEntity;
import com.gmail.raducaz.arduinomate.model.Device;
import com.gmail.raducaz.arduinomate.viewmodel.DeviceListViewModel;

import java.util.List;

public class FragmentDeviceList extends Fragment {

    public static final String TAG = "DeviceListViewModel";

    private AdapterDeviceList mDeviceAdapter;

    private RecyclerViewBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_view, container, false);

        mDeviceAdapter = new AdapterDeviceList(mDeviceClickCallback);
        mBinding.myRecyclerView.setAdapter(mDeviceAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final DeviceListViewModel viewModel =
                ViewModelProviders.of(this).get(DeviceListViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(DeviceListViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getDevices().observe(this, new Observer<List<DeviceEntity>>() {
            @Override
            public void onChanged(@Nullable List<DeviceEntity> myDevices) {
                if (myDevices != null) {
//                    mBinding.setIsLoading(false);
                    mDeviceAdapter.setDeviceList(myDevices);
                } else {
//                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    private final ClickCallbackDevice mDeviceClickCallback = new ClickCallbackDevice() {
        @Override
        public void onClick(Device device) {

//            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
//                ((ActivityMain) getActivity()).show(device);
//            }
        }
    };
}
