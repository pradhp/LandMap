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
import java.util.UUID;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.GenericActivityExceptionHandler;
import com.pearnode.app.placero.custom.LocationPositionReceiver;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.provider.GPSLocationProvider;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.util.FileUtil;

/**
 * Created by USER on 11/1/2017.
 */
public class AreaCameraVideoActivity extends Activity implements LocationPositionReceiver {

    // Camera activity request codes
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri; // file url to store image/video_map
    private final Resource videoResource = new Resource();

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

                videoResource.setName(videoFile.getName());
                videoResource.setPath(videoFile.getAbsolutePath());
                videoResource.setType("file");
                videoResource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                videoResource.setSize(videoFile.length() + "");
                videoResource.setUniqueId(UUID.randomUUID().toString());
                videoResource.setAreaId(ae.getUniqueId());
                videoResource.setMimeType(FileUtil.getMimeType(videoFile));
                videoResource.setContentType("Video");
                videoResource.setContainerId(areaContext.getVideosRootDriveResource().getResourceId());
                videoResource.setCreatedOnMillis(System.currentTimeMillis() + "");
                videoResource.setDirty(1);
                videoResource.setDirtyAction("upload");

                ae.getResources().add(videoResource);
                areaContext.addResourceToQueue(videoResource);

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
        File localRoot = AreaContext.INSTANCE.getAreaLocalVideoRoot(area.getUniqueId());
        return new File(localRoot + File.separator + "VID_" + timeStamp + ".mp4");
    }

    @Override
    public void receivedLocationPostion(Position pe) {
        pe.setType("Media");
        pe.setDirty(1);
        pe.setDirtyAction("insert");
        videoResource.setPosition(pe);
    }

    @Override
    public void locationFixTimedOut() {

    }

    @Override
    public void providerDisabled() {

    }
}
