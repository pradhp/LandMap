package com.pearnode.app.placero;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.pearnode.app.placero.R.drawable;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.area.tasks.UpdateAreaTask;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.custom.MapWrapperLayout;
import com.pearnode.app.placero.custom.OnInfoWindowElemTouchListener;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.PermissionManager;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.position.PositionsDBHelper;
import com.pearnode.app.placero.position.RemovePositionTask;
import com.pearnode.app.placero.position.UpdatePositionTask;
import com.pearnode.app.placero.util.ColorProvider;
import com.pearnode.common.TaskFinishedListener;

public class AreaMapPlotterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private LinkedHashMap<Marker, Position> positionMarkers = new LinkedHashMap<>();
    private LinkedHashMap<Marker, Media> resourceMarkers = new LinkedHashMap<>();
    private LinkedHashMap<Polygon, Marker> polygonMarkers = new LinkedHashMap<>();

    private Polygon polygon;
    private Marker centerMarker;

    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private ImageView infoImage;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private SupportMapFragment mapFragment;

    private Button infoButton;
    private final AreaContext ac = AreaContext.INSTANCE;
    private final Area ae = ac.getAreaElement();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_plotter);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(id.googleMap);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap gmap) {
        googleMap = gmap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setTrafficEnabled(true);
        googleMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                googleMap.snapshot(new MapSnapshotTaker());
            }
        });

        UiSettings settings = googleMap.getUiSettings();
        settings.setMapToolbarEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);

        plotPolygonUsingPositions();
        plotMediaPoints();

        initializeMapEventPropagation();
        initializeMapInfoWindow();
    }

    private void plotPolygonUsingPositions() {
        List<Position> positions = ae.getPositions();
        int noOfPositions = positions.size();

        Set<Marker> markers = positionMarkers.keySet();
        for (Marker m : markers) {
            m.remove();
        }
        positionMarkers.clear();
        if (centerMarker != null) {
            centerMarker.remove();
        }
        for (int i = 0; i < noOfPositions; i++) {
            Position pe = positions.get(i);
            String positionType = pe.getType();
            if(!positionType.equalsIgnoreCase("media")){
                positionMarkers.put(buildMarker(pe), pe);
            }
        }

        Position centerPosition = ae.getCenterPosition();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(centerPosition.getLat(), centerPosition.getLng()));
        centerMarker = googleMap.addMarker(markerOptions);
        centerMarker.setTag("AreaCenter");
        centerMarker.setVisible(true);
        centerMarker.setAlpha((float) 0.1);
        centerMarker.setTitle(ae.getName());

        zoomCameraToPosition(centerMarker);

        PolygonOptions polyOptions = new PolygonOptions();
        polyOptions = polyOptions
                .strokeColor(ColorProvider.DEFAULT_POLYGON_BOUNDARY)
                .fillColor(ColorProvider.DEFAULT_POLYGON_FILL);
        markers = positionMarkers.keySet();

        List<Marker> markerList = new ArrayList<>(markers);
        for (Marker m : markerList) {
            Position position = positionMarkers.get(m);
            if(position.getType().equalsIgnoreCase("boundary")){
                polyOptions.add(m.getPosition());
            }
        }
        polygon = googleMap.addPolygon(polyOptions);

        double polygonAreaSqMt = SphericalUtil.computeArea(polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        AreaMeasure areaMeasure = new AreaMeasure(polygonAreaSqFt);
        ae.setMeasure(areaMeasure);

        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            UpdateAreaTask updateAreaTask = new UpdateAreaTask(getApplicationContext(), new UpdateAreaFinishListener());
            updateAreaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ae);
        }
        polygonMarkers.put(polygon, centerMarker);
    }

    private class UpdateAreaFinishListener implements TaskFinishedListener {

        @Override
        public void onTaskFinished(String response) {
            AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
            adh.insertAreaAddressTagsLocally(ae);
            adh.insertAreaAddressTagsOnServer(ae);
        }
    }

    private void plotMediaPoints() {
        List<Media> pictures = ae.getPictures();
        BitmapDescriptor pictureBMap = BitmapDescriptorFactory.fromResource(drawable.camera_map);
        for (int i = 0; i < pictures.size(); i++) {
            Media picture = pictures.get(i);
            if (picture.getName().equalsIgnoreCase("plot_screenshot.png")) {
                continue;
            }
            LatLng position = new LatLng(new Double(picture.getLat()), new Double(picture.getLng()));

            MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(pictureBMap);
            markerOptions.position(position);

            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag("MediaMarker");
            marker.setTitle(picture.getName());
            marker.setDraggable(false);
            marker.setVisible(true);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(marker.getPosition(), centerMarker.getPosition())
                    .width(5)
                    .color(ColorProvider.DEFAULT_POLYGON_MEDIA_LINK);
            Polyline line = googleMap.addPolyline(polylineOptions);
            line.setClickable(true);
            line.setVisible(true);
            line.setZIndex(1);

            resourceMarkers.put(marker, picture);
        }

        List<Media> videos = ae.getVideos();
        BitmapDescriptor videoBMap = BitmapDescriptorFactory.fromResource(drawable.video_map);
        for (int i = 0; i < videos.size(); i++) {
            Media video = videos.get(i);
            LatLng position = new LatLng(new Double(video.getLat()), new Double(video.getLng()));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(videoBMap);
            markerOptions.position(position);

            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag("MediaMarker");
            marker.setTitle(video.getName());
            marker.setDraggable(false);
            marker.setVisible(true);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(marker.getPosition(), centerMarker.getPosition())
                    .width(5)
                    .color(ColorProvider.DEFAULT_POLYGON_MEDIA_LINK);
            Polyline line = googleMap.addPolyline(polylineOptions);
            line.setClickable(true);
            line.setVisible(true);
            line.setZIndex(1);

            resourceMarkers.put(marker, video);
        }
    }

    private void initializeMapInfoWindow() {
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Position position = positionMarkers.get(marker);
                Media resource = resourceMarkers.get(marker);
                String markerTag = (String) marker.getTag();

                if (markerTag.equalsIgnoreCase("PositionMarker")) {
                    infoImage.setImageResource(drawable.position);
                    infoTitle.setText(position.getName());
                    CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(position.getCreatedOn()),
                            System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    DecimalFormat formatter = new DecimalFormat("##.##");
                    double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), centerMarker.getPosition());
                    infoSnippet.setText(formatter.format(distance) + " mts, " + timeSpan.toString());
                    infoButton.setText("Remove");
                }

                if (markerTag.equalsIgnoreCase("AreaCenter")) {
                    infoTitle.setText(marker.getTitle());
                    LatLng markerPosition = marker.getPosition();
                    DecimalFormat locFormat = new DecimalFormat("##.####");
                    String centerPosStr = "Lat: " + locFormat.format(markerPosition.latitude)
                            + ", Lng: " + locFormat.format(markerPosition.longitude);
                    infoSnippet.setText(centerPosStr);
                    infoImage.setImageResource(drawable.position);
                    infoButton.setVisibility(View.GONE);
                }

                if (markerTag.equalsIgnoreCase("MediaMarker")) {
                    String thumbRootPath = "";
                    if (resource.getType().equalsIgnoreCase("Video")) {
                        thumbRootPath = ac.getAreaLocalVideoThumbnailRoot(ae.getId()).getAbsolutePath();
                    } else {
                        thumbRootPath = ac.getAreaLocalPictureThumbnailRoot(ae.getId()).getAbsolutePath();
                    }
                    String thumbnailPath = thumbRootPath + File.separatorChar + resource.getName();
                    File thumbFile = new File(thumbnailPath);
                    if (thumbFile.exists()) {
                        Bitmap bMap = BitmapFactory.decodeFile(thumbnailPath);
                        infoImage.setImageBitmap(bMap);
                    }
                    infoTitle.setText(resource.getName());
                    CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(resource.getCreatedOn()),
                            System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    DecimalFormat formatter = new DecimalFormat("##.##");
                    double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), centerMarker.getPosition());
                    infoSnippet.setText(formatter.format(distance) + " mts, " + timeSpan.toString());
                    infoButton.setText("Open");
                }

                infoButtonListener.setMarker(marker);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });
    }

    private void initializeMapEventPropagation() {
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 35));

        infoWindow = (ViewGroup) getLayoutInflater().inflate(layout.info_window, null);
        infoTitle = (TextView) infoWindow.findViewById(id.title);
        infoSnippet = (TextView) infoWindow.findViewById(id.snippet);
        infoImage = (ImageView) infoWindow.findViewById(id.info_element_img);
        infoButton = (Button) infoWindow.findViewById(id.map_info_action);

        infoButtonListener = new OnInfoWindowElemTouchListener(infoButton) {

            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                File areaLocalImageRoot = ac.getAreaLocalImageRoot(ae.getId());
                File areaLocalVideoRoot = ac.getAreaLocalVideoRoot(ae.getId());
                String imageRootPath = areaLocalImageRoot.getAbsolutePath();
                String videoRootPath = areaLocalVideoRoot.getAbsolutePath();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);

                final Position position = positionMarkers.get(marker);
                if (position != null) {
                    if(PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)){
                        RemovePositionTask removeTask = new RemovePositionTask(getApplicationContext(), new TaskFinishedListener() {
                            @Override
                            public void onTaskFinished(String response) {
                                ae.getPositions().remove(position);
                                ac.deriveCenter(ae);

                                polygon.remove();
                                plotPolygonUsingPositions();
                            }
                        });
                        removeTask.execute(AsyncTask.THREAD_POOL_EXECUTOR, position);
                    }
                }else {
                    Media resource = resourceMarkers.get(marker);
                    if(resource != null){
                        String type = resource.getType();
                        if(type.equalsIgnoreCase("Image")){
                            File file = new File(imageRootPath + File.separatorChar + resource.getName());
                            if(file.exists()){
                                intent.setDataAndType(Uri.fromFile(file), "image/*");
                                startActivity(intent);
                            }
                        }else {
                            File file = new File(videoRootPath + File.separatorChar + resource.getName());
                            if(file.exists()){
                                intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                                startActivity(intent);
                            }
                        }
                    }
                }
            }
        };
        infoButton.setOnTouchListener(infoButtonListener);
    }

    public Marker buildMarker(Position pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLng());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(position));
        marker.setTag("PositionMarker");
        marker.setTitle(pe.getName());
        marker.setAlpha((float) 0.5);
        marker.setDraggable(false);
        // First check for movement permission then check if the marker is a boundary marker.
        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)
                && pe.getType().equalsIgnoreCase("boundary")) {
            marker.setDraggable(true);
            googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @SuppressWarnings("unchecked")
                @Override
                public void onMarkerDragEnd(Marker marker) {
                    zoomCameraToPosition(marker);

                    Position updatedPosition = positionMarkers.get(marker);
                    updatedPosition.setLat(marker.getPosition().latitude);
                    updatedPosition.setLng(marker.getPosition().longitude);
                    updatedPosition.setCreatedOn(System.currentTimeMillis() + "");

                    UpdatePositionTask updateTask = new UpdatePositionTask(getApplicationContext(), new TaskFinishedListener() {
                        @Override
                        public void onTaskFinished(String response) {
                            PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                            ae.setPositions(pdh.getPositionsForArea(ae));
                            ac.deriveCenter(ae);
                            polygon.remove();
                            plotPolygonUsingPositions();
                        }
                    });
                    updateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, updatedPosition);
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }
            });

        }
        return marker;
    }

    private void zoomCameraToPosition(Marker marker) {
        AreaMeasure measure = ae.getMeasure();
        float zoomLevel = 21f;
        double decimals = measure.getDecimals();
        if(decimals > 20 && decimals < 100) {
            zoomLevel = 20f;
        }else if(decimals > 100 && decimals < 300){
            zoomLevel = 19f;
        }else if(decimals > 300 && decimals < 700){
            zoomLevel = 18f;
        }else if(decimals > 700 && decimals < 1300){
            zoomLevel = 17f;
        }else if(decimals > 1300 && decimals < 2200){
            zoomLevel = 16f;
        }else if(decimals > 2200){
            zoomLevel = 14f;
        }
        LatLng position = marker.getPosition();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, zoomLevel);
        googleMap.animateCamera(cameraUpdate);
        googleMap.moveCamera(cameraUpdate);
    }

    public static int getPixelsFromDp(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        googleMap.clear();
        googleMap = null;

        finish();
        Intent positionMarkerIntent = new Intent(this, AreaDetailsActivity.class);
        startActivity(positionMarkerIntent);
    }

    private class MapSnapshotTaker implements SnapshotReadyCallback {

        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            try {
                File imageStorageDir = ac.getAreaLocalImageRoot(ae.getId());
                String dirPath = imageStorageDir.getAbsolutePath();

                String screenshotFileName = "plot_screenshot.png";
                String screenShotFilePath = dirPath + File.separatorChar + screenshotFileName;
                File screenShotFile = new File(screenShotFilePath);
                if (screenShotFile.exists()) {
                    screenShotFile.delete();
                }
                screenShotFile.createNewFile();

                View rootView = mapFragment.getView();
                rootView.setDrawingCacheEnabled(true);
                Bitmap backBitmap = rootView.getDrawingCache();
                Bitmap bmOverlay = Bitmap.createBitmap(
                        backBitmap.getWidth(), backBitmap.getHeight(),
                        backBitmap.getConfig());
                Canvas canvas = new Canvas(bmOverlay);
                canvas.drawBitmap(snapshot, new Matrix(), null);
                canvas.drawBitmap(backBitmap, 0, 0, null);

                FileOutputStream fos = new FileOutputStream(screenShotFile);
                bmOverlay.compress(CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();

                backBitmap.recycle();
                bmOverlay.recycle();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
