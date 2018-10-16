package com.pearnode.app.placero.tags;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Created by USER on 12/13/2017.
 */
public class TagElement {

    private String name = "";
    private String type = "";
    private String typeField = "";
    private String context = "";
    private String contextId = "";

    public TagElement(String name, String type, String typeField){
        this.name = name;
        this.type = type;
        this.typeField = typeField;
    }

    public TagElement(){
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

    @Override
    public boolean equals(Object o) {
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), ((TagElement) o).getName());
        return builder.isEquals();
    }
}
