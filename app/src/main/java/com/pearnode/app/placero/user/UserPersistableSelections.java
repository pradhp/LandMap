package com.pearnode.app.placero.user;

import java.util.ArrayList;
import java.util.List;

import com.pearnode.app.placero.area.model.AreaElement;
import com.pearnode.app.placero.position.PositionElement;
import com.pearnode.app.placero.tags.TagElement;

/**
 * Created by USER on 12/15/2017.
 */
public class UserPersistableSelections {

    private String search = "";
    private String dashboard = "0";
    private boolean filter = false;
    private List<TagElement> tags = new ArrayList<>();
    private AreaElement area = null;
    private PositionElement position = null;

    public String getSearch() {
        return this.search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List<TagElement> getTags() {
        return this.tags;
    }

    public void setTags(List<TagElement> tags) {
        this.tags = tags;
    }

    public String getDashboard() {
        return this.dashboard;
    }

    public void setDashboard(String dashboard) {
        this.dashboard = dashboard;
    }

    public boolean isFilter() {
        return this.filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public AreaElement getArea() {
        return this.area;
    }

    public void setArea(AreaElement area) {
        this.area = area;
    }

    public PositionElement getPosition() {
        return this.position;
    }

    public void setPosition(PositionElement position) {
        this.position = position;
    }
}
