package com.pearnode.app.placero;

import android.R.drawable;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.AreaDashboardDisplayMetaStore;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.dashboard.AreaDashboardOwnedFragment;
import com.pearnode.app.placero.area.dashboard.AreaDashboardPublicFragment;
import com.pearnode.app.placero.area.dashboard.AreaDashboardSharedFragment;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.reporting.AreaReportingService;
import com.pearnode.app.placero.area.reporting.ReportingContext;
import com.pearnode.app.placero.area.res.disp.AreaListAdaptor;
import com.pearnode.app.placero.area.tasks.CreateAreaTask;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.connectivity.services.AreaSynchronizationService;
import com.pearnode.app.placero.connectivity.services.PositionSynchronizationService;
import com.pearnode.app.placero.connectivity.services.ResourceSynchronizationService;
import com.pearnode.app.placero.custom.FragmentFilterHandler;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.custom.GlobalContext;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.Permission;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.tags.Tag;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.user.UserPersistableSelections;
import com.pearnode.app.placero.util.ColorProvider;
import com.pearnode.common.TaskFinishedListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

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

                Area area = new Area();
                area.setName("PL_" + area.getId());
                area.setCreatedBy(UserContext.getInstance().getUser().getEmail());

                Permission pe = new Permission();
                pe.setUserId(UserContext.getInstance().getUser().getEmail());
                pe.setAreaId(area.getId());
                pe.setFunctionCode(PermissionConstants.FULL_CONTROL);
                area.getPermissions().put(PermissionConstants.FULL_CONTROL, pe);

                CreateAreaCallback createCallback = new CreateAreaCallback();
                createCallback.setArea(area);
                CreateAreaTask createAreaTask = new CreateAreaTask(getApplicationContext(), createCallback);
                createAreaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, area);
            }
        });

        ImageView generateReportView = (ImageView) this.findViewById(id.action_generate_report);
        generateReportView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPersistableSelections selections = UserContext.getInstance().getUser().getSelections();
                Area selectedArea = selections.getArea();
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
                finish();
            }
        });

        final ImageView filterUTView = (ImageView) this.findViewById(id.action_filter_ut);
        User user = UserContext.getInstance().getUser();
        final UserPersistableSelections userPersistableSelections = user.getSelections();
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
                    List<Tag> tags = userPersistableSelections.getTags();
                    List<String> filterables = new ArrayList<>();
                    List<String> executables = new ArrayList<>();
                    for(Tag tag: tags){
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

        ImageView saveOfflineView = (ImageView) findViewById(R.id.action_save_offline);
        final ArrayList<Area> dirtyAreas = new AreaDBHelper(getApplicationContext()).getDirtyAreas();
        final ArrayList<Position> dirtyPositions = new PositionsDBHelper(getApplicationContext()).getDirtyPositions();
        final List<Media> dirtyMedia = new MediaDataBaseHandler(getApplicationContext()).getDirtyMedia();

        saveOfflineView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean offlineSync = new Boolean(GlobalContext.INSTANCE.get(GlobalContext.SYNCHRONIZING_OFFLINE));
                if(!offlineSync){
                    if(dirtyAreas.size() == 0 && dirtyPositions.size() == 0 && dirtyMedia.size() == 0){
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

        if(dirtyAreas.size() > 0 || dirtyPositions.size() > 0 || dirtyMedia.size() > 0){
            saveOfflineView.setBackgroundResource(R.drawable.rounded_corner);
        }

        ImageView plotAreasAction = (ImageView) findViewById(R.id.action_plot_areas);
        plotAreasAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayAreasPagerAdapter adapter = (DisplayAreasPagerAdapter) viewPager.getAdapter();
                FragmentHandler fragment
                        = (FragmentHandler) adapter.getItem(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab());
                AreaListAdaptor viewAdaptor = (AreaListAdaptor) fragment.getViewAdaptor();
                List<String> areaIds = new ArrayList<>();
                ArrayList<Area> adaptorItems = viewAdaptor.getItems();
                if((adaptorItems == null) || (adaptorItems.size() == 0)){
                    showMessage("Nothing to plot..", "error");
                    return;
                }
                for (Area eachItem: adaptorItems) {
                    areaIds.add(eachItem.getId());
                }
                Intent intent = new Intent(getApplicationContext(), CombinedAreasPlotterActivity.class);
                intent.putExtra("area_ids", areaIds.toArray());
                startActivity(intent);
            }
        });
    }

    private class CreateAreaCallback implements TaskFinishedListener {

        private Area area = null;

        public Area getArea() {
            return this.area;
        }

        public void setArea(Area area) {
            this.area = area;
        }

        @Override
        public void onTaskFinished(String response) {
            AreaContext.INSTANCE.setArea(area, getApplicationContext());

            PermissionsDBHelper pdh = new PermissionsDBHelper(getApplicationContext());
            Map<String, Permission> permissions = area.getPermissions();
            Collection<Permission> permissionElements = permissions.values();
            for(Permission permission : permissionElements){
                pdh.insertPermissionLocally(permission);
            }

            Intent intent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Area area = AreaContext.INSTANCE.getArea();
        if(area != null){
            TabLayout tabLayout = (TabLayout) findViewById(id.areas_display_tab_layout);
            AreaDashboardDisplayMetaStore store = AreaDashboardDisplayMetaStore.INSTANCE;
            Integer position = store.getTabPositionByAreaType(area.getType());
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
                        finish();
                    }
                }).setNegativeButton("no", null).show();
    }

    private void showMessage(String message, String type) {
        if (type.equalsIgnoreCase("info")) {
            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(type)
                    .setContentText(message)
                    .show();
        } else if (type.equalsIgnoreCase("error")) {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(type)
                    .setContentText(message)
                    .show();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(type)
                    .setContentText(message)
                    .show();
        }
    }

}
