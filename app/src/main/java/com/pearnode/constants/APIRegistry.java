package com.pearnode.constants;

public final class APIRegistry {

    private static final String API_ROOT = FixedValuesRegistry.PLACERO_API;

    public static final String AREA_CREATE = API_ROOT + "/area/create.php";
    public static final String AREA_REMOVE = API_ROOT + "/area/remove.php";
    public static final String AREA_UPDATE = API_ROOT + "/area/update.php";

    public static final String AREA_SHARE_USER = API_ROOT + "/area/share/user.php";
    public static final String AREA_MAKE_PUBLIC = API_ROOT + "/area/share/make_public.php";

    public static final String POSITION_CREATE = API_ROOT + "/position/create.php";
    public static final String POSITION_UPDATE = API_ROOT + "/position/update.php";
    public static final String POSITION_REMOVE = API_ROOT + "/position/remove.php";


    public static final String TAG_CREATE = API_ROOT + "/tag/create.php";

    public static final String MEDIA_CREATE = API_ROOT + "/media/create.php";
    public static final String MEDIA_REMOVE = API_ROOT + "/media/remove.php";

    // Add API's as required.
    public static final String PUBLIC_AREAS_SEARCH = API_ROOT + "/area/search/public.php";

    public static final String USER_CREATE = API_ROOT + "/user/create.php";
    public static final String USER_AREA_SEARCH = API_ROOT + "/area/search/user.php";
    public static final String USER_SEARCH_GENERIC = API_ROOT + "/user/search/key.php";
}
