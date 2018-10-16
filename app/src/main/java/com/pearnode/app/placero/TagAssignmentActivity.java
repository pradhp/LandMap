package com.pearnode.app.placero;

/**
 * Created by USER on 12/13/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.pearnode.app.placero.tags.TagsAddressFragment;
import com.pearnode.app.placero.tags.TagsAreaFragment;
import com.pearnode.app.placero.tags.TagsDisplayMetaStore;
import com.pearnode.app.placero.tags.TagsUserFragment;
import com.pearnode.app.placero.util.ColorProvider;

public class TagAssignmentActivity extends AppCompatActivity{

    private boolean offline = false;

    public boolean isOffline() {
        return this.offline;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_assignment);
        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_LOST"));

        // Setup Toolbar
        Toolbar topToolbar = (Toolbar) this.findViewById(R.id.areas_tags_toolbar);
        setSupportActionBar(topToolbar);
        topToolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        ViewPager viewPager = (ViewPager) this.findViewById(R.id.areas_tags_tab_pager);
        // Assign created adapter to viewPager
        viewPager.setAdapter(new DisplayTagsPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);

        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.areas_tags_tab_layout);
        // This method setup all required method for TabLayout with Viewpager
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(ColorProvider.getDefaultToolBarColor());
    }

    public static class DisplayTagsPagerAdapter extends FragmentPagerAdapter {

        private Map<Integer, Fragment> store = new HashMap<>();

        public DisplayTagsPagerAdapter(FragmentManager fm) {
            super(fm);
            store.put(TagsDisplayMetaStore.TAB_ADDRESS_SEQ, new TagsAddressFragment());
            store.put(TagsDisplayMetaStore.TAB_AREA_SEQ, new TagsAreaFragment());
            store.put(TagsDisplayMetaStore.TAB_USER_SEQ, new TagsUserFragment());
        }

        @Override
        // For each tab different fragment is returned
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

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase("INTERNET_LOST")){
                offline = true;
            }else {
                offline = false;
            }

            // notify fragments
            final ViewPager viewPager = (ViewPager) findViewById(R.id.areas_display_tab_pager);
            DisplayTagsPagerAdapter adapter = (DisplayTagsPagerAdapter) viewPager.getAdapter();
            int fragmentCount = adapter.getCount();
            for (int i = 0; i < fragmentCount; i++) {
                FragmentHandler item = (FragmentHandler) adapter.getItem(i);
                item.setOffline(offline);
            }
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
        startActivity(intent);

        unregisterReceiver(broadcastReceiver);
        finish();
    }

}