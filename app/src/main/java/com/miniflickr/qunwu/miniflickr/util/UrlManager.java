package com.miniflickr.qunwu.miniflickr.util;

import android.net.Uri;

/**
 * Created by Qun Wu on 2016/2/18.
 */
public class UrlManager {
    private static final String TAG = UrlManager.class.getSimpleName();

    /*
    a valid url is :
    https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=178069b03af62f5735258c0a10a14d6e&format=json&nojsoncallback=1&text=cat&page=1
    Mine is :
    https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=393e376fe3bab9f84a0042a015131f60&format=json&nojsoncallback=1&page=1
     */
    //1.8a96749a0cfdbe161d40a714a63884f2     2.393e376fe3bab9f84a0042a015131f60
    public static final String API_KEY = "393e376fe3bab9f84a0042a015131f60";
    public static final String PREF_SEARCH_QUERY ="searchQuery";

    /*
    page search: reach the photos at server side page by page
    1. get recent photos
    2. search by keyword
     */
    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String METHOD_GETRECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    /*
    single photo search:: search by id
     */
    private static final String FLICKR_URL = "http://flickr.com/photo.gne?id=%s";
    private static final String METHOD_GETINFO = "flickr.photos.getInfo";

    //singleton design pattern
    private static volatile UrlManager instance = null;
    private UrlManager() {

    }

    public static UrlManager getInstance() {
        if (instance == null) {
            synchronized (UrlManager.class) {
                if (instance == null) {
                    instance = new UrlManager();
                }
            }
        }
        return instance;
    }

    public static String getItemUrl(String query, int page) {
        String url;
        if (query != null) {
            url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_SEARCH)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("text", query)
                    .appendQueryParameter("page", String.valueOf(page))
                    .build().toString();
        } else {
            url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GETRECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("page", String.valueOf(page))
                    .build().toString();
        }
        return url;
    }

    public static String getFlickrUrl(String id){
        return String.format(FLICKR_URL,id);
    }

    public static String getPhotoInfoUrl(String id){
        return Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method",METHOD_GETINFO)
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter("format","json")
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("photo_id",id)
                .build().toString();
    }
}
