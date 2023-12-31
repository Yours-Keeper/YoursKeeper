package com.example.yourskeeper;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Store {
    private String mNickname;
    private String mTime;
    private Integer mPoint;
    private String mUid;
    private Float mDistance;
    private double mLat;
    private double mLon;
    private String mContent;
    private Timestamp mTimestamp;

    public Store() { } // Needed for Firebase

    public Store(String nickname, String time, String uid, Float distance, Integer point, String content, double lat, double lon) {
        mNickname = nickname;
        mPoint = point;
        mTime = time;
        mUid = uid;
        mContent = content;
        mDistance =distance;
        mLat =lat;
        mLon = lon;
    }

    public String getNickname() { return mNickname; }
    public void setNickname(String nickname) { mNickname = nickname; }
    public void setLat(double lat) {mLat =lat;}
    public double getLat() {
        return mLat;
    }
    public void setLon(double lon) {mLon =lon;}
    public double getLon() {
        return mLon;
    }

    public String getContent() { return mContent; }
    public void setContent(String content) { mContent = content; }

    public String getTime() { return mTime; }
    public void setTime(String time) { mTime = time; }

    public String getUid() { return mUid; }
    public void setUid(String uid) { mUid = uid; }

    public Float getDistance() {return mDistance;}
    public void setDistance(Float distance) {mDistance = distance;}

    public Integer getPoint() {return mPoint;};
    public void setPoint(Integer point) {mPoint = point;}

    @ServerTimestamp
    public Timestamp getTimestamp() { return mTimestamp; }
    public void setTimestamp(Timestamp timestamp) { mTimestamp = timestamp; }
}
