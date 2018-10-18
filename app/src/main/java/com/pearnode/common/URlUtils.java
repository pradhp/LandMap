package com.pearnode.common;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class URlUtils {

    public static final String getPostDataString(Map<String, Object> params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keySet().iterator();
        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);
            if (first) {
                first = false;
            }else {
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

}
