package com.gmail.raducaz.arduinomate.ui;

import androidx.viewpager.widget.ViewPager;

import android.support.design.widget.ViewPagerBottomSheetBehavior;
import android.view.View;

public class BottomSheetViewPagerListener extends ViewPager.SimpleOnPageChangeListener {
    private final ViewPager mViewPager;
    private final ViewPagerBottomSheetBehavior<View> mBehavior;

    BottomSheetViewPagerListener(ViewPager viewPager, View bottomSheetParent) {
        mViewPager = viewPager;
        mBehavior = ViewPagerBottomSheetBehavior.from(bottomSheetParent);
    }

    @Override
    public void onPageSelected(int position) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
//                mBehavior.updateScrollingChild();
            }
        });
    }
}