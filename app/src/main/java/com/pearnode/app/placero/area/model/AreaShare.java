package com.pearnode.app.placero.area.model;

public class AreaShare {

    private Long id = -1L;
    private String sourceUser = null;
    private String targetUser = null;
    private String areaId = null;
    private String functionCodes = null;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceUser() {
        return this.sourceUser;
    }

    public void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser;
    }

    public String getTargetUser() {
        return this.targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getAreaId() {
        return this.areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getFunctionCodes() {
        return this.functionCodes;
    }

    public void setFunctionCodes(String functionCodes) {
        this.functionCodes = functionCodes;
    }
}
