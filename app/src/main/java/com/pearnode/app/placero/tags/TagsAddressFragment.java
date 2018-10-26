package com.pearnode.app.placero.tags;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cunoraz.tagview.TagView;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.TagAssignmentActivity;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.app.placero.user.UserPersistableSelections;
import com.pearnode.app.placero.util.ColorProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 11/4/2017.
 */
public class TagsAddressFragment extends Fragment implements FragmentHandler {

    private Activity mActivity = null;
    private View mView = null;
    private boolean offline = false;

    public TagsAddressFragment(){
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address_tags, container, false);
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
        offline = ((TagAssignmentActivity)mActivity).isOffline();
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && (mView != null) && (mActivity != null)) {
            TagsDisplayMetaStore.INSTANCE.setActiveTab(TagsDisplayMetaStore.TAB_ADDRESS_SEQ);
            loadFragment();
        }
    }


    private void loadFragment() {
        final TagView topContainer = (TagView) mView.findViewById(R.id.tag_group);
        topContainer.removeAll();

        TagsDBHelper tdh = new TagsDBHelper(getContext());
        ArrayList<Tag> tags = tdh.getTagsByContext("area");
        for(Tag te: tags){
            com.cunoraz.tagview.Tag tag = new com.cunoraz.tagview.Tag(te.getName());
            tag.tagTextSize = 16;
            tag.layoutColor = ColorProvider.getDefaultToolBarColor();
            topContainer.addTag(tag);
        }

        final LinearLayout bottomLayout = (LinearLayout) mView.findViewById(R.id.bottom_container);
        final TagView bottomContainer = (TagView) mView.findViewById(R.id.tag_selection_view);
        bottomContainer.removeAll();

        topContainer.setOnTagLongClickListener(new TagView.OnTagLongClickListener() {
            @Override
            public void onTagLongClick(com.cunoraz.tagview.Tag tag, int i) {
                tag.isDeletable = true;
                topContainer.remove(i);
                bottomContainer.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
                    @Override
                    public void onTagDeleted(TagView tagView, com.cunoraz.tagview.Tag tag, int i) {
                        tag.isDeletable = false;
                        topContainer.addTag(tag);
                        bottomContainer.remove(i);
                        if (bottomContainer.getTags().size() == 0) {
                            bottomLayout.setVisibility(View.GONE);
                        }
                    }
                });

                bottomContainer.addTag(tag);
            }
        });

        Button addTags = (Button) mView.findViewById(R.id.add_tags_user_action);
        addTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<com.cunoraz.tagview.Tag> selectedTags = bottomContainer.getTags();
                UserElement userElement = UserContext.getInstance().getUserElement();
                UserPersistableSelections preferences = userElement.getSelections();
                if(selectedTags.size() > 0){
                    for(com.cunoraz.tagview.Tag selectedTag: selectedTags){
                        Tag tag = new Tag(selectedTag.text, "filterable", "address");
                        preferences.getTags().add(tag);
                    }
                    Integer position = TagsDisplayMetaStore.INSTANCE.getTabPositionByType("user");
                    TabLayout tabLayout = (TabLayout) mActivity.findViewById(R.id.areas_tags_tab_layout);
                    tabLayout.getTabAt(position).select();
                }else {
                    Toast.makeText(getContext(), "No tags selected. Long click on tag to select", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public String getFragmentTitle() {
        return "Address";
    }

    @Override
    public Object getViewAdaptor() {
        return null;
    }

}
