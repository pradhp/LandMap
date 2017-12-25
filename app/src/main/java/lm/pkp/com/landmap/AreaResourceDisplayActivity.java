package lm.pkp.com.landmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.res.disp.AreaDocumentDisplayFragment;
import lm.pkp.com.landmap.area.res.disp.AreaPictureDisplayFragment;
import lm.pkp.com.landmap.area.res.disp.AreaVideoDisplayFragment;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.util.ColorProvider;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaResourceDisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(layout.activity_area_res_display);
        Bundle extras = this.getIntent().getExtras();

        int selectedTab = 0;
        if (extras != null) {
            selectedTab = extras.getInt("tab_position");
        }

        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();

        ViewPager viewPager = (ViewPager) this.findViewById(id.area_tab_pager);
        // Assign created adapter to viewPager
        viewPager.setAdapter(new DisplayResourcesPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) this.findViewById(id.tab_layout);
        // This method setup all required method for TabLayout with Viewpager
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(ColorProvider.getAreaToolBarColor(areaElement));
        tabLayout.getTabAt(selectedTab).select();
    }

    public static class DisplayResourcesPagerAdapter extends FragmentPagerAdapter {
        // As we are implementing two tabs
        private static final int NUM_ITEMS = 3;

        public DisplayResourcesPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // For each tab different fragment is returned
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AreaPictureDisplayFragment();
                case 1:
                    return new AreaVideoDisplayFragment();
                case 2:
                    return new AreaDocumentDisplayFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Images";
                case 1:
                    return "Videos";
                case 2:
                    return "Documents";
                default:
                    return null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent areaDashboardIntent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
        startActivity(areaDashboardIntent);
        finish();
    }
}
