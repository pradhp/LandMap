package com.pearnode.app.placero.area.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.AreaDashboardActivity;
import com.pearnode.app.placero.CreateAreaFoldersActivity;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.AreaDashboardDisplayMetaStore;
import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.res.disp.AreaItemAdaptor;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.FragmentFilterHandler;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.PermissionElement;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.tags.TagElement;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.app.placero.user.UserPersistableSelections;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardOwnedFragment extends Fragment
        implements FragmentHandler, FragmentFilterHandler{

    private Activity mActivity = null;
    private View mView = null;
    private boolean offline = false;
    private AreaItemAdaptor viewAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owned_areas, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mActivity = getActivity();
        if(getUserVisibleHint()){
            loadFragment();
        }
        offline = ((AreaDashboardActivity)mActivity).isOffline();
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && (mView != null) && (mActivity != null)) {
            AreaDashboardDisplayMetaStore.INSTANCE.setActiveTab(AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ);
            loadFragment();
        }
    }

    private void loadFragment() {
        AreaContext.INSTANCE.clearContext();
        mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

        ArrayList<AreaElement> areas = new AreaDBHelper(mActivity).getAreas("self");
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);

        ImageView createAreaView = (ImageView) mView.findViewById(id.owned_area_empty_layout_action);
        createAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                AreaDBHelper adh = new AreaDBHelper(mActivity);
                AreaElement areaElement = adh.insertAreaLocally(offline);

                PermissionElement pe = new PermissionElement();
                pe.setUserId(UserContext.getInstance().getUserElement().getEmail());
                pe.setAreaId(areaElement.getUniqueId());
                pe.setFunctionCode(PermissionConstants.FULL_CONTROL);

                PermissionsDBHelper pdh = new PermissionsDBHelper(mActivity);
                pdh.insertPermissionLocally(pe);
                areaElement.getUserPermissions().put(PermissionConstants.FULL_CONTROL, pe);

                // Resetting the context for new Area
                AreaContext.INSTANCE.setAreaElement(areaElement, mActivity);
                adh = new AreaDBHelper(mActivity, new DataInsertServerCallback(areaElement));
                adh.insertAreaToServer(areaElement);
            }
        });

        if (areas.size() > 0) {
            mView.findViewById(R.id.owned_area_empty_layout).setVisibility(View.GONE);
            areaListView.setVisibility(View.VISIBLE);

            viewAdapter = new AreaItemAdaptor(mActivity, layout.area_element_row, areas);
            areaListView.setAdapter(viewAdapter);
            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        } else {
            areaListView.setVisibility(View.GONE);
            mView.findViewById(id.owned_area_empty_layout).setVisibility(View.VISIBLE);
        }

        EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
        if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ){
            inputSearch.addTextChangedListener(new UserInputWatcher());
        }

        Button seachClearButton = (Button) mActivity.findViewById(id.dashboard_search_clear);
        if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ){
                seachClearButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
                        inputSearch.setText("");
                    }
                });
        }
        mView.findViewById(id.splash_panel).setVisibility(View.GONE);

        final ImageView filterUTView = (ImageView) mActivity.findViewById(id.action_filter_ut);
        UserElement userElement = UserContext.getInstance().getUserElement();
        UserPersistableSelections userPersistableSelections = userElement.getSelections();
        if(userPersistableSelections.isFilter()){
            filterUTView.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
            List<TagElement> tags = userPersistableSelections.getTags();
            List<String> filterables = new ArrayList<>();
            List<String> executables = new ArrayList<>();
            for(TagElement tag: tags){
                if(tag.getType().equals("filterable")){
                    filterables.add(tag.getName());
                }else {
                    executables.add(tag.getName());
                }
            }
            doFilter(filterables, executables);
        }else {
            filterUTView.setBackground(null);
        }
    }

    @Override
    public String getFragmentTitle() {
        return "Owned";
    }

    @Override
    public void doFilter(List<String> filterables, List<String> executables) {
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        AreaItemAdaptor adapter = (AreaItemAdaptor) areaListView.getAdapter();
        if(adapter.getCount() == 0){
            return;
        }
        EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
        Editable inputSearchText = inputSearch.getText();
        adapter.getFilterChain(filterables, executables).filter(inputSearchText.toString());
    }

    @Override
    public void resetFilter() {
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        AreaItemAdaptor adapter = (AreaItemAdaptor) areaListView.getAdapter();
        if(adapter == null){
            return;
        }
        adapter.resetFilter().filter(null);
    }

    private class UserInputWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ){
                if(editable.toString().equalsIgnoreCase("")){
                    return;
                }else {
                    mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

                    ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
                    ArrayAdapter<AreaElement> adapter = (ArrayAdapter<AreaElement>) areaListView.getAdapter();
                    final ImageView filterUTView = (ImageView) mActivity.findViewById(id.action_filter_ut);
                    UserElement userElement = UserContext.getInstance().getUserElement();
                    if(filterUTView.getBackground() != null){
                        List<TagElement> tags = userElement.getSelections().getTags();
                        List<String> filterables = new ArrayList<>();
                        List<String> executables = new ArrayList<>();
                        for(TagElement tag: tags){
                            if(tag.getType().equals("filterable")){
                                filterables.add(tag.getName());
                            }else {
                                executables.add(tag.getName());
                            }
                        }
                        doFilter(filterables, executables);
                    }else {
                        adapter.getFilter().filter(editable.toString());
                    }
                    mView.findViewById(id.splash_panel).setVisibility(View.GONE);
                }
            }
        }
    }

    private class DataInsertServerCallback implements AsyncTaskCallback {

        private AreaElement areaElement;

        public DataInsertServerCallback(AreaElement areaElement){
            this.areaElement = areaElement;
        }
        @Override
        public void taskCompleted(Object result) {
            Intent intent = new Intent(mActivity, CreateAreaFoldersActivity.class);
            intent.putExtra("area_id", areaElement.getUniqueId());
            startActivity(intent);
        }
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public Object getViewAdaptor() {
        return viewAdapter;
    }
}
