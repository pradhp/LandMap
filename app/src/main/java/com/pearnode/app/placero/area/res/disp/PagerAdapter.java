package com.pearnode.app.placero.area.res.disp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by USER on 11/4/2017.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                AreaPictureDisplayFragment idf = new AreaPictureDisplayFragment();
                return idf;
            case 1:
                AreaVideoDisplayFragment vdf = new AreaVideoDisplayFragment();
                return vdf;
            case 2:
                AreaDocumentDisplayFragment ddf = new AreaDocumentDisplayFragment();
                return ddf;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return this.mNumOfTabs;
    }
}