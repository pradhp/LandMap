package com.pearnode.app.placero;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

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

    private Uri fileUri; // file url to store image/video_map
    private Media media = new Media();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);
        startPositioning();
        recordVideo();
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
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File videoFile = new File(fileUri.getPath());
                AreaContext areaContext = AreaContext.INSTANCE;
                Area ae = areaContext.getArea();

                media.setName(videoFile.getName());
                media.setRfPath(videoFile.getAbsolutePath());
                media.setType("video");
                media.setDirty(1);
                media.setDirtyAction("upload");
                media.setCreatedOn(System.currentTimeMillis());

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

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile() {
        Area area = AreaContext.INSTANCE.getArea();
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
