package com.pearnode.app.placero.position;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

import com.pearnode.app.placero.weather.model.WeatherElement;

/**
 * Created by USER on 10/16/2017.
 */
public class Position implements Serializable {

    private String name = "";
    private String description = "No Description";
    private double lat = 0.0;
    private double lon = 0.0;
    private String tags = "";
    private String uniqueAreaId = "";
    private String uniqueId = "";
    private String createdOnMillis = System.currentTimeMillis() + "";
    private String type = "boundary";
    private Integer dirty = 0;
    private String dirtyAction = "";
    private WeatherElement weather;

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

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUniqueAreaId() {
        return this.uniqueAreaId;
    }

    public void setUniqueAreaId(String uniqueAreaId) {
        this.uniqueAreaId = uniqueAreaId;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Position copy() {
        return SerializationUtils.clone(this);
    }

    public String getCreatedOnMillis() {
        return createdOnMillis;
    }

    public void setCreatedOnMillis(String createdOnMillis) {
        this.createdOnMillis = createdOnMillis;
    }

    public WeatherElement getWeather() {
        return weather;
    }

    public void setWeather(WeatherElement weather) {
        this.weather = weather;
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
}
