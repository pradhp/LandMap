package com.pearnode.app.placero;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.custom.LocationPositionReceiver;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.provider.GPSLocationProvider;

/**
 * Created by USER on 11/1/2017.
 */
public class AreaVideoCaptureActivity extends Activity implements LocationPositionReceiver {

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int TAG_CODE_PERMISSION_LOCATION = 7;

    private Uri fileUri; // file url to store image/video_map
    private Media media = new Media();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        askPermissions();
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                TAG_CODE_PERMISSION_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        recordVideo();
        switch (requestCode) {
            case TAG_CODE_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPositioning();
                } else {
                    // Permission denied cannot get location.
                }
                return;
            }
        }
    }

    private void startPositioning() {
        new GPSLocationProvider(this, this, 60).getLocation();
    }

    /**
     * Launching camera app to record video_map
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video_map quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video_map capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File videoFile = new File(fileUri.getPath());
                AreaContext areaContext = AreaContext.INSTANCE;
                Area ae = areaContext.getAreaElement();

                media.setName(videoFile.getName());
                media.setRfPath(videoFile.getAbsolutePath());
                media.setType("video");
                media.setDirty(1);
                media.setDirtyAction("upload");
                media.setCreatedOn(System.currentTimeMillis());

                ae.getVideos().add(media);
                areaContext.addMediaToQueue(media);

                Intent i = new Intent(this, AreaAddResourcesActivity.class);
                startActivity(i);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Cancelled case
                finish();
            } else {
                // Failed case
                finish();
            }
        }
    }

    /**
     * Creating file uri to store image/video_map
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * returning image / video_map
     */
    private static File getOutputMediaFile() {
        Area area = AreaContext.INSTANCE.getAreaElement();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File localRoot = AreaContext.INSTANCE.getAreaLocalVideoRoot(area.getId());
        return new File(localRoot + File.separator + "VID_" + timeStamp + ".mp4");
    }

    @Override
    public void receivedLocationPostion(Position pe) {
        pe.setType("Media");
        pe.setDirty(1);
        pe.setDirtyAction("insert");
        media.setLat(pe.getLat() + "");
        media.setLng(pe.getLng() + "");
    }

    @Override
    public void locationFixTimedOut() {

    }

    @Override
    public void providerDisabled() {

    }
}
