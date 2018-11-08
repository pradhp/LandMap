package com.pearnode.app.placero.position;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.pearnode.app.placero.AreaDetailsActivity;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.PermissionManager;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.util.ColorProvider;
import com.pearnode.common.TaskFinishedListener;

/**
 * Created by USER on 10/16/2017.
 */
public class PositionListAdaptor extends ArrayAdapter<Position> {

    private final ArrayList<Position> items;
    private final Context context;
    private PositionDatabaseHandler pdh;

    public PositionListAdaptor(Context context, int textViewResourceId, ArrayList<Position> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        pdh = new PositionDatabaseHandler(context);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.position_element_row, null);
        }

        final Position pe = items.get(position);
        String posType = pe.getType();

        TextView nameText = (TextView) v.findViewById(R.id.pos_name);
        nameText.setText(StringUtils.capitalize(pe.getName()) + ", " + StringUtils.capitalize(posType));

        TextView descText = (TextView) v.findViewById(R.id.pos_desc);
        descText.setText(StringUtils.capitalize(pe.getDescription()));

        if(pe.getDirty() == 1){
            v.setBackgroundColor(ColorProvider.DEFAULT_DIRTY_ITEM_COLOR);
        }

        final AreaContext areaContext = AreaContext.INSTANCE;
        final Area area = areaContext.getArea();

        ImageView posImgView = v.findViewById(id.position_default_img);
        posImgView.setImageResource(R.drawable.position);

        // Area Positions
        DecimalFormat locFormat = new DecimalFormat("##.####");
        TextView latLongText = (TextView) v.findViewById(R.id.pos_latlng);
        latLongText.setText("Lat: " + locFormat.format(pe.getLat()) + ", "
                + "Lng: " + locFormat.format(pe.getLng()));

        ImageView editButton = (ImageView) v.findViewById(R.id.edit_row);
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionManager.INSTANCE.hasAccess(PermissionConstants.MARK_POSITION)) {
                    ((AreaDetailsActivity)context).showPositionEdit(pe);
                }
            }
        });

        ImageView deleteButton = (ImageView) v.findViewById(R.id.del_row);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PermissionManager.INSTANCE.hasAccess(PermissionConstants.UPDATE_AREA)){
                    RemovePositionTask removeTask = new RemovePositionTask(getContext(), new TaskFinishedListener() {
                        @Override
                        public void onTaskFinished(String response) {
                            items.remove(position);
                            area.getPositions().remove(pe);
                            notifyDataSetChanged();
                        }
                    });
                    removeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pe);
                }
            }
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int siblingCount = parent.getChildCount();
                for (int i = 0; i < siblingCount; i++) {
                    View child = parent.getChildAt(i);
                    child.setBackgroundResource(R.drawable.rounded_pos_list_view);
                }
                v.setBackgroundResource(R.drawable.rounded_pos_list_view_sel);
                User user = UserContext.getInstance().getUser();
                user.getSelections().setPosition(pe);
                ((AreaDetailsActivity) context).findViewById(R.id.action_navigate_area)
                        .setBackgroundResource(R.drawable.rounded_corner);
                return true;
            }
        });

        return v;
    }
}
