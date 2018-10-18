package com.pearnode.constants;

public final class APIRegistry {

    private static final String API_ROOT = FixedValuesRegistry.PLACERO_API;

    public static final String AREA_CREATE = API_ROOT + "/area/create.php";
    public static final String AREA_REMOVE = API_ROOT + "/area/remove.php";
    public static final String AREA_UPDATE = API_ROOT + "/area/update.php";

    public static final String SHARE_AREA = API_ROOT + "/area/share/user.php";

    // Add API's as required.
    public static final String PUBLIC_AREAS_SEARCH = API_ROOT + "/area/search/public.php";
    public static final String USER_AREA_SEARCH = API_ROOT + "/area/search/user.php";

    public static final String DRIVE_SEARCH_GENERIC = API_ROOT + "/drive/search/key.php";
    public static final String POSITIONS_SEARCH_GENERIC = API_ROOT + "/position/search/key.php";
    public static final String USER_SEARCH_GENERIC = API_ROOT + "/user/search/key.php";

    public static final String GENERIC_SEARCH = API_ROOT;
}
