package lm.pkp.com.landmap.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.RemoveDriveResourcesActivity;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.util.FileUtil;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class AreaVideoDisplayAdaptor extends BaseAdapter {

    private final Context context;
    private final Fragment fragment;
    private final int tabPosition;

    final ArrayList<VideoDisplayElement> dataSet = VideoDataHolder.INSTANCE.getData();

    public AreaVideoDisplayAdaptor(Context context, Fragment fragment, int tabPosition) {
        this.context = context;
        this.fragment = fragment;
        this.tabPosition = tabPosition;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(this.context);
            view.setScaleType(CENTER_CROP);
        } else {
            return view;
        }

        Drawable drawable = view.getDrawable();
        if(drawable == null){
            drawable = view.getBackground();
        }
        if(drawable != null){
            Bitmap previousBitmap = ((BitmapDrawable) drawable).getBitmap();
            if(previousBitmap != null){
                previousBitmap.recycle();
            }
        }

        final File thumbFile = dataSet.get(position).getThumbnailFile();
        final File videoFile = dataSet.get(position).getVideoFile();

        Bitmap bMap = null;
        if (thumbFile.exists()) {
            bMap = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
        }else {
            bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
        }
        view.setImageBitmap(bMap);

        final View referredView = view;

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(videoFile), "video/*");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ImageView clickedImage = (ImageView) referredView;
                clickedImage.setBackgroundResource(R.drawable.image_border);

                fragment.getView().findViewById(id.res_action_layout).setVisibility(View.VISIBLE);

                final VideoDisplayElement videoDisplayElement = dataSet.get(position);
                final String resourceId = videoDisplayElement.getResourceId();
                FloatingActionButton deleteButton = (FloatingActionButton) fragment.getView().findViewById(id.res_delete);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(referredView.getContext(), RemoveDriveResourcesActivity.class);
                        intent.putExtra("resource_ids", resourceId);
                        intent.putExtra("tab_position", tabPosition);

                        DriveDBHelper ddh = new DriveDBHelper(fragment.getContext());
                        DriveResource driveResource = ddh.getDriveResourceByResourceId(resourceId);

                        AreaContext.INSTANCE.getAreaElement().getMediaResources().remove(driveResource);
                        ddh.deleteResourceLocally(driveResource);
                        ddh.deleteResourceFromServer(driveResource);

                        dataSet.remove(videoDisplayElement);
                        notifyDataSetChanged();
                        referredView.getContext().startActivity(intent);
                    }
                });

                FloatingActionButton mailButton = (FloatingActionButton) fragment.getView().findViewById(id.res_mail);
                mailButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File videoFile = videoDisplayElement.getVideoFile();

                        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Video shared using [Placero LMS] for place - " + areaElement.getName());
                        intent.putExtra(Intent.EXTRA_TEXT, "Hi, \nCheck out video for " + areaElement.getName());
                        intent.setType(FileUtil.getMimeType(videoFile));
                        Uri uri = Uri.fromFile(videoFile);
                        intent.putExtra(Intent.EXTRA_STREAM, uri);

                        referredView.getContext().startActivity(Intent.createChooser(intent, "Send email..."));
                    }
                });

                return false;
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return this.dataSet.size();
    }

    @Override
    public String getItem(int position) {
        return this.dataSet.get(position).getAbsPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}