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
import com.gmail.raducaz.arduinomate.model.ExecutionLog;
import com.gmail.raducaz.arduinomate.viewmodel.ExecutionLogListViewModel;

import java.util.List;

public class FragmentAllExecutionLogList extends Fragment {

    public static final String TAG = "FragmentAllExecutionLogList";

    private AdapterAllExecutionLogList mExecutionLogAdapter;

    private RecyclerViewBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.recycler_view, container, false);

        mExecutionLogAdapter = new AdapterAllExecutionLogList(mExecutionLogClickCallback);
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

        viewModel.getAllExecutionLogs().observe(this, new Observer<List<ExecutionLogEntity>>() {
            @Override
            public void onChanged(@Nullable List<ExecutionLogEntity> allExecutionLogs) {
                if (allExecutionLogs != null) {
//                    mBinding.setIsLoading(false);
                    mExecutionLogAdapter.setExecutionLogList(allExecutionLogs);
                } else {
//                    mBinding.setIsLoading(true);
                }
                // espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });

    }

    private final ClickCallbackAllExecutionLog mExecutionLogClickCallback = new ClickCallbackAllExecutionLog() {
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
