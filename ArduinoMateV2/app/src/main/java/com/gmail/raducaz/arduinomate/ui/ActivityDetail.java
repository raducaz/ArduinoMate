/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.raducaz.arduinomate.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.design.widget.ViewPagerBottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gmail.raducaz.arduinomate.R;

/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class ActivityDetail extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_NAME = "name";
    public static long functionId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        functionId = getIntent().getLongExtra(EXTRA_ID, 0);
        FragmentFunctionViewItem functionFragment = FragmentFunctionViewItem.forFunction(functionId);
        getSupportFragmentManager()
                .beginTransaction()
                //.addToBackStack("function")
                .replace(R.id.fragment_container,
                        functionFragment, null).commit();

        // Set title of Detail page
        this.setTitle(getIntent().getStringExtra(EXTRA_NAME));

        // Setting ViewPager for each Tabs
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        /* Add the other tabs needed except the first tab */
        tabs.addTab(tabs.newTab().setText("Executions"));
        tabs.addTab(tabs.newTab().setText("Logs"));
        tabs.addOnTabSelectedListener(onTabSelectedListener(viewPager));


        FloatingActionButton fab = findViewById(R.id.executeFABButton);
        View llBottomSheet = findViewById(R.id.bottom_sheet);
        // init the bottom sheet behavior
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    fab.animate().scaleX(1).scaleY(1).setDuration(300).start();

                    tabs.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);

                    TextView tv = findViewById(R.id.header);
                    tv.setVisibility(View.VISIBLE);
                } else if(BottomSheetBehavior.STATE_DRAGGING == newState){
                    TextView tv = findViewById(R.id.header);
                    tv.setVisibility(View.GONE);

                    tabs.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

//         Initial code
//        FragmentPinStateList pinStateListfragment = new FragmentPinStateList();
//        adapter.addFragment(pinStateListfragment, "Pins");
//        FragmentFunctionExecutionList executionListfragment = new FragmentFunctionExecutionList();
//        adapter.addFragment(executionListfragment, "Executions");
//        FragmentExecutionLogList executionLogListfragment = new FragmentExecutionLogList();
//        adapter.addFragment(executionLogListfragment, "Log");
//         Initial code

        /* Use the root fragment so it can be reused for the other tabs in the TabLayout */
        RootFragment initialFragment = new RootFragment();
        initialFragment.setReplacementFragment(new FragmentPinStateList());
        adapter.addFragment(initialFragment, "Pins");

        viewPager.setAdapter(adapter);
//        viewPager.setCurrentItem(0);
//        adapter.notifyDataSetChanged();

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }
        };
        viewPager.addOnPageChangeListener(pageChangeListener);
    }

    private TabLayout.OnTabSelectedListener onTabSelectedListener(final ViewPager viewPager) {

        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                android.support.v4.app.FragmentTransaction trans = getSupportFragmentManager()
                        .beginTransaction();
                /*
                 * IMPORTANT: We use the "root frame" defined in
                 * "root_fragment.xml" as the reference to replace fragment
                 */
                if(tab.getPosition()==0)
                    trans.replace(R.id.root_frame, new FragmentPinStateList());
                if(tab.getPosition()==1)
                    trans.replace(R.id.root_frame, new FragmentFunctionExecutionList());
                if(tab.getPosition()==2)
                    trans.replace(R.id.root_frame, new FragmentExecutionLogList());

                /*
                 * IMPORTANT: The following lines allow us to add the fragment
                 * to the stack and return to it later, by pressing back
                 */
//                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//                trans.addToBackStack(null);

                trans.commit();

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        };
    }

}
