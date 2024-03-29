package com.gmail.raducaz.arduinomate.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.FunctionItemBinding;
import com.gmail.raducaz.arduinomate.model.Function;


public class FunctionItem extends Fragment {

    private FunctionItemBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.function_item, container, false);
        mBinding.setCallback(mFunctionClickCallback);
        return mBinding.getRoot();
    }

    private final FunctionClickCallback mFunctionClickCallback = new FunctionClickCallback() {
        @Override
        public void onClick(Function function) {

        }
    };

}
