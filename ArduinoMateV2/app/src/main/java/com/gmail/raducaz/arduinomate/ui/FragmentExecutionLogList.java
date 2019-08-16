package com.gmail.raducaz.arduinomate.ui;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.databinding.RecyclerViewBinding;
import com.gmail.raducaz.arduinomate.db.entity.ExecutionLogEntity;
import com.gmail.raducaz.arduinomate.db.entity.FunctionExecutionEntity;
import com.gmail.raducaz.arduinomate.model.ExecutionLog;
import com.gmail.raducaz.arduinomate.viewmodel.ExecutionLogListViewModel;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionExecutionViewModel;

import java.util.List;

public class FragmentExecutionLogList extends Fragment {

    public static final String TAG = "ExecutionLogListViewModel";

    private AdapterExecutionLogList mExecutionLogAdapter;

    private RecyclerViewBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_view, container, false);

        mExecutionLogAdapter = new AdapterExecutionLogList(mExecutionLogClickCallback);
        mBinding.myRecyclerView.setAdapter(mExecutionLogAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ExecutionLogListViewModel viewModel =
                ViewModelProviders.of(this).get(ExecutionLogListViewModel.class);

        subscribeUi(viewModel);
    }

    private void subscribeUi(ExecutionLogListViewModel viewModel) {
        FunctionExecutionViewModel.Factory factory = new FunctionExecutionViewModel.Factory(
                getActivity().getApplication(), ActivityDetail.functionId);
        final FunctionExecutionViewModel functionExecutionViewModel = ViewModelProviders.of(this, factory)
                .get(FunctionExecutionViewModel.class);

        functionExecutionViewModel.getObservableFunctionExecution().observe(this,
                new Observer<FunctionExecutionEntity>() {
                    @Override
                    public void onChanged(@Nullable FunctionExecutionEntity functionExecutionEntity) {

                        if(functionExecutionEntity != null) {
                            // Update the list when the data changes
                            viewModel.getExecutionLogs(functionExecutionEntity.getId())
                                    .observe(getActivity(), new Observer<List<ExecutionLogEntity>>() {
                                        @Override
                                        public void onChanged(@Nullable List<ExecutionLogEntity> myExecutionLogs) {
                                            if (myExecutionLogs != null) {

                                                mExecutionLogAdapter.setExecutionLogList(myExecutionLogs);
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

    private final ClickCallbackExecutionLog mExecutionLogClickCallback = new ClickCallbackExecutionLog() {
        @Override
        public void onClick(ExecutionLog executionLog) {

//            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
//                Context context = getContext();
//                Intent intent = new Intent(context, ActivityDetail.class);
//                intent.putExtra(ActivityDetail.EXTRA_ID, executionLog.getId());
//                context.startActivity(intent);
//            }
        }
    };
}
