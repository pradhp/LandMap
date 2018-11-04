package com.pearnode.app.placero.area.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.pearnode.app.placero.media.model.Media;
import com.pearnode.app.placero.permission.Permission;
import com.pearnode.app.placero.position.Position;

/**
 * Created by USER on 10/16/2017.
 */
public class Area implements Serializable {

    private String id = "";
    private String name = "";
    private String description = "No Description";
    private String createdBy = null;
    private String type = "self";
    private Integer dirty = 0;
    private String dirtyAction = "none";

    private Address address = new Address();
    private AreaMeasure measure = new AreaMeasure(0.0);
    private Position centerPosition = new Position();

    private List<Position> positions = new ArrayList<>();
    private List<Media> pictures = new ArrayList<>();
    private List<Media> videos = new ArrayList<>();
    private List<Media> documents = new ArrayList<>();
    private Map<String, Permission> permissions = new HashMap<>();

    public Area(){
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getDirty() {
        return this.dirty;
    }

    public void setDirty(Integer dirty) {
        this.dirty = dirty;
    }

    public String getDirtyAction() {
        return this.dirtyAction;
    }

    public void setDirtyAction(String dirtyAction) {
        this.dirtyAction = dirtyAction;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Position> getPositions() {
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            Position position = positions.get(i);
            String posName = position.getName();
            if(posName.startsWith("P_")){
                position.setName("Position_" + (i + 1));
            }
        }
        return this.positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Permission> permissions) {
        this.permissions = permissions;
    }

    public Position getCenterPosition() {
        return centerPosition;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public AreaMeasure getMeasure() {
        return this.measure;
    }

    public void setMeasure(AreaMeasure measure) {
        this.measure = measure;
    }

    public List<Media> getPictures() {
        return pictures;
    }

    public void setPictures(List<Media> pictures) {
        this.pictures = pictures;
    }

    public List<Media> getVideos() {
        return videos;
    }

    public void setVideos(List<Media> videos) {
        this.videos = videos;
    }

    public List<Media> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Media> documents) {
        this.documents = documents;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }
}
