package com.pearnode.app.placero.area.res.disp;

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

import java.io.File;
import java.util.ArrayList;

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.RemoveDriveResourcesActivity;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.custom.ThumbnailCreator;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.util.FileUtil;

import static android.widget.ImageView.ScaleType.FIT_XY;

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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(this.context);
            view.setScaleType(FIT_XY);
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
        final Area area = AreaContext.INSTANCE.getAreaElement();
        if (thumbFile.exists()) {
            bMap = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
        }else {
            if(videoFile.exists()){
                ThumbnailCreator creator = new ThumbnailCreator(context);
                creator.createVideoThumbnail(videoFile, area.getUniqueId());
                bMap = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
            }else {
                bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
            }
        }
        view.setImageBitmap(bMap);

        final View referredView = view;

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(videoFile), "video/mp4");
                referredView.getContext().startActivity(intent);
            }
        });

        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int siblingCount = parent.getChildCount();
                for (int i = 0; i < siblingCount; i++) {
                    View child = parent.getChildAt(i);
                    child.setPadding(0,0,0,0);
                }
                referredView.setPadding(20,20,20,20);
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
                        Resource resource = ddh.getDriveResourceByResourceId(resourceId);

                        area.getMediaResources().remove(resource);
                        ddh.deleteResourceLocally(resource);
                        ddh.deleteResourceFromServer(resource);

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
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Video shared using [Placero LMS] for place - " + area.getName());
                        intent.putExtra(Intent.EXTRA_TEXT, "Hi, \nCheck out video_map for " + area.getName());
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