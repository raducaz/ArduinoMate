package com.gmail.raducaz.arduinomate.ui;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.FunctionListItemBinding;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionSync;

import java.util.List;
import java.util.Objects;

public class AdapterFunctionList extends RecyclerView.Adapter<AdapterFunctionList.FunctionViewHolder> {

    List<? extends Function> mFunctionList;
    ArduinoMateApp application;

    @Nullable
    private final ClickCallbackFunction mFunctionClickCallback;

    public AdapterFunctionList(@Nullable ClickCallbackFunction clickCallback, ArduinoMateApp application) {
        mFunctionClickCallback = clickCallback;
        this.application = application;
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
                            && Objects.equals(newFunction.getCallState(), oldFunction.getCallState())
                            && Objects.equals(newFunction.getResultState(), oldFunction.getResultState())
                            && Objects.equals(newFunction.getName(), oldFunction.getName());
                }
            });
            mFunctionList = functionList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public FunctionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FunctionListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.function_list_item,
                        parent, false);
        binding.setCallback(mFunctionClickCallback);
        return new FunctionViewHolder(binding, application);
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

        final FunctionListItemBinding binding;
        final ArduinoMateApp application;

        public FunctionViewHolder(FunctionListItemBinding binding, ArduinoMateApp application) {
            super(binding.getRoot());
            this.binding = binding;
            this.application = application;

            ImageButton executeButton = itemView.findViewById(R.id.execute_button);
            executeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Snackbar.make(v, "Share " + binding.getFunction().getName(),
//                            Snackbar.LENGTH_LONG).show();

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AppTheme));
                    builder.setMessage(R.string.confirm_fct_execute)
                            .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    TaskFunctionCaller functionCaller = new TaskFunctionCaller(
                                            application.getRepository(),
                                            binding.getFunction().getId());
                                    application.getNetworkExecutor().execute(functionCaller);
                                }
                            })
                            .setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                    // Create the AlertDialog object and return it
                    AlertDialog alert = builder.create();
                    alert.show();

                }
            });
        }
    }
}
