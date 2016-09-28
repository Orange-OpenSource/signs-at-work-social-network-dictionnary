package com.orange.signsatwork;


import java.util.HashMap;
import java.util.Map;


public class DailymotionCache {
    public Map<String, String> cacheurl;

    public DailymotionCache() {
        this.cacheurl = new HashMap<String, String>();
    }
    public void append(String url, String streamUrl) {
        cacheurl.put(url, streamUrl);
    }

}
