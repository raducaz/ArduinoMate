package com.gmail.raducaz.arduinomate.ui;


import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.PinStateListItemBinding;
import com.gmail.raducaz.arduinomate.model.PinState;

import java.util.List;

public class AdapterPinStateList extends RecyclerView.Adapter<AdapterPinStateList.PinStateViewHolder> {

    List<? extends PinState> mPinStateList;

    @Nullable
    private final ClickCallbackPinState mPinStateClickCallback;

    public AdapterPinStateList(@Nullable ClickCallbackPinState clickCallback) {
        mPinStateClickCallback = clickCallback;
    }

    public void setPinStateList(final List<? extends PinState> pinStateList) {
        if (pinStateList == null) {
            mPinStateList = pinStateList;
            notifyItemRangeInserted(0, 0);
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mPinStateList !=null ? mPinStateList.size() : 0;
                }

                @Override
                public int getNewListSize() {
                    return pinStateList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return (mPinStateList !=null ? mPinStateList.get(oldItemPosition).getId() : 0) ==
                            pinStateList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    PinState newPinState = pinStateList.get(newItemPosition);
                    PinState oldPinState = mPinStateList != null ? mPinStateList.get(oldItemPosition):null;
                    return newPinState.getId() == oldPinState.getId() &&
                            newPinState.getState() == oldPinState.getState() &&
                            newPinState.getFromDate() == oldPinState.getFromDate() &&
                            newPinState.getSecondsFromLastUpdate() == oldPinState.getSecondsFromLastUpdate();
                }
            });
            mPinStateList = pinStateList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public PinStateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PinStateListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.pin_state_list_item,
                        parent, false);
        binding.setCallback(mPinStateClickCallback);
        return new PinStateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(PinStateViewHolder holder, int position) {
        holder.binding.setPinState(mPinStateList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mPinStateList == null ? 0 : mPinStateList.size();
    }

    static class PinStateViewHolder extends RecyclerView.ViewHolder {

        final PinStateListItemBinding binding;

        public PinStateViewHolder(PinStateListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }
    }
}
