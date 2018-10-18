package com.pearnode.app.placero.util;

import android.graphics.Color;

import com.pearnode.app.placero.area.model.Area;

/**
 * Created by USER on 11/12/2017.
 */
public class ColorProvider {

    private ColorProvider() {
    }

    public static final int DEFAULT_TOOLBAR_COLOR = Color.parseColor("#07063D");

    public static final int BUFF_YELLOW_TOOLBAR = Color.parseColor("#556E01");
    public static final int BUFF_BLUE_TOOLBAR = Color.parseColor("#07063D");
    public static final int BUFF_ORANGE_TOOLBAR = Color.parseColor("#622807");

    public static final int BUFF_YELLOW_AREA_DISPLAY = Color.parseColor("#CCEA69");
    public static final int BUFF_BLUE_AREA_DISPLAY = Color.parseColor("#C5D2F7");
    public static final int BUFF_ORANGE_AREA_DISPLAY = Color.parseColor("#F7C7AC");

    public static final int DEFAULT_POLYGON_BOUNDARY = Color.parseColor("#5DADE2");
    public static final int DEFAULT_POLYGON_FILL = Color.parseColor("#85C1E9");
    public static final int DEFAULT_POLYGON_MEDIA_LINK = Color.parseColor("#F5EEF8");

    public static final int DEFAULT_DIRTY_ITEM_COLOR = Color.parseColor("#F8F2DF");

    public static final int getAreaToolBarColor(Area area) {
        if (area.getType().equalsIgnoreCase("shared")) {
            return BUFF_YELLOW_TOOLBAR;
        } else if (area.getType().equalsIgnoreCase("public")) {
            return BUFF_ORANGE_TOOLBAR;
        } else {
            return BUFF_BLUE_TOOLBAR;
        }
    }

    public static final int getAreaDetailsColor(Area area) {
        if (area.getType().equalsIgnoreCase("shared")) {
            return BUFF_YELLOW_AREA_DISPLAY;
        } else if (area.getType().equalsIgnoreCase("public")) {
            return BUFF_ORANGE_AREA_DISPLAY;
        } else {
            return BUFF_BLUE_AREA_DISPLAY;
        }
    }

    public static int getDefaultToolBarColor() {
        return DEFAULT_TOOLBAR_COLOR;
    }
}
