package lm.pkp.com.landmap.area.res.doc;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import lm.pkp.com.landmap.AreaAddResourcesActivity;
import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.drawable;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.res.disp.DocumentChooserAdaptor;
import lm.pkp.com.landmap.area.res.disp.FileDisplayElement;
import lm.pkp.com.landmap.custom.PermittedFileArrayList;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.FileUtil;

public class AreaDocumentChooserFragment extends Fragment {

    private View fragmentView;
    private ListView listView;
    private DocumentChooserAdaptor listAdapter;
    private final PermittedFileArrayList<FileDisplayElement> items = new PermittedFileArrayList<FileDisplayElement>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (this.fragmentView == null) {
            this.fragmentView = inflater.inflate(layout.document_select_layout, container, false);

            this.listAdapter = new DocumentChooserAdaptor(this.getContext(), this.items);
            TextView emptyView = (TextView) this.fragmentView.findViewById(id.searchEmptyView);
            this.listView = (ListView) this.fragmentView.findViewById(id.document_list_view);
            this.listView.setEmptyView(emptyView);
            this.listView.setAdapter(this.listAdapter);

            this.listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    FileDisplayElement item = AreaDocumentChooserFragment.this.items.get(i);
                    File documentFile = new File(item.getPath());
                    if (!documentFile.canRead()) {
                        AreaDocumentChooserFragment.this.showErrorBox("Error: File cannot be read. Probably corrupt / locked.");
                        return;
                    }
                    if (documentFile.length() == 0) {
                        AreaDocumentChooserFragment.this.showErrorBox("Error: File does not have any contents.");
                        return;
                    }

                    AreaContext areaContext = AreaContext.INSTANCE;
                    AreaElement ae = areaContext.getAreaElement();

                    File loadFile = new File(areaContext.getAreaLocalDocumentRoot(ae.getUniqueId())
                            .getAbsolutePath() + File.separatorChar + documentFile.getName());
                    try {
                        FileUtils.copyFile(documentFile, loadFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    DriveResource resource = new DriveResource();
                    resource.setName(loadFile.getName());
                    resource.setPath(loadFile.getAbsolutePath());
                    resource.setType("file");
                    resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
                    resource.setSize(loadFile.length() + "");
                    resource.setUniqueId(UUID.randomUUID().toString());
                    resource.setAreaId(ae.getUniqueId());
                    resource.setMimeType(FileUtil.getMimeType(loadFile));
                    resource.setContentType("Document");
                    resource.setContainerId(areaContext.getDocumentRootDriveResource().getResourceId());

                    areaContext.addResourceToQueue(resource);

                    AreaDocumentChooserFragment.this.getActivity().finish();

                    Intent intent = new Intent(AreaDocumentChooserFragment.this.getActivity(), AreaAddResourcesActivity.class);
                    AreaDocumentChooserFragment.this.startActivity(intent);
                }
            });

            PermittedFileArrayList<FileDisplayElement> files = new PermittedFileArrayList<>();
            files.addAll(this.findFiles(this.getContext(), "external", "application/pdf"));
            files.addAll(this.findFiles(this.getContext(), "internal", "application/pdf"));

            this.items.addAll(files);
            this.listAdapter.notifyDataSetChanged();
        } else {
            ViewGroup parent = (ViewGroup) this.fragmentView.getParent();
            if (parent != null) {
                parent.removeView(this.fragmentView);
            }
        }
        return this.fragmentView;
    }

    private PermittedFileArrayList<FileDisplayElement> findFiles(Context context, String location, String mimeType) {
        PermittedFileArrayList<FileDisplayElement> searchedFiles = new PermittedFileArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri(location);

        String mimeTypeCriteria = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String[] mimeTypeArgs = {mimeType};

        Cursor cursor = cr.query(uri, null, mimeTypeCriteria, mimeTypeArgs, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                String fileTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String fileMime = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
                String fileSize = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String fileLastModifed = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));
                String fileCreated = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));

                FileDisplayElement fileDisplayItem = new FileDisplayElement();
                fileDisplayItem.setIcon(drawable.pdf_icon);
                fileDisplayItem.setName(fileTitle);
                fileDisplayItem.setDesc(this.createDescriptionText(fileSize, fileLastModifed));
                fileDisplayItem.setMimeType(fileMime);
                fileDisplayItem.setPath(filePath);
                fileDisplayItem.setCreated(fileCreated);
                fileDisplayItem.setLastModified(fileLastModifed);
                fileDisplayItem.setSizeBytes(fileSize);

                searchedFiles.add(fileDisplayItem);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return searchedFiles;
    }

    private String createDescriptionText(String fileSize, String fileLastModifed) {
        // Prepare the size desc.
        Long size = new Long(fileSize);
        int sizeKB = (int) ((float) size / 1024);
        int sizeMB = (int) ((float) sizeKB / 1024);
        StringBuffer descBuffer = new StringBuffer();
        if (sizeKB > 1024) {
            descBuffer.append("Size " + sizeMB + "MBs, ");
        } else {
            descBuffer.append("Size " + sizeKB + "KBs, ");
        }

        // Prepare the last modified
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Calcutta"));
        calendar.setTimeInMillis(new Long(fileLastModifed) * 1000);
        String formattedDate = format.format(calendar.getTime());
        descBuffer.append(formattedDate);

        return descBuffer.toString();
    }


    public void showErrorBox(String error) {
        if (this.getActivity() == null) {
            return;
        }
        new Builder(this.getActivity())
                .setTitle("Document Chooser Error")
                .setMessage(error).setPositiveButton("OK", null).show();
    }
}
