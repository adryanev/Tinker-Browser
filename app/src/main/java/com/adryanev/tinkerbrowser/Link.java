package com.adryanev.tinkerbrowser;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by AdryanEV on 03/06/2016.
 * File ini akan menjadi tipe data dari link yang disimpan ke dalam list
 */
public class Link implements Serializable{
    //properties
    private String url;
    private String title;


    public Link()
    {

    }

    public Link(String url,String title)
    {
        super();
        this.url = url;
        this.title = title;

    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }


    public String getTitle()
    {
        return title;
    }


    public void setTitle(String title)
    {
        this.title = title;
    }

}
