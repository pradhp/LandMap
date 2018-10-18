package com.pearnode.app.placero.tags;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.pearnode.app.placero.AreaDashboardActivity;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.TagAssignmentActivity;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.app.placero.user.UserPersistableSelections;

import java.util.List;

/**
 * Created by USER on 11/4/2017.
 */
public class TagsUserFragment extends Fragment implements FragmentHandler {

    private Activity mActivity = null;
    private View mView = null;
    private boolean offline = false;

    public TagsUserFragment(){
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_tags, container, false);
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
            TagsDisplayMetaStore.INSTANCE.setActiveTab(TagsDisplayMetaStore.TAB_USER_SEQ);
            loadFragment();
        }
    }


    private void loadFragment() {
        final TagView topContainer = (TagView) mView.findViewById(R.id.tag_group);
        topContainer.removeAll();

        UserElement userElement = UserContext.getInstance().getUserElement();
        final UserPersistableSelections preferences = userElement.getSelections();
        final String userId = userElement.getEmail();

        final List<TagElement> userTags = preferences.getTags();
        for(TagElement userTag: userTags){
            Tag tag = new Tag(userTag.getName());
            tag.tagTextSize = 15;
            tag.isDeletable = true;
            tag.layoutColor = Color.parseColor("#E67E22");
            topContainer.addTag(tag);
        }

        topContainer.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
            @Override
            public void onTagDeleted(TagView tagView, Tag tag, int i) {
                topContainer.remove(i);
                for (int j = 0; j < userTags.size(); j++) {
                    TagElement tagElement = userTags.get(j);
                    if(tagElement.getName().equalsIgnoreCase(tag.text)){
                        userTags.remove(tagElement);
                    }
                }
            }
        });

        Button addTags = (Button) mView.findViewById(R.id.add_tags_user_action);
        addTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagsDBHelper tdh = new TagsDBHelper(mActivity);

                tdh.deleteTagsByContext("user", userId);
                tdh.insertTagsLocally(userTags, "user", userId);
                tdh.insertTagsToServer(userTags, "user", userId);

                Intent intent = new Intent(mActivity, AreaDashboardActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public String getFragmentTitle() {
        return "User";
    }

    @Override
    public Object getViewAdaptor() {
        return null;
    }

}
