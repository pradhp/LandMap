package com.pearnode.app.placero.permission;

import java.util.Map;

import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.model.Area;

/**
 * Created by USER on 11/11/2017.
 */
public class PermissionManager {

    public static final PermissionManager INSTANCE = new PermissionManager();

    private PermissionManager() {
    }

    public boolean hasAccess(String functionCode) {
        Area area = AreaContext.INSTANCE.getArea();
        if(area.getType().equalsIgnoreCase("self")){
            return true;
        }
        Map<String, Permission> areaPermissions = area.getPermissions();
        Permission fullControl = areaPermissions.get(PermissionConstants.FULL_CONTROL);
        if (fullControl != null) {
            return true;
        }
        Permission viewOnly = areaPermissions.get(PermissionConstants.VIEW_ONLY);
        if (viewOnly != null) {
            return false;
        }
        Permission permission = areaPermissions.get(functionCode);
        return permission != null;
    }
}
