package com.pearnode.app.placero.position;

import com.google.gson.Gson;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by USER on 10/16/2017.
 */
public class Position implements Serializable {

    private String id = "";
    private String name = "";
    private String description = "No Description";
    private double lat = 0.0;
    private double lng = 0.0;
    private String tags = "";
    private String areaRef = "";
    private String createdOn = System.currentTimeMillis() + "";
    private String type = "boundary";
    private Integer dirty = 0;
    private String dirtyAction = "";

    public Position(){
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAreaRef() {
        return areaRef;
    }

    public void setAreaRef(String areaRef) {
        this.areaRef = areaRef;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return this.lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Position copy() {
        return SerializationUtils.clone(this);
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String toString() {
        return new Gson().toJson(this).toString();
    }

}
