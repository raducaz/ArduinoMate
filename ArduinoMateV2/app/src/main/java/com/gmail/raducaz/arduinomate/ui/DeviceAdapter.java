package com.gmail.raducaz.arduinomate.ui;


import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.gmail.raducaz.arduinomate.databinding.DeviceItemBinding;
import com.gmail.raducaz.arduinomate.model.Device;
import com.gmail.raducaz.arduinomate.R;

import java.util.List;
import java.util.Objects;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    List<? extends Device> mDeviceList;

    @Nullable
    private final DeviceClickCallback mDeviceClickCallback;

    public DeviceAdapter(@Nullable DeviceClickCallback clickCallback) {
        mDeviceClickCallback = clickCallback;
    }

    public void setDeviceList(final List<? extends Device> deviceList) {
        if (mDeviceList == null) {
            mDeviceList = deviceList;
            notifyItemRangeInserted(0, deviceList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mDeviceList.size();
                }

                @Override
                public int getNewListSize() {
                    return deviceList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mDeviceList.get(oldItemPosition).getId() ==
                            deviceList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Device newDevice = deviceList.get(newItemPosition);
                    Device oldDevice = mDeviceList.get(oldItemPosition);
                    return newDevice.getId() == oldDevice.getId()
                            && Objects.equals(newDevice.getDescription(), oldDevice.getDescription())
                            && Objects.equals(newDevice.getName(), oldDevice.getName());
                }
            });
            mDeviceList = deviceList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DeviceItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.device_item,
                        parent, false);
        binding.setCallback(mDeviceClickCallback);
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        holder.binding.setDevice(mDeviceList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mDeviceList == null ? 0 : mDeviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        final DeviceItemBinding binding;

        public DeviceViewHolder(DeviceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.share_button);
            shareImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Share " + binding.getDevice().getName(),
                            Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }
}
