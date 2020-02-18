package com.tietha.chatgo.model;

public class User {
    private String id;
    private String avatar;
    private String name;
    private String des;
    private Boolean state;

    public User(String id, String avatar, String name, String des, Boolean state) {
        this.id = id;
        this.avatar = avatar;
        this.name = name;
        this.des = des;
        this.state = state;
    }

    public User() {
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
