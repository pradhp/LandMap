package lm.pkp.com.landmap.area.reporting;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDFieldTreeNode;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.model.AreaAddress;
import lm.pkp.com.landmap.area.model.AreaElement;
import lm.pkp.com.landmap.custom.ThumbnailCreator;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.user.UserContext;

/**
 * Created by USER on 11/24/2017.
 */
public class AreaReportingService extends IntentService {

    private ReportingContext reportingContext = ReportingContext.INSTANCE;
    private AreaElement areaElement = reportingContext.getAreaElement();
    private Map<String, String> reportContent = new HashMap<>();
    private String reportName = null;
    private File docFile = null;
    private LinkedList<PDDocument> workDocuments = new LinkedList<>();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AreaReportingService(String name) {
        super(name);
        PDFBoxResourceLoader.init(reportingContext.getActivityContext());
        reportName = "placero_lms_report_" + areaElement.getName() + ".pdf";
    }

    public AreaReportingService() {
        super("AreaReportingService");
        PDFBoxResourceLoader.init(reportingContext.getActivityContext());
        reportName = "placero_lms_report_" + areaElement.getName() + ".pdf";
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        reportingContext.setGeneratingReport(true);
        generateReport();
        DriveResource driveResource = createDriveResource();
        if (driveResource != null) {
            DriveDBHelper ddh = new DriveDBHelper(reportingContext.getActivityContext());
            ddh.deleteResourceLocally(driveResource);
            ddh.insertResourceLocally(driveResource);
        }
        reportingContext.setGeneratingReport(false);
        notifyCompletion();
    }

