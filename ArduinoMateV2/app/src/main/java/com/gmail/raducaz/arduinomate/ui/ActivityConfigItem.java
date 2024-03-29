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

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail.raducaz.arduinomate.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class ActivityConfigItem extends AppCompatActivity {

    public static final String EXTRA_ID = "id";
    public static long deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_item);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar =
                findViewById(R.id.collapsing_toolbar);

        deviceId = getIntent().getLongExtra(EXTRA_ID, 0);
        FragmentDeviceEditItem deviceFragment = FragmentDeviceEditItem.forDevice(deviceId);
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("device")
                .replace(R.id.fragment_container,
                        deviceFragment, null).commit();

        // Set title of Detail page
        collapsingToolbar.setTitle(getString(R.string.item_title));

    }

}
