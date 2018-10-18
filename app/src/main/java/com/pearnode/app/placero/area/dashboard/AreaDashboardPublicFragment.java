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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.AreaDashboardActivity;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.AreaDashboardDisplayMetaStore;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.res.disp.AreaItemAdaptor;
import com.pearnode.app.placero.custom.AsyncTaskCallback;
import com.pearnode.app.placero.custom.FragmentFilterHandler;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.sync.LocalDataRefresher;
import com.pearnode.app.placero.tags.TagElement;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.app.placero.user.UserPersistableSelections;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardPublicFragment extends Fragment
        implements FragmentHandler, FragmentFilterHandler {

    private Activity mActivity = null;
    private View mView = null;
    private boolean offline = false;
    private AreaItemAdaptor viewAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_public_areas, container, false);
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
            AreaDashboardDisplayMetaStore.INSTANCE.setActiveTab(AreaDashboardDisplayMetaStore.TAB_PUBLIC_SEQ);
            loadFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loadFragment() {
        AreaContext.INSTANCE.setDisplayBMap(null);
        mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

        final EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
        String availableKey = inputSearch.getText().toString();
        inputSearch.addTextChangedListener(new UserInputWatcher());

        if (availableKey.trim().equalsIgnoreCase("")) {
            LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity,new DataReloadCallback());
            dataRefresher.refreshPublicAreas();
        } else {
            LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback(availableKey.trim()));
            dataRefresher.refreshPublicAreas(availableKey.trim());
        }

        Button seachClearButton = (Button) mActivity.findViewById(id.dashboard_search_clear);
        seachClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText inputSearch = (EditText) mActivity.findViewById(id.dashboard_search_box);
                inputSearch.setText("");
            }
        });
    }

    @Override
    public String getFragmentTitle() {
        return "Public";
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

    private class DataReloadCallback implements AsyncTaskCallback {

        private final String filterStr;

        public DataReloadCallback(String searchKey) {
            filterStr = searchKey;
        }

        public DataReloadCallback() {
            filterStr = "";
        }

        @Override
        public void taskCompleted(Object result) {
            AreaDBHelper adh = new AreaDBHelper(mActivity);

            final ArrayList<Area> publicAreas = adh.getAreas("public");
            if(publicAreas.size() > 0){
                mView.findViewById(id.area_display_list).setVisibility(View.VISIBLE);
                mView.findViewById(id.public_area_empty_layout).setVisibility(View.GONE);
            }else {
                mView.findViewById(id.area_display_list).setVisibility(View.GONE);
                mView.findViewById(id.public_area_empty_layout).setVisibility(View.VISIBLE);
            }

            viewAdapter = new AreaItemAdaptor(mView.getContext(), layout.area_element_row, publicAreas);
            ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
            areaListView.setAdapter(viewAdapter);

            if(!filterStr.equalsIgnoreCase("")){
                viewAdapter.getFilter().filter(filterStr);
            }
            viewAdapter.notifyDataSetChanged();

            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
            mView.findViewById(id.splash_panel).setVisibility(View.INVISIBLE);

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
            if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_PUBLIC_SEQ){
                mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                String filterStr = editable.toString().trim();
                if (!filterStr.equalsIgnoreCase("")) {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback(filterStr));
                    dataRefresher.refreshPublicAreas(filterStr);
                } else {
                    LocalDataRefresher dataRefresher = new LocalDataRefresher(mActivity, new DataReloadCallback());
                    dataRefresher.refreshPublicAreas();
                }
            }
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
