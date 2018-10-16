package com.pearnode.app.placero.area.res.disp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDocumentDisplayFragment extends Fragment {

    private GridView gridView;
    private AreaDocumentDisplayAdaptor adaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_document_display, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.gridView = (GridView) this.getView().findViewById(id.gridView);
        this.adaptor = new AreaDocumentDisplayAdaptor(this.getContext(), this, 2);
        this.gridView.setAdapter(this.adaptor);
        getView().findViewById(id.res_action_layout).setVisibility(View.GONE);
    }

}
