package com.pearnode.app.placero.area.res.disp;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.File;

/**
 * Created by USER on 11/6/2017.
 */
public class DocumentDisplayElement {

    private String name = "";
    private String absPath = "";
    private String resourceId = "";
    private File thumbnailFile = null;
    private File documentFile = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbsPath() {
        return absPath;
    }

    public void setAbsPath(String absPath) {
        this.absPath = absPath;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public File getThumbnailFile() {
        return this.thumbnailFile;
    }

    public void setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public File getDocumentFile() {
        return this.documentFile;
    }

    public void setDocumentFile(File documentFile) {
        this.documentFile = documentFile;
    }

    @Override
    public boolean equals(Object o) {
        EqualsBuilder builder = new EqualsBuilder().append(getResourceId(), ((DocumentDisplayElement) o).getResourceId());
        return builder.isEquals();
    }
}
