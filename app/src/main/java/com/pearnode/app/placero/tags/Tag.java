package com.pearnode.app.placero.tags;

import com.google.gson.Gson;

/**
 * Created by USER on 12/13/2017.
 */
public class Tag {

    private Long id = -1L;
    private String name = "";
    private String type = "";
    private String typeField = "";
    private String context = "";
    private String contextId = "";
    private Integer dirty = 0;
    private String dirtyAction = "none";
    private Long createdOn = -1L;

    public Tag(String name, String type, String typeField){
        this.name = name;
        this.type = type;
        this.typeField = typeField;
    }

    public Tag(){
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextId() {
        return this.contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeField() {
        return this.typeField;
    }

    public void setTypeField(String typeField) {
        this.typeField = typeField;
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

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }

}
