package com.pearnode.app.placero.area.dashboard;

import android.app.Activity;
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

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.AreaDashboardDisplayMetaStore;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.db.AreaDatabaseHandler;
import com.pearnode.app.placero.area.res.disp.AreaListAdaptor;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.FragmentFilterHandler;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.sync.LocalDataRefresher;
import com.pearnode.app.placero.tags.Tag;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.User;
import com.pearnode.app.placero.user.UserPersistableSelections;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardSharedFragment extends Fragment
        implements FragmentHandler, FragmentFilterHandler {

    private Activity mActivity = null;
    private View mView = null;
    private AreaListAdaptor viewAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shared_areas, container, false);
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
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && (mView != null) && (mActivity != null)) {
            AreaDashboardDisplayMetaStore.INSTANCE.setActiveTab(AreaDashboardDisplayMetaStore.TAB_SHARED_SEQ);
            LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback());
            dataRefresher.refreshSharedAreas();
        }
    }

    private class DataReloadCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            loadFragment();
        }
    }

    private void loadFragment() {
        AreaContext.INSTANCE.setDisplayBMap(null);
        mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

        EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
        inputSearch.addTextChangedListener(new UserInputWatcher());

        Button seachClearButton = (Button) mActivity.findViewById(id.dashboard_search_clear);
        seachClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
                inputSearch.setText("");
            }
        });

        AreaDatabaseHandler adh = new AreaDatabaseHandler(mView.getContext());
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        ArrayList<Area> sharedAreas = adh.getAreas("shared");
        viewAdapter = new AreaListAdaptor(mView.getContext(), layout.area_element_row, sharedAreas);

        if (sharedAreas.size() > 0) {
            mView.findViewById(id.shared_area_empty_layout).setVisibility(View.GONE);
            areaListView.setVisibility(View.VISIBLE);

            areaListView.setAdapter(viewAdapter);
            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        } else {
            areaListView.setVisibility(View.GONE);
            mView.findViewById(id.shared_area_empty_layout).setVisibility(View.VISIBLE);
        }
        mView.findViewById(id.splash_panel).setVisibility(View.GONE);

        final ImageView filterUTView = (ImageView) mActivity.findViewById(id.action_filter_ut);
        User user = UserContext.getInstance().getUser();
        UserPersistableSelections userPersistableSelections = user.getSelections();
        if(userPersistableSelections.isFilter()){
            filterUTView.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
            List<Tag> tags = userPersistableSelections.getTags();
            List<String> filterables = new ArrayList<>();
            List<String> executables = new ArrayList<>();
            for(Tag tag: tags){
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
        return "Shared";
    }

    @Override
    public void doFilter(List<String> filterables, List<String> executables) {
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        if(areaListView != null){
            AreaListAdaptor adapter = (AreaListAdaptor) areaListView.getAdapter();
            if(adapter == null || adapter.getCount() == 0){
                return;
            }
            EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
            Editable inputSearchText = inputSearch.getText();
            adapter.getFilterChain(filterables, executables).filter(inputSearchText.toString());
        }
    }

    @Override
    public void resetFilter() {
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        AreaListAdaptor adapter = (AreaListAdaptor) areaListView.getAdapter();
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
            if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_SHARED_SEQ){
                if(editable.toString().equalsIgnoreCase("")){
                    return;
                }else {
                    mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

                    ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
                    ArrayAdapter<Area> adapter = (ArrayAdapter<Area>) areaListView.getAdapter();
                    adapter.getFilter().filter(editable.toString());

                    mView.findViewById(id.splash_panel).setVisibility(View.GONE);
                }
            }
        }

    }

    @Override
    public Object getViewAdaptor() {
        return viewAdapter;
    }

}