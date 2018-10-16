package com.pearnode.app.placero.area.res.disp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.layout;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaPictureDisplayFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.fragment_picture_display, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) this.getView().findViewById(id.gridView);
        gridView.setAdapter(new AreaPictureDisplayAdaptor(getContext(), this, 0));
        getView().findViewById(id.res_action_layout).setVisibility(View.GONE);
    }

}
