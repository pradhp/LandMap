package com.pearnode.app.placero.media.model;

import com.google.gson.Gson;

import java.util.UUID;

public class Media {

    private String id = null;
    private String placeRef = null;
    private String name = "";
    private String type = "";
    private String tfName = "";
    private String tfPath = "";
    private String rfName = "";
    private String rfPath = "";
    private String lat = "";
    private String lng = "";
    private Integer dirty = 0;
    private String dirtyAction = "none";
    private Long createdOn = -1L;
    private Long fetchedOn = -1L;

    public void Media(){
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceRef() {
        return placeRef;
    }

    public void setPlaceRef(String placeRef) {
        this.placeRef = placeRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTfName() {
        return tfName;
    }

    public void setTfName(String tfName) {
        this.tfName = tfName;
    }

    public String getRfName() {
        return rfName;
    }

    public void setRfName(String rfName) {
        this.rfName = rfName;
    }

    public String getTfPath() {
        return tfPath;
    }

    public void setTfPath(String tfPath) {
        this.tfPath = tfPath;
    }

    public String getRfPath() {
        return rfPath;
    }

    public void setRfPath(String rfPath) {
        this.rfPath = rfPath;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Long getFetchedOn() {
        return fetchedOn;
    }

    public void setFetchedOn(Long fetchedOn) {
        this.fetchedOn = fetchedOn;
    }

    public Integer getDirty() {
        return dirty;
    }

    public void setDirty(Integer dirty) {
        this.dirty = dirty;
    }

    public String getDirtyAction() {
        return dirtyAction;
    }

    public void setDirtyAction(String dirtyAction) {
        this.dirtyAction = dirtyAction;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }

}
