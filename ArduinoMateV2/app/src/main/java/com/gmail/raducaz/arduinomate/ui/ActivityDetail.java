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

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.gmail.raducaz.arduinomate.ArduinoMateApp;
import com.gmail.raducaz.arduinomate.R;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionCaller;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionReset;
import com.gmail.raducaz.arduinomate.processes.TaskFunctionSync;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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
        tabs.addTab(tabs.newTab().setText("Pins"));
        tabs.addOnTabSelectedListener(onTabSelectedListener(viewPager));


        FloatingActionButton fab = findViewById(R.id.executeFABButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AppTheme));
                builder.setMessage(R.string.confirm_fct_execute)
                        .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ArduinoMateApp application = (ArduinoMateApp) getApplication();
                                TaskFunctionCaller functionCaller = new TaskFunctionCaller(
                                        application.getRepository(),
                                        functionId);
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

        View llBottomSheet = findViewById(R.id.bottom_sheet);
        // init the bottom sheet behavior
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
//                    fab.animate().scaleX(1).scaleY(1).setDuration(300).start();

                    tabs.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);

                    TextView tv = findViewById(R.id.header);
                    tv.setVisibility(View.VISIBLE);
                } else if(BottomSheetBehavior.STATE_DRAGGING == newState){
//                    fab.show();
                    TextView tv = findViewById(R.id.header);
                    tv.setVisibility(View.GONE);

                    tabs.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Micsorare buton cand se face expand
//                fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        TextView tv = findViewById(R.id.header);
        tv.setVisibility(View.GONE);

        tabs.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
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
        initialFragment.setReplacementFragment(new FragmentExecutionLogList());
        adapter.addFragment(initialFragment, "Logs");

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
                FragmentTransaction trans = getSupportFragmentManager()
                        .beginTransaction();
                /*
                 * IMPORTANT: We use the "root frame" defined in
                 * "root_fragment.xml" as the reference to replace fragment
                 */
                if(tab.getPosition()==0)
                    trans.replace(R.id.root_frame, new FragmentExecutionLogList());
                if(tab.getPosition()==1)
                    trans.replace(R.id.root_frame, new FragmentFunctionExecutionList());
                if(tab.getPosition()==2)
                    trans.replace(R.id.root_frame, new FragmentPinStateList());

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
            builder.setMessage(R.string.confirm_reset)
                    .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDetail.this);
                            builder.setMessage(R.string.confirm_also_remote)
                                    .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            ArduinoMateApp application = (ArduinoMateApp) getApplication();
                                            TaskFunctionReset functionReset = new TaskFunctionReset(application.getRepository(), functionId, false, true);
                                            new TaskExecutor().execute(functionReset);
                                        }
                                    })
                                    .setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            ArduinoMateApp application = (ArduinoMateApp) getApplication();
                                            TaskFunctionReset functionReset = new TaskFunctionReset(application.getRepository(), functionId, false, false);
                                            new TaskExecutor().execute(functionReset);
                                        }
                                    });

                            // Create the AlertDialog object and return it
                            AlertDialog alert = builder.create();
                            alert.show();

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

        }else if (id==R.id.action_restart)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
            builder.setMessage(R.string.confirm_restart)
                    .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArduinoMateApp application = (ArduinoMateApp) getApplication();
                            TaskFunctionReset functionReset = new TaskFunctionReset(application.getRepository(), functionId, true,true);
                            new TaskExecutor().execute(functionReset);
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

        }else if (id==R.id.action_sync)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
            builder.setMessage(R.string.confirm_sync)
                    .setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ArduinoMateApp application = (ArduinoMateApp) getApplication();
                            TaskFunctionSync caller = new TaskFunctionSync(application.getRepository(), functionId);
                            new TaskExecutor().execute(caller);
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

        return super.onOptionsItemSelected(item);
    }

}
