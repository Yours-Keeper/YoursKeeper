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
    private Timestamp mTimestamp;

    public Store() { } // Needed for Firebase

    public Store(String nickname, String time,String uid, Float distance, Integer point) {
        mNickname = nickname;
        mPoint = point;
        mTime = time;
        mUid = uid;

        mDistance =distance;
    }

    public String getNickname() { return mNickname; }

    public void setNickname(String nickname) { mNickname = nickname; }

    public String getTime() { return mTime; }
    public Float getDistance() {return mDistance;}
    public void setTime(String time) { mTime = time; }
    public void setDistance(Float distance) {mDistance = distance;}
    public Integer getPoint() {return mPoint;};

    public void setPoint(Integer point) {mPoint = point;}


    @ServerTimestamp
    public Timestamp getTimestamp() { return mTimestamp; }

    public void setTimestamp(Timestamp timestamp) { mTimestamp = timestamp; }
}
