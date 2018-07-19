package com.gmail.raducaz.arduinomate.ui;


import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.RecyclerViewBinding;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.db.entity.PinStateEntity;
import com.gmail.raducaz.arduinomate.model.PinState;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.PinStateListViewModel;

import java.util.List;

public class FragmentPinStateList extends Fragment {

    public static final String TAG = "PinStateListViewModel";

    private AdapterPinStateList mPinStateAdapter;

    private RecyclerViewBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_view, container, false);

        mPinStateAdapter = new AdapterPinStateList(mPinStateClickCallback);
        mBinding.myRecyclerView.setAdapter(mPinStateAdapter);

        mBinding.myRecyclerView.setHasFixedSize(true);
        // Set padding for Tiles
        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
        mBinding.myRecyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        mBinding.myRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final PinStateListViewModel viewModel =
                ViewModelProviders.of(this).get(PinStateListViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(PinStateListViewModel viewModel) {
        FunctionViewModel.Factory factory = new FunctionViewModel.Factory(
                getActivity().getApplication(), ActivityDetail.functionId);
        final FunctionViewModel functionViewModel = ViewModelProviders.of(this, factory)
                .get(FunctionViewModel.class);

        functionViewModel.getObservableFunction().observe(this,
                new Observer<FunctionEntity>() {
                    @Override
                    public void onChanged(@Nullable FunctionEntity functionEntity) {

                        if(functionEntity != null) {
                            // Update the list when the data changes
                            viewModel.getDeviceCurrentPinStates(functionEntity.getDeviceId())
                                    .observe(getActivity(), new Observer<List<PinStateEntity>>() {
                                        @Override
                                        public void onChanged(@Nullable List<PinStateEntity> myPinStates) {
                                            if (myPinStates != null) {

                                                mPinStateAdapter.setPinStateList(myPinStates);
                                            } else {
                                            }
                                            mBinding.executePendingBindings();
                                        }
                                    });
                        }

                    }
                }

        );

    }

    private final ClickCallbackPinState mPinStateClickCallback = new ClickCallbackPinState() {
        @Override
        public void onClick(View v, PinState pinState) {
            Snackbar.make(v, "State from " + pinState.getFromDate(),
                    Snackbar.LENGTH_LONG).show();
        }
    };
}
