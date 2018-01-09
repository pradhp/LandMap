package lm.pkp.com.landmap;

import android.R.drawable;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaDashboardDisplayMetaStore;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardOwnedFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardPublicFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardSharedFragment;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.reporting.AreaReportingService;
import lm.pkp.com.landmap.area.reporting.ReportingContext;
import lm.pkp.com.landmap.connectivity.ConnectivityChangeReceiver;
import lm.pkp.com.landmap.connectivity.services.AreaSynchronizationService;
import lm.pkp.com.landmap.connectivity.services.PositionSynchronizationService;
import lm.pkp.com.landmap.connectivity.services.ResourceSynchronizationService;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.FragmentFilterHandler;
import lm.pkp.com.landmap.custom.FragmentHandler;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.GlobalContext;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionElement;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.tags.TagElement;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.user.UserPersistableSelections;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaDashboardActivity extends AppCompatActivity {

    private boolean online = true;

    public boolean isOffline(){
        return !online;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        online = ConnectivityChangeReceiver.isConnected(this);

        setContentView(R.layout.activity_area_dashboard);
        registerReceiver(broadcastReceiver, new IntentFilter("INTERNET_LOST"));

        // Setup Toolbar
        Toolbar topToolbar = (Toolbar) this.findViewById(R.id.areas_display_toolbar);
        setSupportActionBar(topToolbar);
        topToolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        Toolbar bottomToolbar = (Toolbar) findViewById(R.id.areas_macro_toolbar);
        bottomToolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        final ViewPager viewPager = (ViewPager) findViewById(R.id.areas_display_tab_pager);
        viewPager.setAdapter(new DisplayAreasPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.areas_display_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        ImageView createAreaView = (ImageView) findViewById(id.action_area_create);
        createAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
                AreaElement areaElement = adh.insertAreaLocally(online);

                PermissionElement pe = new PermissionElement();
                pe.setUserId(UserContext.getInstance().getUserElement().getEmail());
                pe.setAreaId(areaElement.getUniqueId());
                pe.setFunctionCode(PermissionConstants.FULL_CONTROL);

                PermissionsDBHelper pdh = new PermissionsDBHelper(getApplicationContext());
                pdh.insertPermissionLocally(pe);
                areaElement.getUserPermissions().put(PermissionConstants.FULL_CONTROL, pe);

                // Resetting the context for new Area
                AreaContext.INSTANCE.setAreaElement(areaElement, getApplicationContext());
                adh = new AreaDBHelper(getApplicationContext(), new DataInsertServerCallback(areaElement));
                if (!adh.insertAreaToServer(areaElement)) {
                    Intent intent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
                    startActivity(intent);
                }
            }
        });

        ImageView generateReportView = (ImageView) this.findViewById(id.action_generate_report);
        generateReportView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPersistableSelections selections = UserContext.getInstance().getUserElement().getSelections();
                AreaElement selectedArea = selections.getArea();
                if (selectedArea == null) {
                    showMessage("You need to select a Place first", "error");
                    return;
                }
                ReportingContext reportingContext = ReportingContext.INSTANCE;
                if (!reportingContext.getGeneratingReport()) {
                    reportingContext.setAreaElement(selectedArea, getApplicationContext());
                    Intent serviceIntent = new Intent(getApplicationContext(), AreaReportingService.class);
                    startService(serviceIntent);
                    showMessage("Report generation started", "info");
                } else {
                    showMessage("Report generation is active. Please try later", "error");
                }
            }
        });

        ImageView tagAssignmentView = (ImageView) this.findViewById(id.action_tag_assignment);
        tagAssignmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TagAssignmentActivity.class);
                startActivity(intent);
                unregisterReceiver(broadcastReceiver);
                finish();
            }
        });

        final ImageView filterUTView = (ImageView) this.findViewById(id.action_filter_ut);
        UserElement userElement = UserContext.getInstance().getUserElement();
        final UserPersistableSelections userPersistableSelections = userElement.getSelections();
        filterUTView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayAreasPagerAdapter adapter = (DisplayAreasPagerAdapter) viewPager.getAdapter();
                FragmentFilterHandler filterHandler
                        = (FragmentFilterHandler) adapter.getItem(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab());
                if(userPersistableSelections.isFilter()){
                    filterHandler.resetFilter();
                    userPersistableSelections.setFilter(false);
                    filterUTView.setBackground(null);
                }else {
                    userPersistableSelections.setFilter(true);
                    filterUTView.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                    List<TagElement> tags = userPersistableSelections.getTags();
                    List<String> filterables = new ArrayList<>();
                    List<String> executables = new ArrayList<>();
                    for(TagElement tag: tags){
                        if(tag.getType().equals("filterable")){
                            filterables.add(tag.getName());
                        }else {
                            executables.add(tag.getName());
                        }
                    }
                    filterHandler.doFilter(filterables, executables);
                }
            }
        });

        ImageView saveOfflineView = (ImageView) findViewById(id.action_save_offline);
        final ArrayList<AreaElement> dirtyAreas = new AreaDBHelper(getApplicationContext()).getDirtyAreas();
        final ArrayList<PositionElement> dirtyPositions = new PositionsDBHelper(getApplicationContext()).getDirtyPositions();
        final ArrayList<DriveResource> dirtyResources = new DriveDBHelper(getApplicationContext()).getDirtyResources();

        saveOfflineView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean offlineSync = new Boolean(GlobalContext.INSTANCE.get(GlobalContext.SYNCHRONIZING_OFFLINE));
                if(!offlineSync){
                    if(dirtyAreas.size() == 0 && dirtyPositions.size() == 0 && dirtyResources.size() == 0){
                        showMessage("All caught up !!", "info");
                        return;
                    }
                    GlobalContext.INSTANCE.put(GlobalContext.SYNCHRONIZING_OFFLINE, new Boolean(true).toString());
                    startService(new Intent(getApplicationContext(), PositionSynchronizationService.class));
                    startService(new Intent(getApplicationContext(), ResourceSynchronizationService.class));
                    startService(new Intent(getApplicationContext(), AreaSynchronizationService.class));
                }else {
                    showMessage("Offline sync in progress..", "error");
                }
            }
        });

        if(dirtyAreas.size() > 0 || dirtyPositions.size() > 0 || dirtyResources.size() > 0){
            saveOfflineView.setBackgroundResource(R.drawable.rounded_corner);
        }

    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        if(areaElement != null){
            TabLayout tabLayout = (TabLayout) this.findViewById(id.areas_display_tab_layout);
            AreaDashboardDisplayMetaStore store = AreaDashboardDisplayMetaStore.INSTANCE;
            Integer position = store.getTabPositionByAreaType(areaElement.getType());
            tabLayout.getTabAt(position).select();
        }
        showErrorsIfAny();
    }

    private void showErrorsIfAny() {
        Bundle intentBundle = getIntent().getExtras();
        if (intentBundle != null) {
            String action = intentBundle.getString("action");
            String outcome = intentBundle.getString("outcome");
            String outcomeType = intentBundle.getString("outcome_type");
            showMessage(action + " " + outcomeType + ". " + outcome, outcomeType);
        }
    }


    public static class DisplayAreasPagerAdapter extends FragmentPagerAdapter {

        private Map<Integer, Fragment> store = new HashMap<>();

        public DisplayAreasPagerAdapter(FragmentManager fm) {
            super(fm);
            store.put(0, new AreaDashboardOwnedFragment());
            store.put(1, new AreaDashboardSharedFragment());
            store.put(2, new AreaDashboardPublicFragment());
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

    @Override
    public void onBackPressed() {
        new Builder(this).setIcon(drawable.ic_dialog_alert).setTitle("Exit Placero")
                .setMessage("Are you sure?")
                .setPositiveButton("yes", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                        unregisterReceiver(broadcastReceiver);
                        finish();
                    }
                }).setNegativeButton("no", null).show();
    }

    private class DataInsertServerCallback implements AsyncTaskCallback {

        private AreaElement areaElement;

        public DataInsertServerCallback(AreaElement areaElement){
            this.areaElement = areaElement;
        }
        @Override
        public void taskCompleted(Object result) {
            Intent intent = new Intent(getApplicationContext(), CreateAreaFoldersActivity.class);
            intent.putExtra("area_id", areaElement.getUniqueId());
            startActivity(intent);
        }
    }

    private void showMessage(String message, String type) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView(),
                message + ".", Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.getView().setBackgroundColor(Color.parseColor("#FAF7F6"));

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        if (type.equalsIgnoreCase("info")) {
            textView.setTextColor(Color.parseColor("#30601F"));
        } else if (type.equalsIgnoreCase("error")) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        textView.setTextSize(15);
        textView.setMaxLines(3);

        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(ColorProvider.DEFAULT_TOOLBAR_COLOR);
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(ColorProvider.DEFAULT_TOOLBAR_COLOR);
        snackbar.show();
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            online = !action.equalsIgnoreCase("INTERNET_LOST");
            // notify fragments
            final ViewPager viewPager = (ViewPager) findViewById(R.id.areas_display_tab_pager);
            DisplayAreasPagerAdapter adapter = (DisplayAreasPagerAdapter) viewPager.getAdapter();
            int fragmentCount = adapter.getCount();
            for (int i = 0; i < fragmentCount; i++) {
                FragmentHandler item = (FragmentHandler) adapter.getItem(i);
                item.setOffline(online);
            }
        }
    };

}
