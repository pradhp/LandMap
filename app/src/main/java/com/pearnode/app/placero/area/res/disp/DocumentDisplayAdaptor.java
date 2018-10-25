package com.pearnode.app.placero.area.res.disp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.media.model.Media;

final class DocumentDisplayAdaptor extends ArrayAdapter {

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

    public DocumentDisplayAdaptor(Context context, int layoutResourceId, List<Media> dataSet) {
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
                    intent.setDataAndType(Uri.parse(media.getRfPath()), "application/pdf");
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
}