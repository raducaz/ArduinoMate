package com.gmail.raducaz.arduinomate.ui;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gmail.raducaz.arduinomate.R;

public class RootFragment extends Fragment {

        private static final String TAG = "RootFragment";
        private Fragment mReplacementFragment = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            /* Inflate the layout for this fragment */
            View view = inflater.inflate(R.layout.root_fragment, container, false);

            FragmentTransaction trans = getChildFragmentManager()
                    .beginTransaction();
            trans.replace(R.id.root_frame, mReplacementFragment);
            trans.commit();

            return view;
        }

        public void setReplacementFragment(Fragment replacementFragment)
        {
            mReplacementFragment = replacementFragment;
        }
}
