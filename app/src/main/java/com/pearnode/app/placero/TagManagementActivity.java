package com.pearnode.app.placero;

/**
 * Created by USER on 12/13/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.tags.AddressTagsFragment;
import com.pearnode.app.placero.tags.MeasurementTagsFragment;
import com.pearnode.app.placero.tags.TagsDisplayMetaStore;
import com.pearnode.app.placero.tags.UserTagsFragment;
import com.pearnode.app.placero.util.ColorProvider;

public class TagManagementActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_assignment);

        Toolbar topToolbar = (Toolbar) this.findViewById(R.id.areas_tags_toolbar);
        setSupportActionBar(topToolbar);
        topToolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        ViewPager viewPager = (ViewPager) this.findViewById(R.id.areas_tags_tab_pager);
        viewPager.setAdapter(new DisplayTagsPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);

        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.areas_tags_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(ColorProvider.getDefaultToolBarColor());
    }

    public static class DisplayTagsPagerAdapter extends FragmentPagerAdapter {
        private Map<Integer, Fragment> store = new HashMap<>();

        public DisplayTagsPagerAdapter(FragmentManager fm) {
            super(fm);
            store.put(TagsDisplayMetaStore.TAB_ADDRESS_SEQ, new AddressTagsFragment());
            store.put(TagsDisplayMetaStore.TAB_AREA_SEQ, new MeasurementTagsFragment());
            store.put(TagsDisplayMetaStore.TAB_USER_SEQ, new UserTagsFragment());
        }

        @Override
        public Fragment getItem(int position) {
            return store.get(position);
        }

        @Override
        public int getCount() {
            return store.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            FragmentHandler identification = (FragmentHandler) store.get(position);
            return identification.getFragmentTitle();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
        startActivity(intent);
        finish();
    }

}