package com.pearnode.app.placero.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.media.model.Media;

final class PictureDisplayAdaptor extends ArrayAdapter {

    private final Context context;
    private List<Media> dataSet;
    private Fragment fragment;
    private int layoutResourceId;

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }

    public void setFragment(Fragment fragment){
        this.fragment = fragment;
    }

    public PictureDisplayAdaptor(Context context, int layoutResourceId, List dataSet) {
        super(context, R.layout.media_display_item, dataSet);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.dataSet = dataSet;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        final Media media = dataSet.get(position);
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.media_text);
            holder.imageTitle.setText(media.getName());
            holder.image = (ImageView) row.findViewById(R.id.media_thumbnail);
            row.setTag(holder);

            GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(holder.image);
            Glide.with(fragment).load(media.getTfPath()).placeholder(R.drawable.cube).centerCrop().crossFade().into(imageViewTarget);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(media.getRfPath()), "image/*");
                    fragment.getActivity().startActivity(intent);
                }
            });
        }
        return row;
    }

    @Override
    public int getCount() {
        return this.dataSet.size();
    }

    @Override
    public String getItem(int position) {
        return this.dataSet.get(position).getRfPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void showErrorMessage(View view, String message, String type) {
        final Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.getView().setBackgroundColor(Color.WHITE);

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        if (type.equalsIgnoreCase("info")) {
            textView.setTextColor(Color.GREEN);
        } else if (type.equalsIgnoreCase("error")) {
            textView.setTextColor(Color.RED);
        } else {
            textView.setTextColor(Color.DKGRAY);
        }
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        textView.setTextSize(15);
        textView.setMaxLines(3);

        snackbar.setAction("Dismiss", new OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

}