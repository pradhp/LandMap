package com.pearnode.app.placero.user;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.position.Position;
import com.pearnode.app.placero.tags.Tag;

/**
 * Created by USER on 12/15/2017.
 */
public class UserPersistableSelections {

    private boolean filter = false;
    private List<Tag> tags = new ArrayList<>();
    private Area area = null;
    private Position position = null;

    public List<Tag> getTags() {
        return this.tags;
    }

    public boolean isFilter() {
        return this.filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public Area getArea() {
        return this.area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
