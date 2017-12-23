package lm.pkp.com.landmap.tags;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;

import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.custom.FragmentIdentificationHandler;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.user.UserPersistableSelections;

/**
 * Created by USER on 11/4/2017.
 */
public class TagsAreaFragment extends Fragment implements FragmentIdentificationHandler{

    private Activity mActivity = null;
    private View mView = null;

    public TagsAreaFragment(){
        super();
        setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_tags, container, false);
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
            TagsDisplayMetaStore.INSTANCE.setActiveTab(TagsDisplayMetaStore.TAB_ADDRESS_SEQ);
            loadFragment();
        }
    }


    private void loadFragment() {

        Button addConditionTagButton = (Button) mView.findViewById(R.id.add_condition_tag);
        final TagView bottomContainer = (TagView) mView.findViewById(R.id.tag_selection_view);
        bottomContainer.removeAll();

        addConditionTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner measureSpinner = (Spinner) mView.findViewById(R.id.measure_spinner);
                String selectedMeasure = measureSpinner.getSelectedItem().toString();

                Spinner compareSpinner = (Spinner) mView.findViewById(R.id.compare_spinner);
                String selectedCompare = compareSpinner.getSelectedItem().toString();

                EditText compareValue = (EditText) mView.findViewById(R.id.compare_value);
                Editable text = compareValue.getText();
                if(text.toString().trim().equalsIgnoreCase("")
                        || text.toString().equalsIgnoreCase("?")){
                    compareValue.setText("?");
                    return;
                }
                Tag tag = new Tag(selectedMeasure + " " + selectedCompare + " " + text);
                tag.isDeletable = true;
                bottomContainer.addTag(tag);
            }
        });

        bottomContainer.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
            @Override
            public void onTagDeleted(TagView tagView, Tag tag, int i) {
                bottomContainer.remove(i);
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
                        TagElement tagElement = new TagElement(selectedTag.text, "executable", "area");
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
        return "Area";
    }

}
