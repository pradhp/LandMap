package lm.pkp.com.landmap.tags;

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

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;

import java.util.ArrayList;
import java.util.List;

import lm.pkp.com.landmap.AreaDashboardActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.TagAssignmentActivity;
import lm.pkp.com.landmap.custom.FragmentHandler;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.user.UserPersistableSelections;
import lm.pkp.com.landmap.util.ColorProvider;

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
        ArrayList<TagElement> tagElements = tdh.getTagsByContext("area");
        for(TagElement te: tagElements){
            Tag tag = new Tag(te.getName());
            tag.tagTextSize = 16;
            tag.layoutColor = ColorProvider.getDefaultToolBarColor();
            topContainer.addTag(tag);
        }

        final LinearLayout bottomLayout = (LinearLayout) mView.findViewById(R.id.bottom_container);
        final TagView bottomContainer = (TagView) mView.findViewById(R.id.tag_selection_view);
        bottomContainer.removeAll();

        topContainer.setOnTagLongClickListener(new TagView.OnTagLongClickListener() {
            @Override
            public void onTagLongClick(Tag tag, int i) {
                tag.isDeletable = true;
                topContainer.remove(i);
                bottomContainer.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
                    @Override
                    public void onTagDeleted(TagView tagView, Tag tag, int i) {
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
                List<Tag> selectedTags = bottomContainer.getTags();
                UserElement userElement = UserContext.getInstance().getUserElement();
                UserPersistableSelections preferences = userElement.getSelections();
                if(selectedTags.size() > 0){
                    for(Tag selectedTag: selectedTags){
                        TagElement tagElement = new TagElement(selectedTag.text, "filterable", "address");
                        preferences.getTags().add(tagElement);
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
    public void setOffline(boolean offline) {
        this.offline = offline;
    }

}
