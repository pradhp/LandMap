package com.pearnode.app.placero;

import android.R.string;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.tasks.RemoveAreaTask;
import com.pearnode.app.placero.connectivity.ConnectivityChangeReceiver;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.custom.LocationPositionReceiver;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.PermissionManager;
import com.pearnode.app.placero.position.CreatePositionTask;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionListAdaptor;
import com.pearnode.app.placero.position.UpdatePositionTask;
import com.pearnode.app.placero.provider.GPSLocationProvider;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.util.ColorProvider;
import com.pearnode.common.TaskFinishedListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AreaDetailsActivity extends AppCompatActivity implements LocationPositionReceiver {

    private Area area;
    private boolean online = true;

    private final ArrayList<Position> positionList = new ArrayList<Position>();
    private PositionListAdaptor adaptor;
    private AreaDetailsActivity activity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        online = ConnectivityChangeReceiver.isConnected(this);
        activity = this;

        setContentView(R.layout.activity_area_details);
        getSupportActionBar().hide();

        area = AreaContext.INSTANCE.getArea();

        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        ColorDrawable topDrawable = (ColorDrawable) topTB.getBackground().getCurrent();
        topDrawable.setColor(ColorProvider.getAreaToolBarColor(area));

        Toolbar bottomTB = (Toolbar) findViewById(R.id.toolbar_bottom);
        ColorDrawable bottomDrawable = (ColorDrawable) bottomTB.getBackground().getCurrent();
        bottomDrawable.setColor(ColorProvider.getAreaToolBarColor(area));

        ListView posListView = (ListView) findViewById(R.id.positionList);
        positionList.addAll(area.getPositions());

        adaptor = new PositionListAdaptor(this, R.id.positionList, positionList);
        posListView.setAdapter(adaptor);
        adaptor.notifyDataSetChanged();

        TextView areaNameView = (TextView) findViewById(R.id.area_name_text);
        String areaName = area.getName();
        if (areaName.length() > 16) {
            areaName = areaName.substring(0, 15).concat("...");
        }
        areaNameView.setText(areaName);

        findViewById(R.id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(R.id.positions_view_master).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        }

        ImageView areaEditItem = (ImageView) findViewById(R.id.action_area_edit);
        areaEditItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_NAME)
                        && PermissionManager.INSTANCE.hasAccess(PermissionConstants.CHANGE_DESCRIPTION)) {
                    Intent areaEditIntent = new Intent(getApplicationContext(), AreaEditActivity.class);
                    startActivity(areaEditIntent);
                }else {
                    showMessage("You do not have change rights !!", "error");
                }
            }
        });

        ImageView deleteAreaItem = (ImageView) findViewById(R.id.action_delete_area);
        deleteAreaItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionManager.INSTANCE.hasAccess(PermissionConstants.REMOVE_AREA)) {
                    showMessage("You do not have removal rights !!", "error");
                }else {
                    showAreaDeleteConfirmation();
                }
            }
        });

        ImageView markLocationItem = (ImageView) findViewById(R.id.action_mark_location);
        markLocationItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                    findViewById(R.id.positions_view_master).setVisibility(View.GONE);
                    findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                    new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                } else {
                    showMessage("You do not have Plotting rights !!", "error");
                }
            }
        });

        ImageView markLocationEmptyItem = (ImageView) findViewById(R.id.action_mark_location_empty);
        if (markLocationEmptyItem != null) {
            markLocationEmptyItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                        findViewById(R.id.positions_view_master).setVisibility(View.GONE);
                        findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                        new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                    } else {
                        showMessage("You do not have Plotting rights !!", "error");
                    }
                }
            });
        }

        ImageView plotItem = (ImageView) findViewById(R.id.action_plot_area);
        plotItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!online){
                    showMessage("No Internet..", "error");
                    return;
                }
                List<Position> positions = area.getPositions();
                if (positions.size() >= 1) {
                    Intent intent = new Intent(getApplicationContext(), AreaMapPlotterActivity.class);
                    startActivity(intent);
                } else {
                    showMessage("You need atleast 1 points to plot!!!", "error");
                }
            }
        });

        ImageView navigateItem = (ImageView) findViewById(R.id.action_navigate_area);
        navigateItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!online){
                    showMessage("No Internet..", "error");
                    return;
                }
                List<Position> positions = area.getPositions();
                if (positions.size() > 0) {
                    User user = UserContext.getInstance().getUser();
                    Position position = user.getSelections().getPosition();
                    if(position == null){
                        position = positions.get(0);
                    }
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="
                            + position.getLat() + "," + position.getLng()+"&daddr=" + area.getName());
                    Intent navigationIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    navigationIntent.setPackage("com.google.android.apps.maps");
                    startActivity(navigationIntent);
                } else {
                    showMessage("No positions available for navigation", "error");
                }
            }
        });

        ImageView shareAreaItem = (ImageView) findViewById(R.id.action_share_area);
        shareAreaItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!online){
                    showMessage("No Internet..", "error");
                    return;
                }
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.SHARE_READ_ONLY)) {
                    Intent areaShareIntent = new Intent(getApplicationContext(), AreaShareActivity.class);
                    startActivity(areaShareIntent);
                } else {
                    showMessage("You do not have area sharing rights !!", "error");
                }
            }
        });

        ImageView addResourcesItem = (ImageView) findViewById(R.id.action_drive_area);
        addResourcesItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.ADD_RESOURCES)) {
                    Intent intent = new Intent(getApplicationContext(), AreaAddResourcesActivity.class);
                    startActivity(intent);
                } else {
                    showMessage("You do not have resource addition rights !!", "error");
                }
            }
        });

        ImageView displayResItem = (ImageView) findViewById(R.id.action_display_res);
        displayResItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AreaMediaDisplayActivity.class);
                startActivity(intent);
            }
        });

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

    @Override
    public void receivedLocationPostion(Position pe) {
        pe.setName("P_" + pe.getId());

        Area ae = AreaContext.INSTANCE.getArea();
        List<Position> positions = ae.getPositions();
        if(!positions.contains(pe)){
            pe.setName("Position_" + positions.size());
            positions.add(pe);

            CreatePositionTask createPositionTask = new CreatePositionTask(getApplicationContext(), null);
            createPositionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pe);

            positionList.add(pe);
            adaptor.notifyDataSetChanged();
        }else{
            showMessage("Position already exists. Ignoring.", "info");
        }

        findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        findViewById(R.id.splash_panel).setVisibility(View.GONE);

        showPositionEdit(pe);
    }

    @Override
    public void locationFixTimedOut() {
        findViewById(R.id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(R.id.positions_view_master).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
        }
        showLocationFixFailureDialog();
    }

    @Override
    public void providerDisabled() {
        findViewById(R.id.splash_panel).setVisibility(View.GONE);
        if (positionList.size() == 0) {
            findViewById(R.id.positions_view_master).setVisibility(View.GONE);
            findViewById(R.id.position_list_empty_img).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
            findViewById(R.id.positions_view_master).setVisibility(View.VISIBLE);
        }
        showEnableGPSDialog();
    }

    @Override
    public void onBackPressed() {
        Intent areaDashboardIntent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
        startActivity(areaDashboardIntent);
        finish();
    }

    private void showEnableGPSDialog() {
        new Builder(this)
                .setTitle("GPS Location disabled.")
                .setMessage("Do you want to enable GPS ?")
                .setPositiveButton(string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private void showLocationFixFailureDialog() {
        new Builder(this)
                .setTitle("Location Fix failed. Please stay outdoors !!")
                .setMessage("Do you want to try again ?")
                .setPositiveButton(string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        findViewById(R.id.positions_view_master).setVisibility(View.GONE);
                        findViewById(R.id.position_list_empty_img).setVisibility(View.GONE);
                        findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                        new GPSLocationProvider(AreaDetailsActivity.this).getLocation();
                    }
                })
                .setNegativeButton(string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                    }
                })
                .show();
    }

    private void showAreaDeleteConfirmation() {
        new Builder(this)
                .setTitle("Delete Area !!, Cannot Undo")
                .setMessage("Do you really want to remove the area ?")
                .setPositiveButton(string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        RemoveAreaTask rat = new RemoveAreaTask(getApplicationContext(), new DeleteAreaCallback());
                        rat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, area);
                    }
                })
                .setNegativeButton(string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add some marker in the context saying that GPS is not enabled.
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private class DeleteAreaCallback implements TaskFinishedListener {

        @Override
        public void onTaskFinished(String response) {
            Intent removeIntent = new Intent(getApplicationContext(), AreaDashboardActivity.class);
            startActivity(removeIntent);
            finish();
        }
    }

    public void showPositionEdit(final Position position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Position Details");

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.position_edit,
                (ViewGroup) findViewById(R.id.position_edit_layout_root), false);
        builder.setView(v);

        final EditText posNameView = (EditText) v.findViewById(R.id.position_name);
        posNameView.setText(position.getName());

        final EditText posDescView = (EditText) v.findViewById(R.id.position_desc);
        posDescView.setText(position.getDescription());

        final Spinner spinner = (Spinner) v.findViewById(id.position_type);
        String ptype = StringUtils.capitalize(position.getType());
        spinner.setSelection(((ArrayAdapter)spinner.getAdapter()).getPosition(ptype));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                position.setName(posNameView.getText().toString());
                position.setType(spinner.getSelectedItem().toString());
                position.setDescription(posDescView.getText().toString());

                UpdatePositionTask updateTask = new UpdatePositionTask(getApplicationContext(), new TaskFinishedListener() {
                    @Override
                    public void onTaskFinished(String response) {
                        adaptor.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                updateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, position);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
