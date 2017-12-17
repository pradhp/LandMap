package lm.pkp.com.landmap;

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

import lm.pkp.com.landmap.custom.FragmentIdentificationHandler;
import lm.pkp.com.landmap.tags.TagsAddressFragment;
import lm.pkp.com.landmap.tags.TagsAreaFragment;
import lm.pkp.com.landmap.tags.TagsDisplayMetaStore;
import lm.pkp.com.landmap.tags.TagsUserFragment;
import lm.pkp.com.landmap.util.ColorProvider;

public class TagAssignmentActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_assignment);

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
            FragmentIdentificationHandler identification = (FragmentIdentificationHandler) store.get(position);
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