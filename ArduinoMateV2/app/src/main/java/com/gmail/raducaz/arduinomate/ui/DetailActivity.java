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

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.raducaz.arduinomate.R;

/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        int functionId = getIntent().getIntExtra(EXTRA_ID, 0);
        FunctionFragment functionFragment = FunctionFragment.forFunction(functionId);
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("function")
                .replace(R.id.fragment_container,
                        functionFragment, null).commit();

        // Set title of Detail page
        collapsingToolbar.setTitle(getString(R.string.item_title));


        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // All Tab
        FunctionListFragment fragment = new FunctionListFragment();
        adapter.addFragment(fragment, "Indicators");

        viewPager.setAdapter(adapter);
    }
}
