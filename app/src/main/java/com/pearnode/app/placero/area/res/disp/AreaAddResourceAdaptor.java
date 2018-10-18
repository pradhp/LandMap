package com.pearnode.app.placero.area.res.disp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.drive.DriveDBHelper;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.util.ColorProvider;

/**
 * Created by USER on 10/16/2017.
 */
public class AreaAddResourceAdaptor extends ArrayAdapter<Resource> {

    private final ArrayList<Resource> items;
    private final Context context;

    public AreaAddResourceAdaptor(Context context, ArrayList<Resource> items) {
        super(context, R.layout.upload_element_row, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.upload_element_row, null);
        }

        final AreaContext areaContext = AreaContext.INSTANCE;
        final Resource resource = items.get(position);

        TextView nameText = (TextView) v.findViewById(R.id.ar_file_name);
        nameText.setText(resource.getName());

        TextView filePathText = (TextView) v.findViewById(R.id.ar_file_path);
        Position resourcePosition = resource.getPosition();
        if(resourcePosition != null){
            String message = "Position: " + resourcePosition.getLat() + ", " + resourcePosition.getLon();
            filePathText.setText(message);
        }else {
            filePathText.setText(resource.getSize() + " bytes");
        }

        v.setBackgroundColor(ColorProvider.DEFAULT_DIRTY_ITEM_COLOR);

        Button removeButton = (Button) v.findViewById(id.remove_upload_resource);
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                areaContext.getUploadedQueue().remove(resource);
                items.remove(resource);
                notifyDataSetChanged();

                DriveDBHelper ddh = new DriveDBHelper(getContext());
                ddh.deleteResourceLocally(resource);
            }
        });
        return v;
    }
}
