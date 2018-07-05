package com.gmail.raducaz.arduinomate.ui;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.RecyclerViewBinding;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.model.FunctionExecution;
import com.gmail.raducaz.arduinomate.viewmodel.DeviceViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionExecutionListViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionListViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionViewModel;

import java.util.List;

public class FragmentFunctionExecutionList extends Fragment {

    public static final String TAG = "FunctionExecutionListViewModel";

    private AdapterFunctionExecutionList mFunctionExecutionAdapter;

    private RecyclerViewBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_view, container, false);

        mFunctionExecutionAdapter = new AdapterFunctionExecutionList(mFunctionExecutionClickCallback);
        mBinding.myRecyclerView.setAdapter(mFunctionExecutionAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final FunctionExecutionListViewModel viewModel =
                ViewModelProviders.of(this).get(FunctionExecutionListViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(FunctionExecutionListViewModel viewModel) {

        // Update the list when the data changes
        viewModel.getFunctionExecutions(ActivityDetail.functionId)
                .observe(this, new Observer<List<FunctionExecutionEntity>>() {
            @Override
            public void onChanged(@Nullable List<FunctionExecutionEntity> myFunctionExecutions) {
                if (myFunctionExecutions != null) {
//                    mBinding.setIsLoading(false);
                    mFunctionExecutionAdapter.setFunctionExecutionList(myFunctionExecutions);
                } else {
//                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    private final ClickCallbackFunctionExecution mFunctionExecutionClickCallback = new ClickCallbackFunctionExecution() {
        @Override
        public void onClick(FunctionExecution functionExecution) {

//            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
//                Context context = getContext();
//                Intent intent = new Intent(context, ActivityDetail.class);
//                intent.putExtra(ActivityDetail.EXTRA_ID, functionExecution.getId());
//                context.startActivity(intent);
//            }
        }
    };
}
