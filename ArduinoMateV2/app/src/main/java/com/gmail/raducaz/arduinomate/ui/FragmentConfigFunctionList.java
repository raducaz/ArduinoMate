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
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionListViewModel;

import java.util.List;

public class FragmentConfigFunctionList extends Fragment {

    public static final String TAG = "FunctionListViewModel";

    private AdapterConfigFunctionList mFunctionAdapter;

    private RecyclerViewBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_view, container, false);

        mFunctionAdapter = new AdapterConfigFunctionList(mFunctionClickCallback);
        mBinding.myRecyclerView.setAdapter(mFunctionAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final FunctionListViewModel viewModel =
                ViewModelProviders.of(this).get(FunctionListViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(FunctionListViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getFunctions().observe(this, new Observer<List<FunctionEntity>>() {
            @Override
            public void onChanged(@Nullable List<FunctionEntity> myFunctions) {
                if (myFunctions != null) {
//                    mBinding.setIsLoading(false);
                    mFunctionAdapter.setFunctionList(myFunctions);
                } else {
//                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    private final ClickCallbackFunction mFunctionClickCallback = new ClickCallbackFunction() {
        @Override
        public void onClick(View v, Function function) {

            if(v.getTag().equals("DELETE"))
            {}
            else if (v.getTag()==null) {
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                    Context context = getContext();
                    Intent intent = new Intent(context, ActivityDetail.class);
                    intent.putExtra(ActivityDetail.EXTRA_ID, function.getId());
                    context.startActivity(intent);
                }
            }
        }
    };
}
