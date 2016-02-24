package com.miniflickr.qunwu.miniflickr.entity;

import java.io.Serializable;

/**
 * Created by Qun Wu on 2016/2/22.
 */

/*
Contains all the information about a photo received from server
 */
public class GalleryItem implements Serializable{
    private String id;
    private String secret;
    private String server;
    private String farm;

    public GalleryItem(String id, String secret, String server, String farm) {
        this.id = id;
        this.secret = secret;
        this.server = server;
        this.farm = farm;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return "http://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret + ".jpg";
    }
}
