package com.gmail.raducaz.arduinomate.ui;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {

        // Working solution for single fragment
        if(mFragmentList.size()>0)
            return mFragmentList.get(0);
        return new RootFragment();
    }

    @Override
    public int getCount() {
        return mFragmentTitleList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

//    @Override
//    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.setPrimaryItem(container, position, object);
//        for (int i = 0; i < container.getChildCount(); i++) {
//            //First disable nested scrolling for all instantiated child views
//            //((NestedScrollView)container.getChildAt(i)).setNestedScrollingEnabled(false);
////            if(i!= position)
////                container.removeViewAt(i);
////            if(container.getChildAt(i) instanceof LinearLayout)
////            {
////                LinearLayout linearLayout = (LinearLayout) container.getChildAt(i);
////                if(linearLayout.getChildCount() > 0 && linearLayout.getChildCount()>0) {
////                    if(linearLayout.getChildAt(0) instanceof RecyclerView)
////                    {
////                        RecyclerView r = (RecyclerView)linearLayout.getChildAt(0);
////                        if(position == i)
////                            //r.setNestedScrollingEnabled(false);
////                        //else
////                            r.setNestedScrollingEnabled(true);
////                    }
////                }
////            }
//        }
//
//        //Enable nested scrolling for the primary item
//        //FragmentFunctionList fragment = (FragmentFunctionList)object;
//        //RecyclerView r = fragment.mBinding.myRecyclerView;
//        //r.setNestedScrollingEnabled();
////        ViewDataBinding binding = (ViewDataBinding) object;
////        NestedScrollView current = ((NestedScrollView)binding.getRoot());
////        current.setNestedScrollingEnabled(true);
//
//        container.requestLayout();
//    }
}
