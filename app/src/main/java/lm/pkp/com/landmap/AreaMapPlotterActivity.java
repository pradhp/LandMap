package lm.pkp.com.landmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import java.util.UUID;

import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.model.AreaMeasure;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.custom.MapWrapperLayout;
import lm.pkp.com.landmap.custom.OnInfoWindowElemTouchListener;
import lm.pkp.com.landmap.custom.ThumbnailCreator;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.ColorProvider;
import lm.pkp.com.landmap.util.FileUtil;

public class AreaMapPlotterActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private LinkedHashMap<Marker, PositionElement> positionMarkers = new LinkedHashMap<>();
    private LinkedHashMap<Marker, DriveResource> resourceMarkers = new LinkedHashMap<>();
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
    private final AreaElement ae = ac.getAreaElement();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_plotter);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(id.googleMap);
        mapFragment.getMapAsync(this);
    }

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
        List<PositionElement> positionElements = ae.getPositions();
        int noOfPositions = positionElements.size();

        Set<Marker> markers = positionMarkers.keySet();
        for (Marker m : markers) {
            m.remove();
        }
        positionMarkers.clear();
        if (centerMarker != null) {
            centerMarker.remove();
        }
        for (int i = 0; i < noOfPositions; i++) {
            PositionElement pe = positionElements.get(i);
            String positionType = pe.getType();
            if(!positionType.equalsIgnoreCase("media")){
                positionMarkers.put(buildMarker(pe), pe);
            }
        }

        PositionElement centerPosition = ae.getCenterPosition();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(centerPosition.getLat(), centerPosition.getLon()));
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
            PositionElement positionElement = positionMarkers.get(m);
            if(positionElement.getType().equalsIgnoreCase("boundary")){
                polyOptions.add(m.getPosition());
            }
        }
        polygon = googleMap.addPolygon(polyOptions);

        double polygonAreaSqMt = SphericalUtil.computeArea(polygon.getPoints());
        double polygonAreaSqFt = polygonAreaSqMt * 10.7639;

        AreaMeasure areaMeasure = new AreaMeasure(polygonAreaSqFt);
        ae.setMeasure(areaMeasure);

        if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)) {
            AreaDBHelper adh = new AreaDBHelper(getApplicationContext());
            adh.updateAreaLocally(ae);
            adh.updateAreaOnServer(ae);
            adh.insertAreaAddressTagsLocally(ae);
            adh.insertAreaAddressTagsOnServer(ae);
        }
        polygonMarkers.put(polygon, centerMarker);
    }

    private void plotMediaPoints() {
        List<DriveResource> driveResources = ae.getMediaResources();
        BitmapDescriptor videoBMap = BitmapDescriptorFactory.fromResource(drawable.video_map);
        BitmapDescriptor pictureBMap = BitmapDescriptorFactory.fromResource(drawable.camera_map);
        for (int i = 0; i < driveResources.size(); i++) {
            DriveResource resource = driveResources.get(i);
            if (resource.getType().equals("file")) {
                PositionElement resourcePosition = resource.getPosition();
                if(resourcePosition != null){
                    if (resource.getName().equalsIgnoreCase("plot_screenshot.png")) {
                        continue;
                    }
                    LatLng position = new LatLng(resourcePosition.getLat(), resourcePosition.getLon());

                    MarkerOptions markerOptions = new MarkerOptions();
                    if(resource.getContentType().equalsIgnoreCase("Video")){
                        markerOptions.icon(videoBMap);
                    }else {
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

                    resourceMarkers.put(marker, resource);
                }
            }
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
                PositionElement position = positionMarkers.get(marker);
                DriveResource resource = resourceMarkers.get(marker);
                String markerTag = (String) marker.getTag();

                if (markerTag.equalsIgnoreCase("PositionMarker")) {
                    infoImage.setImageResource(drawable.position);
                    infoTitle.setText(position.getName());
                    CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(new Long(position.getCreatedOnMillis()),
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
                    if (resource.getContentType().equalsIgnoreCase("Video")) {
                        thumbRootPath = ac.getAreaLocalVideoThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
                    } else {
                        thumbRootPath = ac.getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();
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
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(getApplicationContext(), 35));
        mapWrapperLayout.setPolygonMarkers(polygonMarkers);

        infoWindow = (ViewGroup) getLayoutInflater().inflate(layout.info_window, null);
        infoTitle = (TextView) infoWindow.findViewById(id.title);
        infoSnippet = (TextView) infoWindow.findViewById(id.snippet);
        infoImage = (ImageView) infoWindow.findViewById(id.info_element_img);
        infoButton = (Button) infoWindow.findViewById(id.map_info_action);

        infoButtonListener = new OnInfoWindowElemTouchListener(infoButton) {

            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                File areaLocalImageRoot = ac.getAreaLocalImageRoot(ae.getUniqueId());
                File areaLocalVideoRoot = ac.getAreaLocalVideoRoot(ae.getUniqueId());
                String imageRootPath = areaLocalImageRoot.getAbsolutePath();
                String videoRootPath = areaLocalVideoRoot.getAbsolutePath();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);

                PositionElement position = positionMarkers.get(marker);
                if (position != null) {
                    if(PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)){
                        PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                        PositionElement markedPosition = positionMarkers.get(marker);
                        pdh.deletePositionGlobally(markedPosition);
                        ae.getPositions().remove(markedPosition);
                        ac.reCenter(ae);

                        polygon.remove();
                        plotPolygonUsingPositions();
                    }
                }else {
                    DriveResource resource = resourceMarkers.get(marker);
                    if(resource != null){
                        String contentType = resource.getContentType();
                        if(contentType.equalsIgnoreCase("Image")){
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

    public Marker buildMarker(PositionElement pe) {
        LatLng position = new LatLng(pe.getLat(), pe.getLon());
        Marker marker = googleMap.addMarker(new MarkerOptions().position(position));
        marker.setTag("PositionMarker");
        marker.setTitle(pe.getUniqueId());
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

                    PositionElement newPosition = positionMarkers.get(marker);
                    newPosition.setLat(marker.getPosition().latitude);
                    newPosition.setLon(marker.getPosition().longitude);
                    newPosition.setCreatedOnMillis(System.currentTimeMillis() + "");

                    PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
                    pdh.updatePositionLocally(newPosition);
                    pdh.updatePositionToServer(newPosition);

                    ae.setPositions(pdh.getPositionsForArea(ae));
                    ac.reCenter(ae);

                    polygon.remove();
                    plotPolygonUsingPositions();
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
                File imageStorageDir = ac.getAreaLocalImageRoot(ae.getUniqueId());
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

                List<DriveResource> driveResources = ae.getMediaResources();
                DriveDBHelper ddh = new DriveDBHelper(getApplicationContext());
                for (int i = 0; i < driveResources.size(); i++) {
                    DriveResource resource = driveResources.get(i);
                    if (resource.getType().equalsIgnoreCase("file")) {
                        String resourceName = resource.getName();
                        if (resourceName.equalsIgnoreCase(screenshotFileName)) {
                            ddh.deleteResourceLocally(resource);
                            ddh.deleteResourceFromServer(resource);
                            driveResources.remove(resource);
                            break;
                        }
                    }
                }


                DriveResource imagesRootResource = ac.getImagesRootDriveResource();

                DriveResource resource = new DriveResource();
                resource.setUniqueId(UUID.randomUUID().toString());
                resource.setContainerId(imagesRootResource.getResourceId());
                resource.setContentType("Image");
                resource.setMimeType(FileUtil.getMimeType(screenShotFile));
                resource.setType("file");
                resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                resource.setAreaId(ae.getUniqueId());
                resource.setName(screenshotFileName);
                resource.setSize(screenShotFile.length() + "");

                PositionElement position = new PositionElement();
                position.setLat(ae.getCenterPosition().getLat());
                position.setLon(ae.getCenterPosition().getLon());
                position.setName("Position_" + ae.getPositions().size());
                position.setUniqueId(UUID.randomUUID().toString());
                resource.setPosition(position);

                resource.setPath(screenShotFilePath);
                resource.setCreatedOnMillis(System.currentTimeMillis() + "");

                ddh.insertResourceLocally(resource);
                ddh.insertResourceToServer(resource);
                driveResources.add(resource);

                ThumbnailCreator creator = new ThumbnailCreator(getApplicationContext());
                creator.createImageThumbnail(screenShotFile, ae.getUniqueId());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
