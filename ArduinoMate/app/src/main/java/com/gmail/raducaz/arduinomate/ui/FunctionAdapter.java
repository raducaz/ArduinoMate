package com.gmail.raducaz.arduinomate.ui;


import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.databinding.FunctionItemBinding;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.R;

import java.util.List;
import java.util.Objects;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionViewHolder> {

    private List<? extends Function> mFunctionList;

    @Nullable
    private final FunctionClickCallback mFunctionClickCallback;

    public FunctionAdapter(@Nullable FunctionClickCallback functionClickCallback) {
        mFunctionClickCallback = functionClickCallback;
    }

    public void setFunctionList(final List<? extends Function> functions) {
        if (mFunctionList == null) {
            mFunctionList = functions;
            notifyItemRangeInserted(0, functions.size());
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mFunctionList.size();
                }

                @Override
                public int getNewListSize() {
                    return functions.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    Function old = mFunctionList.get(oldItemPosition);
                    Function function = functions.get(newItemPosition);
                    return old.getId() == function.getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Function old = mFunctionList.get(oldItemPosition);
                    Function function = functions.get(newItemPosition);
                    return old.getId() == function.getId()
                            && old.getPostedAt() == function.getPostedAt()
                            && old.getDeviceId() == function.getDeviceId()
                            && Objects.equals(old.getText(), function.getText());
                }
            });
            mFunctionList = functions;
            diffResult.dispatchUpdatesTo(this);
        }
    }

    @Override
    public FunctionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FunctionItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.function_item,
                        parent, false);
        binding.setCallback(mFunctionClickCallback);
        return new FunctionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(FunctionViewHolder holder, int position) {
        holder.binding.setFunction(mFunctionList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mFunctionList == null ? 0 : mFunctionList.size();
    }

    static class FunctionViewHolder extends RecyclerView.ViewHolder {

        final FunctionItemBinding binding;

        FunctionViewHolder(FunctionItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
