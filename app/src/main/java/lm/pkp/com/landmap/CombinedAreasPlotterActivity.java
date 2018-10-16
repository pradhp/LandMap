package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.model.AreaMeasure;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.MapWrapperLayout;
import lm.pkp.com.landmap.custom.OnInfoWindowElemTouchListener;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.util.ColorProvider;

public class CombinedAreasPlotterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private ImageView infoImage;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private SupportMapFragment mapFragment;
    private List<String> areaIds = new ArrayList<>();

    private Map<String, LinkedHashMap<Marker, PositionElement>> areaBoundaryMarkers = new HashMap<>();
    private Map<String, LinkedHashMap<Marker, DriveResource>> areaMediaMarkers = new HashMap<>();
    private Map<String, LinkedHashMap<Marker, PositionElement>> areaMediaPositions = new HashMap<>();
    private Map<Marker, AreaElement> markerAreaMap = new LinkedHashMap<>();

    private Map<String, Marker> areaCenterMarkers = new HashMap<>();
    private Map<String, PositionElement> areaCenterPositions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(layout.activity_area_plotter);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(id.googleMap);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Object[] plotAreaIds = (Object[]) extras.get("area_ids");
            for (int i = 0; i < plotAreaIds.length; i++) {
                areaIds.add(plotAreaIds[i].toString());
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap gmap) {
        googleMap = gmap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setTrafficEnabled(true);

        UiSettings settings = googleMap.getUiSettings();
        settings.setMapToolbarEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);

        plotUsingAreas();
    }

    private void plotUsingAreas() {
        AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
        for (String areaId : areaIds) {
            AreaElement ae = adh.getAreaById(areaId);
            plotPolygonUsingPositions(ae);
            plotMediaPoints(ae);
        }
        initializeMapEventPropagation();
        initializeMapInfoWindow();
        zoomCamera();
    }

    private void plotPolygonUsingPositions(AreaElement areaElement) {
        AreaContext.INSTANCE.centerize(areaElement);

        String areaId = areaElement.getUniqueId();
        areaCenterPositions.put(areaId, areaElement.getCenterPosition());

        List<PositionElement> positionElements = areaElement.getPositions();
        int noOfPositions = positionElements.size();
        if (noOfPositions == 0) {
            return;
        }

        LinkedHashMap<Marker, PositionElement> boundaryMarkers = areaBoundaryMarkers.get(areaId);
        if (boundaryMarkers == null) {
            boundaryMarkers = new LinkedHashMap<>();
            areaBoundaryMarkers.put(areaId, boundaryMarkers);
        }

        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positionElements.get(i);
            String positionType = pe.getType();
            if (positionType.equalsIgnoreCase("boundary")) {
                Marker marker = buildMarker(pe);
                boundaryMarkers.put(marker, pe);
                markerAreaMap.put(marker, areaElement);
            }
        }

        PositionElement centerPosition = areaElement.getCenterPosition();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(centerPosition.getLat(), centerPosition.getLon()));

        Marker centerMarker = googleMap.addMarker(markerOptions);
        centerMarker.setTag("AreaCenter");
        centerMarker.setVisible(true);
        centerMarker.setTitle(areaElement.getName());
        //centerMarker.showInfoWindow();

        areaCenterMarkers.put(areaId, centerMarker);
        markerAreaMap.put(centerMarker, areaElement);

        PolygonOptions polyOptions = new PolygonOptions();
        polyOptions = polyOptions
                .strokeColor(ColorProvider.DEFAULT_POLYGON_BOUNDARY)
                .fillColor(ColorProvider.DEFAULT_POLYGON_FILL);

        Set<Marker> markerSet = boundaryMarkers.keySet();
        for (Marker m : markerSet) {
            polyOptions.add(m.getPosition());
        }

        Polygon polygon = googleMap.addPolygon(polyOptions);
        double polygonAreaSqMt = SphericalUtil.computeArea(polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        AreaMeasure areaMeasure = new AreaMeasure(polygonAreaSqFt);
        areaElement.setMeasure(areaMeasure);
    }

    private void plotMediaPoints(AreaElement areaElement) {
        String areaId = areaElement.getUniqueId();
        Marker centerMarker = areaCenterMarkers.get(areaId);

        LinkedHashMap<Marker, DriveResource> mediaMarkers = areaMediaMarkers.get(areaId);
        if (mediaMarkers == null) {
            mediaMarkers = new LinkedHashMap<>();
            areaMediaMarkers.put(areaId, mediaMarkers);
        }
        LinkedHashMap<Marker, PositionElement> mediaPositions = areaMediaPositions.get(areaId);
        if (mediaPositions == null) {
            mediaPositions = new LinkedHashMap<>();
            areaMediaPositions.put(areaId, mediaPositions);
        }

        List<DriveResource> driveResources = areaElement.getMediaResources();
        BitmapDescriptor videoBMap = BitmapDescriptorFactory.fromResource(R.drawable.video_map);
        BitmapDescriptor pictureBMap = BitmapDescriptorFactory.fromResource(R.drawable.camera_map);
        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                PositionElement resourcePosition = resource.getPosition();
                if (resourcePosition != null) {
                    if (resource.getName().equalsIgnoreCase("plot_screenshot.png")) {
                        continue;
                    }
                    LatLng position = new LatLng(resourcePosition.getLat(), resourcePosition.getLon());

                    MarkerOptions markerOptions = new MarkerOptions();
                    if (resource.getContentType().equalsIgnoreCase("Video")) {
                        markerOptions.icon(videoBMap);
                    } else {
                        markerOptions.icon(pictureBMap);
                    }
                    markerOptions.position(position);

                    Marker marker = googleMap.addMarker(markerOptions);
                    marker.setTag("MediaMarker");
                    marker.setTitle(resource.getName());
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

                    mediaMarkers.put(marker, resource);
                    mediaPositions.put(marker, resourcePosition);
                    markerAreaMap.put(marker, areaElement);
                }
            }
        }
    }

    private void initializeMapInfoWindow() {
        final AreaContext ac = AreaContext.INSTANCE;

        googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                AreaElement areaObj = markerAreaMap.get(marker);
                String areaId = areaObj.getUniqueId();

                Marker centerMarker = areaCenterMarkers.get(areaId);
                String markerTag = (String) marker.getTag();

                if (markerTag.equalsIgnoreCase("PositionMarker")) {
                    LinkedHashMap<Marker, PositionElement> boundaryMarkers = areaBoundaryMarkers.get(areaId);
                    PositionElement position = boundaryMarkers.get(marker);
                    infoImage.setImageResource(drawable.position);
                    infoTitle.setText(position.getName());
                    CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(position.getCreatedOnMillis()),
                            System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
                    DecimalFormat formatter = new DecimalFormat("##.##");
                    double distance = SphericalUtil.computeDistanceBetween(marker.getPosition(), centerMarker.getPosition());
                    infoSnippet.setText(formatter.format(distance) + " mts, " + timeSpan.toString());
                    infoButton.setText("View");
                }

                if (markerTag.equalsIgnoreCase("AreaCenter")) {
                    infoTitle.setText(marker.getTitle());
                    infoSnippet.setText(areaObj.getMeasure().toString());
                    infoImage.setImageResource(R.drawable.position);
                    infoButton.setText("Details");
                }

                if (markerTag.equalsIgnoreCase("MediaMarker")) {
                    LinkedHashMap<Marker, DriveResource> mediaMarkers = areaMediaMarkers.get(areaId);
                    DriveResource resource = mediaMarkers.get(marker);
                    String thumbRootPath = "";
                    if (resource.getContentType().equalsIgnoreCase("Video")) {
                        thumbRootPath = ac.getAreaLocalVideoThumbnailRoot(areaId).getAbsolutePath();
                    } else {
                        thumbRootPath = ac.getAreaLocalPictureThumbnailRoot(areaId).getAbsolutePath();
                    }
                    String thumbnailPath = thumbRootPath + File.separatorChar + resource.getName();
                    File thumbFile = new File(thumbnailPath);
                    if (thumbFile.exists()) {
                        Bitmap bMap = BitmapFactory.decodeFile(thumbnailPath);
                        infoImage.setImageBitmap(bMap);
                    }
                    infoTitle.setText(resource.getName());
                    CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(resource.getCreatedOnMillis()),
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
        final AreaContext ac = AreaContext.INSTANCE;

        mapWrapperLayout = (MapWrapperLayout) findViewById(id.map_relative_layout);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 35));

        infoWindow = (ViewGroup) getLayoutInflater().inflate(layout.info_window, null);
        infoTitle = (TextView) infoWindow.findViewById(id.title);
        infoSnippet = (TextView) infoWindow.findViewById(id.snippet);
        infoImage = (ImageView) infoWindow.findViewById(id.info_element_img);
        infoButton = (Button) infoWindow.findViewById(id.map_info_action);

        infoButtonListener = new OnInfoWindowElemTouchListener(infoButton) {

            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                AreaElement areaObj = markerAreaMap.get(marker);
                String areaId = areaObj.getUniqueId();

                File areaLocalImageRoot = ac.getAreaLocalImageRoot(areaId);
                File areaLocalVideoRoot = ac.getAreaLocalVideoRoot(areaId);
                String imageRootPath = areaLocalImageRoot.getAbsolutePath();
                String videoRootPath = areaLocalVideoRoot.getAbsolutePath();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                String markerTag = (String) marker.getTag();
                if (markerTag.equalsIgnoreCase("MediaMarker")) {
                    LinkedHashMap<Marker, DriveResource> mediaMarkers = areaMediaMarkers.get(areaId);
                    DriveResource resource = mediaMarkers.get(marker);
                    String contentType = resource.getContentType();
                    if (contentType.equalsIgnoreCase("Image")) {
                        File file = new File(imageRootPath + File.separatorChar + resource.getName());
                        if (file.exists()) {
                            intent.setDataAndType(Uri.fromFile(file), "image/*");
                            startActivity(intent);
                        }
                    } else {
                        File file = new File(videoRootPath + File.separatorChar + resource.getName());
                        if (file.exists()) {
                            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
                            startActivity(intent);
                        }
                    }
                }else if(markerTag.equalsIgnoreCase("AreaCenter")){
                    AreaContext.INSTANCE.setAreaElement(areaObj,getApplicationContext());
                    Intent detailsIntent = new Intent(getApplicationContext(), AreaMapPlotterActivity.class);
                    startActivity(detailsIntent);
                    finish();
                }
            }
        };
        infoButton.setOnTouchListener(infoButtonListener);
    }

    public Marker buildMarker(PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(position));
        marker.setTag("PositionMarker");
        marker.setTitle(pe.getUniqueId());
        marker.setAlpha((float) 0.1);
        marker.setDraggable(false);
        marker.setVisible(false);
        return marker;
    }

    private void zoomCamera() {
        if (areaCenterMarkers.size() > 0) {
            Marker firstCM = areaCenterMarkers.values().iterator().next();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstCM.getPosition(), 12f);
            googleMap.animateCamera(cameraUpdate);
            googleMap.moveCamera(cameraUpdate);
        }
    }

    /**
     * private void zoomCameraToPosition(Marker marker) {
     * AreaMeasure measure = ae.getMeasure();
     * float zoomLevel = 21f;
     * double decimals = measure.getDecimals();
     * if(decimals > 20 && decimals < 100) {
     * zoomLevel = 20f;
     * }else if(decimals > 100 && decimals < 300){
     * zoomLevel = 19f;
     * }else if(decimals > 300 && decimals < 700){
     * zoomLevel = 18f;
     * }else if(decimals > 700 && decimals < 1300){
     * zoomLevel = 17f;
     * }else if(decimals > 1300 && decimals < 2200){
     * zoomLevel = 16f;
     * }else if(decimals > 2200){
     * zoomLevel = 14f;
     * }
     * LatLng position = marker.getPosition();
     * CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, zoomLevel);
     * googleMap.animateCamera(cameraUpdate);
     * googleMap.moveCamera(cameraUpdate);
     * }
     */

    public static int getPixelsFromDp(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onBackPressed() {
        googleMap.clear();
        googleMap = null;

        finish();
        Intent positionMarkerIntent = new Intent(this, AreaDashboardActivity.class);
        startActivity(positionMarkerIntent);
    }

}