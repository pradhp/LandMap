package com.pearnode.app.placero.area;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 11/23/2017.
 */
public class AreaDashboardDisplayMetaStore {

    public static final Integer TAB_OWNED_SEQ = 0;
    public static final Integer TAB_SHARED_SEQ = 1;
    public static final Integer TAB_PUBLIC_SEQ = 2;

    private Integer ACTIVE_TAB = 0;

    private AreaDashboardDisplayMetaStore(){
        tabMeta.put("self", TAB_OWNED_SEQ);
        tabMeta.put("shared", TAB_SHARED_SEQ);
        tabMeta.put("public", TAB_PUBLIC_SEQ);
    }

    public static final AreaDashboardDisplayMetaStore INSTANCE = new AreaDashboardDisplayMetaStore();

    private Map<String, Integer> tabMeta = new HashMap<>();

    public Integer getTabPositionByAreaType(String type){
        return tabMeta.get(type);
    }

    public Integer getActiveTab() {
        return this.ACTIVE_TAB;
    }

    public void setActiveTab(Integer activeTab) {
        this.ACTIVE_TAB = activeTab;
    }
}
