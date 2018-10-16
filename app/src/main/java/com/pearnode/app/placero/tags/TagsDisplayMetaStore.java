package com.pearnode.app.placero.tags;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 11/23/2017.
 */
public class TagsDisplayMetaStore {

    public static final Integer TAB_ADDRESS_SEQ = 0;
    public static final Integer TAB_AREA_SEQ = 1;
    public static final Integer TAB_USER_SEQ = 2;

    private Integer ACTIVE_TAB = 0;

    private TagsDisplayMetaStore(){
        tabMeta.put("address", TAB_ADDRESS_SEQ);
        tabMeta.put("area", TAB_AREA_SEQ);
        tabMeta.put("user", TAB_USER_SEQ);
    }

    public static final TagsDisplayMetaStore INSTANCE = new TagsDisplayMetaStore();

    private Map<String, Integer> tabMeta = new HashMap<>();

    public Integer getTabPositionByType(String type){
        return tabMeta.get(type);
    }

    public Integer getActiveTab() {
        return this.ACTIVE_TAB;
    }

    public void setActiveTab(Integer activeTab) {
        this.ACTIVE_TAB = activeTab;
    }
}
