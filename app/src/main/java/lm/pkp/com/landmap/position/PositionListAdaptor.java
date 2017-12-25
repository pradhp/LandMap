package lm.pkp.com.landmap.position;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import lm.pkp.com.landmap.AreaDetailsActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionManager;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.util.ColorProvider;

/**
 * Created by USER on 10/16/2017.
 */
public class PositionListAdaptor extends ArrayAdapter<PositionElement> {

    private final ArrayList<PositionElement> items;
    private final Context context;
    private PositionsDBHelper pdh;
    private DriveDBHelper ddh;

    public PositionListAdaptor(Context context, int textViewResourceId, ArrayList<PositionElement> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        pdh = new PositionsDBHelper(context);
        ddh = new DriveDBHelper(context);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.position_element_row, null);
        }

        final PositionElement pe = items.get(position);
        String posType = pe.getType();

        TextView nameText = (TextView) v.findViewById(R.id.pos_name);
        nameText.setText(StringUtils.capitalize(pe.getName()) + ", " + StringUtils.capitalize(posType));

        TextView descText = (TextView) v.findViewById(R.id.pos_desc);
        descText.setText(StringUtils.capitalize(pe.getDescription()));

        if(pe.getDirty() == 1){
            v.setBackgroundColor(ColorProvider.BUFF_YELLOW_AREA_DISPLAY);
        }

        final AreaContext areaContext = AreaContext.INSTANCE;
        final AreaElement areaElement = areaContext.getAreaElement();
        final String uniqueId = areaElement.getUniqueId();

        ImageView posImgView = (ImageView) v.findViewById(id.position_default_img);
        if(posType.equalsIgnoreCase("media")){
            String rootPath = null;
            DriveResource resource = ddh.getDriveResourceByPositionId(pe.getUniqueId());
            if(resource.getContentType().equalsIgnoreCase("Image")){
                rootPath = areaContext.getAreaLocalPictureThumbnailRoot(uniqueId).getAbsolutePath();
            }else {
                rootPath = areaContext.getAreaLocalVideoThumbnailRoot(uniqueId).getAbsolutePath();
            }
            String thumbnailPath = rootPath + File.separatorChar + resource.getName();
            File thumbFile = new File(thumbnailPath);
            if (thumbFile.exists()) {
                posImgView.setImageBitmap(BitmapFactory.decodeFile(thumbnailPath));
            }else {
                posImgView.setImageResource(R.drawable.position);
            }
        }else {
            posImgView.setImageResource(R.drawable.position);
        }

        // Area Positions
        DecimalFormat locFormat = new DecimalFormat("##.####");
        TextView latLongText = (TextView) v.findViewById(R.id.pos_latlng);
        latLongText.setText("Lat: " + locFormat.format(pe.getLat()) + ", "
                + "Lng: " + locFormat.format(pe.getLon()));

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
                    items.remove(position);
                    pdh.deletePositionGlobally(pe);

                    if(pe.getType().equalsIgnoreCase("Media")){
                        DriveResource resource = ddh.getDriveResourceByPositionId(pe.getUniqueId());
                        resource.setPosition(null);
                        ddh.updateResourceLocally(resource);
                        ddh.updateResourceToServer(resource);
                        areaElement.getMediaResources().remove(resource);
                        areaElement.getMediaResources().add(resource);
                    }

                    areaElement.getPositions().remove(pe);
                    notifyDataSetChanged();
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
                UserElement userElement = UserContext.getInstance().getUserElement();
                userElement.getSelections().setPosition(pe);
                ((AreaDetailsActivity) context).findViewById(R.id.action_navigate_area)
                        .setBackgroundResource(R.drawable.rounded_corner);
                return true;
            }
        });

        return v;
    }
}
