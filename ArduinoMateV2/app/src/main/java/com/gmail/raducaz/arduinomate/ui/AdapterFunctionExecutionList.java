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
import com.gmail.raducaz.arduinomate.databinding.ExecutionListItemBinding;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.model.FunctionExecution;

import java.util.List;
import java.util.Objects;

public class AdapterFunctionExecutionList extends RecyclerView.Adapter<AdapterFunctionExecutionList.FunctionExecutionViewHolder> {

    List<? extends FunctionExecution> mFunctionExecutionList;

    @Nullable
    private final ClickCallbackFunctionExecution mFunctionExecutionClickCallback;

    public AdapterFunctionExecutionList(@Nullable ClickCallbackFunctionExecution clickCallback) {
        mFunctionExecutionClickCallback = clickCallback;
    }

    public void setFunctionExecutionList(final List<? extends FunctionExecution> functionExecutionList) {
        if (functionExecutionList == null) {
            mFunctionExecutionList = functionExecutionList;
            notifyItemRangeInserted(0, 0);
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mFunctionExecutionList !=null ? mFunctionExecutionList.size() : 0;
                }

                @Override
                public int getNewListSize() {
                    return functionExecutionList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return (mFunctionExecutionList !=null ? mFunctionExecutionList.get(oldItemPosition).getId() : 0) ==
                            functionExecutionList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    FunctionExecution newFunctionExecution = functionExecutionList.get(newItemPosition);
                    FunctionExecution oldFunctionExecution = mFunctionExecutionList != null ? mFunctionExecutionList.get(oldItemPosition):null;
                    return newFunctionExecution.getId() == oldFunctionExecution.getId() &&
                            newFunctionExecution.getEndDate() == oldFunctionExecution.getEndDate() &&
                            newFunctionExecution.getState() == oldFunctionExecution.getState();
                }
            });
            mFunctionExecutionList = functionExecutionList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public FunctionExecutionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ExecutionListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.execution_list_item,
                        parent, false);
        binding.setCallback(mFunctionExecutionClickCallback);
        return new FunctionExecutionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(FunctionExecutionViewHolder holder, int position) {
        holder.binding.setExecution(mFunctionExecutionList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mFunctionExecutionList == null ? 0 : mFunctionExecutionList.size();
    }

    static class FunctionExecutionViewHolder extends RecyclerView.ViewHolder {

        final ExecutionListItemBinding binding;

        public FunctionExecutionViewHolder(ExecutionListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.details_button);
            shareImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Share " + binding.getExecution().getStartDate(),
                            Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }
}
