package com.pearnode.app.placero.area.res.disp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.pearnode.app.placero.area.res.doc.BaseFragmentAdapter;
import com.pearnode.app.placero.area.res.doc.TextDetailDocumentsCell;
import com.pearnode.app.placero.custom.PermittedFileArrayList;

/**
 * Created by USER on 11/7/2017.
 */
public class DocumentChooserAdaptor extends BaseFragmentAdapter {

    private final Context mContext;
    private final PermittedFileArrayList<FileDisplayElement> items;

    public DocumentChooserAdaptor(Context context, PermittedFileArrayList<FileDisplayElement> chosenItems) {
        mContext = context;
        items = chosenItems;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    public void clear() {
        this.items.clear();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int pos) {
        return this.items.get(pos).getDesc().length() > 0 ? 0 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextDetailDocumentsCell(this.mContext);
        }
        FileDisplayElement item = this.items.get(position);
        ((TextDetailDocumentsCell) convertView).setValues(item.getName(), item.getDesc(), item.getIcon());
        return convertView;
    }
}
