package com.pearnode.app.placero.area.res.disp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.media.db.MediaDataBaseHandler;
import com.pearnode.app.placero.media.model.Media;

import java.util.List;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaVideoDisplayFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_display, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) this.getView().findViewById(R.id.gridView);
        Area area = AreaContext.INSTANCE.getArea();
        VideoDisplayAdaptor adaptor = new VideoDisplayAdaptor(getContext(), R.layout.media_display_item, area.getVideos());
        adaptor.setFragment(this);
        gridView.setAdapter(adaptor);
    }

}
