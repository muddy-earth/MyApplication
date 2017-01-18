package com.example.debajyotidas.myapplication.model;

/**
 * Created by overtatech-4 on 17/1/17.
 */

public class User {
    String name;
    String img_url;
    boolean isOnline;

    public User() {
    }

    public User(String name, String img_url, boolean isOnline) {
        this.name = name;
        this.img_url = img_url;
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
