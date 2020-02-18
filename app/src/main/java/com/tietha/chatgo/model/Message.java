package com.tietha.chatgo.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class Message {
    private String from;
    private int type; //0 - text, 1 - img, 2 - sticker
    private String mess;
    private String img;
    private String voice;
    private String valueOf;
    private String state;
    private HashMap<String, Object> timestampCreated;

    public Message() {
    }


    public Message(String from, int type, String mess, String img, String voice, String valueOf, String state) {
        this.from = from;
        this.type = type;
        this.mess = mess;
        this.img = img;
        this.voice = voice;
        this.valueOf = valueOf;
        this.state = state;
        HashMap<String, Object> timestampNow = new HashMap<>();
        timestampNow.put("timestamp", ServerValue.TIMESTAMP);
        this.timestampCreated = timestampNow;
    }

    public String getValueOf() {
        return valueOf;
    }

    public void setValueOf(String valueOf) {
        this.valueOf = valueOf;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public HashMap<String, Object> getTimestampCreated(){
        return timestampCreated;
    }
    @Exclude
    public long getTimestampCreatedLong(){
        return (long)timestampCreated.get("timestamp");
    }
}