    private String docRoot = null;
    private void generateReport() {
        String areaId = areaElement.getUniqueId();
        docRoot = reportingContext.getAreaLocalDocumentRoot(areaId).getAbsolutePath();
        // Gets data from the incoming Intent
        prepareAreaTextContext();

        populateSummary();
        populatePictures(1);
        populateDocuments();
        populateEnd();

        // Prepare final doc by merging all documents
        PDDocument finalDoc = mergeDocuments();
        String docFilePath = docRoot + File.separatorChar + reportName;
        try {
            docFile = new File(docFilePath);
            finalDoc.save(docFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Iterator<PDDocument> docsIter = workDocuments.iterator();
            while (docsIter.hasNext()) {
                PDDocument document = docsIter.next();
                document.close();
            }
            finalDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateDocuments() {
        List<DriveResource> mediaResources = areaElement.getMediaResources();
        for (DriveResource resource : mediaResources) {
            if (resource.getType().equalsIgnoreCase("file")
                    && resource.getContentType().equalsIgnoreCase("Document")
                    && (!resource.getResourceId().startsWith("_doc_generated_"))) {
                populateDocumentAdd(resource.getName());
                PDDocument document = null;
                try {
                    String docPath = docRoot + File.separatorChar + resource.getName();
                    InputStream stream = new FileInputStream(new File(docPath));
                    document = PDDocument.load(stream);
                    workDocuments.add(document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private PDDocument mergeDocuments() {
        PDDocument finalDocument = new PDDocument();
        for (PDDocument document : workDocuments) {
            PDPageTree pageTree = document.getPages();
            int pageCount = pageTree.getCount();
            for (int i = 0; i < pageCount; i++) {
                finalDocument.addPage(pageTree.get(i));
            }
        }
        return finalDocument;
    }

    private void prepareAreaTextContext() {
        // Area Header attributes
        reportContent.put("area_name", areaElement.getName());
        reportContent.put("area_description", areaElement.getDescription());

        // Area Address attributes
        AreaAddress areaAddress = areaElement.getAddress();
        if(areaAddress != null){
            String quote = Pattern.quote("@$");
            String[] addressSplit = areaAddress.getStorableAddress().split(quote);
            for (int i = 0; i < addressSplit.length; i++) {
                String line = addressSplit[i];
                reportContent.put("area_address_line" + i, line);
            }
        }

        // Area Measurement attributes
        DecimalFormat areaFormat = new DecimalFormat("###.##");
        double areaMeasureSqFt = areaElement.getMeasureSqFt();
        double areaMeasureSqMts = areaMeasureSqFt * 0.092903;
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureHec = areaMeasureAcre * 0.404686;
        double areaMeasureDecimals = areaMeasureSqFt / 436;

        reportContent.put("area_measurement_sq_ft", "Square Feet - " + areaFormat.format(areaMeasureSqFt));
        reportContent.put("area_measurement_sq_mt", "Square Meters - " + areaFormat.format(areaMeasureSqMts));
        reportContent.put("area_measurement_dec", "Decimals - " + areaFormat.format(areaMeasureDecimals));
        reportContent.put("area_measurement_acre", "Acre - " + areaFormat.format(areaMeasureAcre));
        reportContent.put("area_measurement_hec", "Hectare - " + areaFormat.format(areaMeasureHec));

        // Area Positions
        DecimalFormat locFormat = new DecimalFormat("##.####");
        PositionElement centerPosition = areaElement.getCenterPosition();
        String centerPosStr = "Center - Latitude: " + locFormat.format(centerPosition.getLat())
                + ", Longitude: " + locFormat.format(centerPosition.getLon());
        reportContent.put("center_position", centerPosStr);

        List<PositionElement> positions = areaElement.getPositions();
        int posCtr = 1;
        for (PositionElement position : positions) {
            String fieldName = "position_" + posCtr;
            String posStr = "Position_" + posCtr + " - Latitude: " + locFormat.format(position.getLat())
                    + ", Longitude: " + locFormat.format(position.getLon());
            reportContent.put(fieldName, posStr);
            posCtr++;
            if (posCtr == 12) {
                break;
            }
        }
    }

    private void populateSummary() {
        AssetManager assetManager = reportingContext.getActivityContext().getAssets();
        PDDocument document = null;
        try {
            InputStream stream = assetManager.open("templates/area_report_template_page_summary.pdf");
            document = PDDocument.load(stream);
            workDocuments.add(document);
            stream.close();

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            List<PDFieldTreeNode> fields = acroForm.getFields();
            for (PDFieldTreeNode field : fields) {
                String fieldName = field.getFullyQualifiedName();
                String fieldValue = reportContent.get(fieldName);
                field.setValue(fieldValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateDocumentAdd(String docName) {
        AssetManager assetManager = reportingContext.getActivityContext().getAssets();
        PDDocument document = null;
        try {
            InputStream stream = assetManager.open("templates/area_report_template_page_documents.pdf");
            document = PDDocument.load(stream);
            workDocuments.add(document);
            stream.close();

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            List<PDFieldTreeNode> fields = acroForm.getFields();
            for (PDFieldTreeNode field : fields) {
                String fieldName = field.getFullyQualifiedName();
                String fieldValue = null;
                if(fieldName.equalsIgnoreCase("doc_name")){
                    fieldValue = docName;
                }else {
                    fieldValue = reportContent.get(fieldName);
                }
                field.setValue(fieldValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final int IMG_HEIGHT = 220;
    private final int IMG_WIDTH = 220;

    private void populatePictures(int start) {
        AssetManager assetManager = reportingContext.getActivityContext().getAssets();
        PDDocument document = null;
        try {
            InputStream stream = assetManager.open("templates/area_report_template_page_pictures.pdf");
            document = PDDocument.load(stream);
            stream.close();

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            List<PDFieldTreeNode> fields = acroForm.getFields();
            for (PDFieldTreeNode field : fields) {
                String fieldName = field.getFullyQualifiedName();
                String fieldValue = reportContent.get(fieldName);
                field.setValue(fieldValue);
            }

            File[] pictureFiles = getPictureFiles();

            int x = 70; int y = 400;
            int xshift = IMG_WIDTH + (int) (20 * (8.0 / 6.0));
            int yshift = IMG_HEIGHT + (int) (20 * (8.0 / 6.0));
            int startX = x; int startY = y;

            PDPage page = document.getPages().get(0);

            PDRectangle mediaBox = page.getMediaBox();
            float pageLeftX = mediaBox.getLowerLeftX();
            float pageLeftY = mediaBox.getLowerLeftY();

            int i = start;
            for (; i <= pictureFiles.length; i++) {
                File pictureFile = pictureFiles[i - 1];
                // Draw image here
                PDImageXObject imgObject = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap fileBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
                Bitmap imageBitmap = Bitmap.createScaledBitmap(fileBitmap, IMG_WIDTH, IMG_HEIGHT, true);
                fileBitmap.recycle();

                imgObject = JPEGFactory.createFromImage(document, imageBitmap);

                float positionX = pageLeftX + startX;
                float positionY = pageLeftY + startY;

                PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false, true);
                //contentStream.restoreGraphicsState();
                contentStream.drawImage(imgObject, positionX, positionY);
                //contentStream.saveGraphicsState();
                contentStream.close();

                if ((i % 2) == 0) {
                    startY = startY - yshift;
                    startX = x;
                } else {
                    startX = startX + xshift;
                }
                if((i % 4 == 0)){
                    break;
                }
            }
            workDocuments.add(document);

            if(pictureFiles.length > i){
                populatePictures(i + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateEnd() {
        AssetManager assetManager = reportingContext.getActivityContext().getAssets();
        PDDocument document = null;
        try {
            InputStream stream = assetManager.open("templates/area_report_template_page_end.pdf");
            document = PDDocument.load(stream);
            workDocuments.add(document);
            stream.close();

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            List<PDFieldTreeNode> fields = acroForm.getFields();
            for (PDFieldTreeNode field : fields) {
                String fieldName = field.getFullyQualifiedName();
                String fieldValue = reportContent.get(fieldName);
                field.setValue(fieldValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DriveResource createDriveResource() {
        String docRoot = reportingContext.getAreaLocalDocumentRoot(areaElement.getUniqueId()).getAbsolutePath();
        String docFilePath = docRoot + File.separatorChar + reportName;
        File docFile = new File(docFilePath);
        if (!docFile.exists()) {
            return null;
        }
        DriveResource resource = new DriveResource();
        resource.setUniqueId(UUID.randomUUID().toString());
        resource.setUserId(UserContext.getInstance().getUserElement().getEmail());
        resource.setContainerId(reportingContext.getDocumentRootDriveResource().getContainerId());
        resource.setResourceId("_doc_generated_" + areaElement.getUniqueId());
        resource.setName(reportName);
        resource.setType("file");
        resource.setContentType("Document");
        resource.setMimeType("application/pdf");
        resource.setAreaId(areaElement.getUniqueId());
        resource.setSize(docFile.length() + "");
        resource.setLatitude("");
        resource.setLongitude("");
        resource.setCreatedOnMillis(System.currentTimeMillis() + "");

        ThumbnailCreator creator = new ThumbnailCreator(reportingContext.getActivityContext());
        creator.createDocumentThumbnail(docFile, areaElement.getUniqueId());
        return resource;
    }

    private void notifyCompletion() {
        // Show notification post generation.
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(docFile), "application/pdf");
        PendingIntent pIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Your report is now available")
                .setContentText(reportName)
                .setSmallIcon(R.drawable.pdf_icon)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

    private File[] getPictureFiles() {
        File pictureRoot = reportingContext.getAreaLocalImageRoot(areaElement.getUniqueId());
        File[] pictureFiles = pictureRoot.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory() && file.canRead() && (file.length() > 0)) {
                    return false;
                } else {
                    return true;
                }
            }
        });
        Arrays.sort(pictureFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });

        return pictureFiles;
    }
}
