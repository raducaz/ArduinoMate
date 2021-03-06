package com.gmail.raducaz.arduinomate.ui;


import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.databinding.DeviceListItemBinding;
import com.gmail.raducaz.arduinomate.model.Device;
import com.gmail.raducaz.arduinomate.R;

import java.util.List;
import java.util.Objects;

public class AdapterDeviceList extends RecyclerView.Adapter<AdapterDeviceList.DeviceViewHolder> {

    List<? extends Device> mDeviceList;

    @Nullable
    private final ClickCallbackDevice mDeviceClickCallback;

    public AdapterDeviceList(@Nullable ClickCallbackDevice clickCallback) {
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
        DeviceListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.device_list_item,
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

        final DeviceListItemBinding binding;

        public DeviceViewHolder(DeviceListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

//            ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.share_button);
//            shareImageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Snackbar.make(v, "Share " + binding.getDevice().getName(),
//                            Snackbar.LENGTH_LONG).show();
//                }
//            });
        }
    }
}
