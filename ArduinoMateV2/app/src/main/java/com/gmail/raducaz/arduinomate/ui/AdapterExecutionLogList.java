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

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.ExecutionLogListItemBinding;
import com.gmail.raducaz.arduinomate.model.ExecutionLog;

import java.util.List;

public class AdapterExecutionLogList extends RecyclerView.Adapter<AdapterExecutionLogList.ExecutionLogViewHolder> {

    List<? extends ExecutionLog> mExecutionLogList;

    @Nullable
    private final ClickCallbackExecutionLog mExecutionLogClickCallback;

    public AdapterExecutionLogList(@Nullable ClickCallbackExecutionLog clickCallback) {
        mExecutionLogClickCallback = clickCallback;
    }

    public void setExecutionLogList(final List<? extends ExecutionLog> executionLogList) {
        if (executionLogList == null) {
            mExecutionLogList = executionLogList;
            notifyItemRangeInserted(0, 0);
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mExecutionLogList !=null ? mExecutionLogList.size() : 0;
                }

                @Override
                public int getNewListSize() {
                    return executionLogList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return (mExecutionLogList !=null ? mExecutionLogList.get(oldItemPosition).getId() : 0) ==
                            executionLogList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    ExecutionLog newExecutionLog = executionLogList.get(newItemPosition);
                    ExecutionLog oldExecutionLog = mExecutionLogList != null ? mExecutionLogList.get(oldItemPosition):null;
                    return newExecutionLog.getId() == oldExecutionLog.getId() &&
                            newExecutionLog.getLog() == oldExecutionLog.getLog();
                }
            });
            mExecutionLogList = executionLogList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public ExecutionLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ExecutionLogListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.execution_log_list_item,
                        parent, false);
        binding.setCallback(mExecutionLogClickCallback);
        return new ExecutionLogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ExecutionLogViewHolder holder, int position) {
        holder.binding.setExecutionLog(mExecutionLogList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mExecutionLogList == null ? 0 : mExecutionLogList.size();
    }

    static class ExecutionLogViewHolder extends RecyclerView.ViewHolder {

        final ExecutionLogListItemBinding binding;

        public ExecutionLogViewHolder(ExecutionLogListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }
    }
}
