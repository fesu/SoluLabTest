package com.android.solulabtest.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.android.solulabtest.R;
import com.android.solulabtest.ui.DistanceFragment;
import com.android.solulabtest.ui.MapFragment;
import com.android.solulabtest.ui.RecordsFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a MapFragment (defined as a static inner class below).
        switch (position){
            case 0:
                return MapFragment.newInstance(position + 1);
            case 1:
                return DistanceFragment.newInstance(position + 1);
            case 2:
                return RecordsFragment.newInstance(position + 1);
            default:
                return MapFragment.newInstance(position + 1);

        }

    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}