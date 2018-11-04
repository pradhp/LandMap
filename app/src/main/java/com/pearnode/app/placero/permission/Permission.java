package com.pearnode.app.placero.permission;

import com.google.gson.Gson;

/**
 * Created by USER on 11/11/2017.
 */
public class Permission {

    private String areaId;
    private String userId;
    private String functionCode;
    private Integer dirty = 0;
    private String dirtyAction = "";

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }

}
