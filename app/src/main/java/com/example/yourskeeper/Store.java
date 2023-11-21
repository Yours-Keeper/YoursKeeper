package com.example.yourskeeper;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Store {
    private String mNickname;
    private String mTime;
    private Long mScore;
    private String mUid;
    private String mDistance;
    private Timestamp mTimestamp;

    public Store() { } // Needed for Firebase

    public Store(String nickname, String time,String uid) {
        mNickname = nickname;

        mTime = time;
        mUid = uid;
//        mScore =score;
//        mDistance =distance;
    }

    public String getNickname() { return mNickname; }

    public void setNickname(String nickname) { mNickname = nickname; }
//    public Long getScore() { return mScore; }
//
//    public void setScore(Long score) { mScore = score; }

    public String getTime() { return mTime; }

    public void setTime(String time) { mTime = time; }

//    public String getDistance() { return mDistance; }
//
//    public void setDistance(String distance) { mDistance = distance; }

    @ServerTimestamp
    public Timestamp getTimestamp() { return mTimestamp; }

    public void setTimestamp(Timestamp timestamp) { mTimestamp = timestamp; }
}
