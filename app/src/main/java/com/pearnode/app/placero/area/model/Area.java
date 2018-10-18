package com.pearnode.app.placero.area.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.permission.PermissionElement;
import com.pearnode.app.placero.position.Position;

/**
 * Created by USER on 10/16/2017.
 */
public class Area implements Serializable {

    private String name = null;
    private String description = "No Description";
    private String createdBy = null;
    private String type = "self";
    private String uniqueId = null;
    private Integer dirty = 0;
    private String dirtyAction = "none";
    private Address address = null;
    private AreaMeasure measure = new AreaMeasure(0.0);
    private Position centerPosition = new Position();
    private Long createdOn = -1L;
    private Long updatedOn = -1L;

    private List<Position> positions = new ArrayList<>();
    private List<Resource> mediaResources = new ArrayList<>();
    private Map<String, PermissionElement> userPermissions = new HashMap<>();

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

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public List<Resource> getMediaResources() {
        return this.mediaResources;
    }

    public void setMediaResources(List<Resource> mediaResources) {
        this.mediaResources = mediaResources;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, PermissionElement> getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Map<String, PermissionElement> userPermissions) {
        this.userPermissions = userPermissions;
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

    public void setCenterPosition(Position centerPosition) {
        this.centerPosition = centerPosition;
    }

    public Long getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public Long getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(Long updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }
}
