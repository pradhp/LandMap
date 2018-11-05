package com.pearnode.app.placero.area.res.disp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.AreaDashboardActivity;
import com.pearnode.app.placero.AreaDetailsActivity;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.model.AreaMeasure;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.util.AreaPopulationUtil;

/**
 * Created by USER on 10/20/2017.
 */
public class AreaListAdaptor extends ArrayAdapter {

    private ArrayList<Area> items;
    private final ArrayList<Area> fixedItems = new ArrayList<>();
    private final Context context;

    public AreaListAdaptor(Context context, int textViewResourceId, ArrayList<Area> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;
        fixedItems.addAll(items);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = vi.inflate(R.layout.area_element_row, null);
        }

        final AreaContext areaContext = AreaContext.INSTANCE;
        areaContext.setDisplayBMap(null);
        final Area area = items.get(position);

        AreaPopulationUtil.INSTANCE.populateAreaElement(itemView, area);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int siblingCount = parent.getChildCount();
                for (int i = 0; i < siblingCount; i++) {
                    View child = parent.getChildAt(i);
                    child.setBackgroundResource(R.drawable.rounded_area_list_view);
                }
                v.setBackgroundResource(R.drawable.rounded_area_list_view_sel);
                User user = UserContext.getInstance().getUser();
                user.getSelections().setArea(area);

                final Activity activity = (Activity) context;
                final View reportView = activity.findViewById(R.id.action_generate_report);
                reportView.setBackgroundResource(R.drawable.rounded_corner);

                return true;
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AreaDashboardActivity activity = (AreaDashboardActivity) context;

                AreaContext.INSTANCE.setArea(area, activity);
                User user = UserContext.getInstance().getUser();
                user.getSelections().setArea(area);

                Intent intent = new Intent(activity, AreaDetailsActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }

        });

        return itemView;
    }

    public int getCount() {
        return this.items.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                items = (ArrayList<Area>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                List<Area> filteredResults = getFilteredResults(constraint);
                Filter.FilterResults results = new Filter.FilterResults();
                results.values = filteredResults;
                return results;
            }

            private List<Area> getFilteredResults(CharSequence constraint) {
                List<Area> results = new ArrayList<>();
                for (int i = 0; i < fixedItems.size(); i++) {
                    Area area = fixedItems.get(i);
                    String areaName = area.getName().toLowerCase();
                    String description = area.getDescription().toLowerCase();
                    String address = area.getAddress().getDisplaybleAddress();
                    String cons = constraint.toString().toLowerCase();
                    if (areaName.contains(cons) || description.contains(constraint) || address.contains(constraint)) {
                        results.add(area);
                    }
                }
                return results;
            }
        };
    }

    public Filter resetFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                items = (ArrayList<Area>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                Filter.FilterResults results = new Filter.FilterResults();
                results.values = fixedItems;
                return results;
            }
        };
    }

    public Filter getFilterChain(final List<String> filterables, final List<String> executables) {
        return new Filter() {
            ArrayList<Area> filteredItems = fixedItems;

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                items = (ArrayList<Area>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                String constraintStr = "";
                Filter.FilterResults results = new Filter.FilterResults();
                if(constraint == null || constraint.toString().trim().equals("")){
                    results.values = fixedItems;
                }else {
                    filteredItems = (ArrayList<Area>) filterByConstraint(fixedItems, constraintStr);
                }
                for (String filterable: filterables) {
                    filteredItems = (ArrayList<Area>) filterByFilterable(filteredItems, filterable);
                }
                for (String executable: executables) {
                    filteredItems = (ArrayList<Area>) filterByExecutable(filteredItems, executable);
                }
                results.values = filteredItems;
                return results;
            }

            private List<Area> filterByFilterable(ArrayList<Area> filterableItems, String filterable) {
                if(filterableItems.size() == 0){
                    return filterableItems;
                }
                List<Area> results = new ArrayList<>();
                for (int i = 0; i < filterableItems.size(); i++) {
                    Area area = filterableItems.get(i);
                    String address = area.getAddress().getDisplaybleAddress().toLowerCase();
                    String lowerFilterable = filterable.toString().toLowerCase();
                    if (address.contains(lowerFilterable)) {
                        results.add(area);
                    }
                }
                return results;
            }

            private List<Area> filterByExecutable(ArrayList<Area> executableItems, String executable) {
                if(executableItems.size() == 0){
                    return executableItems;
                }
                List<Area> results = new ArrayList<>();
                for (int i = 0; i < executableItems.size(); i++) {
                    Area area = executableItems.get(i);
                    AreaMeasure measure = area.getMeasure();

                    String[] splitExec = executable.split(" ");
                    double instanceValue = measure.getValueByField(splitExec[0]);
                    String condition = splitExec[1];
                    String conditionValueStr = splitExec[2];
                    double conditionValue = new Double(conditionValueStr);

                    if(condition.equalsIgnoreCase("greater_than")){
                        if(instanceValue > conditionValue){
                            results.add(area);
                        }
                    }else if(condition.equalsIgnoreCase("less_than")){
                        if(instanceValue < conditionValue){
                            results.add(area);
                        }
                    }else if(condition.equalsIgnoreCase("equals")){
                        if(instanceValue == conditionValue){
                            results.add(area);
                        }
                    }else if(condition.equalsIgnoreCase("less_than_equals")){
                        if(instanceValue <= conditionValue){
                            results.add(area);
                        }
                    }else if(condition.equalsIgnoreCase("greater_than_equals")){
                        if(instanceValue >= conditionValue){
                            results.add(area);
                        }
                    }

                }
                return results;
            }

            private List<Area> filterByConstraint(ArrayList<Area> filterableItems, String constraint) {
                List<Area> results = new ArrayList<>();
                for (int i = 0; i < filterableItems.size(); i++) {
                    Area area = filterableItems.get(i);
                    String areaName = area.getName().toLowerCase();
                    String description = area.getDescription().toLowerCase();
                    String address = area.getAddress().getDisplaybleAddress();
                    String cons = constraint.toString().toLowerCase();
                    if (areaName.contains(cons) || description.contains(constraint) || address.contains(constraint)) {
                        results.add(area);
                    }
                }
                return results;
            }
        };
    }

    public ArrayList<Area> getItems() {
        return this.items;
    }

}
