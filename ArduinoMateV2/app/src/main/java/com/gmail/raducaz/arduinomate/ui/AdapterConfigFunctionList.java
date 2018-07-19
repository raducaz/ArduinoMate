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
import com.gmail.raducaz.arduinomate.databinding.ConfigFunctionListItemBinding;
import com.gmail.raducaz.arduinomate.model.Function;

import java.util.List;
import java.util.Objects;

public class AdapterConfigFunctionList extends RecyclerView.Adapter<AdapterConfigFunctionList.FunctionViewHolder> {

    List<? extends Function> mFunctionList;

    @Nullable
    private final ClickCallbackFunction mFunctionClickCallback;

    public AdapterConfigFunctionList(@Nullable ClickCallbackFunction clickCallback) {
        mFunctionClickCallback = clickCallback;
    }

    public void setFunctionList(final List<? extends Function> functionList) {
        if (mFunctionList == null) {
            mFunctionList = functionList;
            notifyItemRangeInserted(0, functionList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mFunctionList.size();
                }

                @Override
                public int getNewListSize() {
                    return functionList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mFunctionList.get(oldItemPosition).getId() ==
                            functionList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Function newFunction = functionList.get(newItemPosition);
                    Function oldFunction = mFunctionList.get(oldItemPosition);
                    return newFunction.getId() == oldFunction.getId()
                            && Objects.equals(newFunction.getDescription(), oldFunction.getDescription())
                            && Objects.equals(newFunction.getName(), oldFunction.getName());
                }
            });
            mFunctionList = functionList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public FunctionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConfigFunctionListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.config_function_list_item,
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

        final ConfigFunctionListItemBinding binding;

        public FunctionViewHolder(ConfigFunctionListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

//            ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.delete_button);
//            shareImageButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Snackbar.make(v, "Delete " + binding.getFunction().getName(),
//                            Snackbar.LENGTH_LONG).show();
//                }
//            });
        }
    }
}
