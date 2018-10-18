package com.pearnode.app.placero.place.model;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.model.Address;
import com.pearnode.app.placero.drive.Resource;
import com.pearnode.app.placero.position.Position;

import java.util.ArrayList;
import java.util.List;

public class Place {

    private Area area = null;
    private List<Resource> resources = new ArrayList<>();
    private Address address = null;
    private Position centerPosition = null;

    public Area getArea() {
        return this.area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<Resource> getResources() {
        return this.resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Position getCenterPosition() {
        return this.centerPosition;
    }

    public void setCenterPosition(Position centerPosition) {
        this.centerPosition = centerPosition;
    }
}
