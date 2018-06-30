package com.gmail.raducaz.arduinomate.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.db.entity.FunctionEntity;
import com.gmail.raducaz.arduinomate.databinding.FunctionFragmentBinding;
import com.gmail.raducaz.arduinomate.model.Function;
import com.gmail.raducaz.arduinomate.viewmodel.FunctionViewModel;

public class FunctionFragment extends Fragment {

    private static final String KEY_FUNCTION_ID = "function_id";

    private FunctionFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.function_fragment, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FunctionViewModel.Factory factory = new FunctionViewModel.Factory(
                getActivity().getApplication(), getArguments().getInt(KEY_FUNCTION_ID));

        final FunctionViewModel model = ViewModelProviders.of(this, factory)
                .get(FunctionViewModel.class);

        mBinding.setFunctionViewModel(model);

        subscribeToModel(model);

//        Button button = (Button) mBinding.getRoot().findViewById(R.id.btn_execute_function);
//        button.setOnClickListener(new OnClickListener() {
//            public void onClick(View b) {
//                Function function = (Function) b.getTag();
//
//                // Start sending command to Arduino
////                DataRepository repository = ((ArduinoMateApp) getActivity().getApplication()).getRepository();
////                ((ArduinoMateApp) getActivity().getApplication())
////                ((MainActivity) getActivity()).tcpClient.stop();
////                ((MainActivity) getActivity()).tcpClient = new TcpClientService("","");
////                ((MainActivity) getActivity()).tcpClient.execute(
////                        new CommentChannelClientInboundHandler((CommentEntity) comment, repository)
////                );
//            }
//        });


    }

    private void subscribeToModel(final FunctionViewModel model) {

        // Observe function data
        model.getObservableFunction().observe(this, new Observer<FunctionEntity>() {
            @Override
            public void onChanged(@Nullable FunctionEntity functionEntity) {
                model.setFunction(functionEntity);
            }
        });
    }

    /** Creates function fragment for specific function ID */
    public static FunctionFragment forFunction(int functionId) {
        FunctionFragment fragment = new FunctionFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_FUNCTION_ID, functionId);
        fragment.setArguments(args);
        return fragment;
    }

}
