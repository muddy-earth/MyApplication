package com.example.debajyotidas.myapplication.model;

/**
 * Created by overtatech-4 on 17/1/17.
 */

public class User {
    String name, last_seen;
    String img_url;
    boolean isOnline;
    String reg_token;

    public User() {
    }

    public User(String name, String img_url, boolean isOnline, String reg_token) {
        this.name = name;
        this.last_seen = last_seen;
        this.img_url = img_url;
        this.isOnline = isOnline;
        this.reg_token = reg_token;
    }



    public String getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(String last_seen) {
        this.last_seen = last_seen;
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

    public String getReg_token() {
        return reg_token;
    }

    public void setReg_token(String reg_token) {
        this.reg_token = reg_token;
    }
}
