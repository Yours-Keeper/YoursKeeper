package com.example.yourskeeper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Chat {
    private String mName;
    private String mMessage;
    private String mUid;
    private String mTime;

    private Timestamp mTimestamp;

    public Chat() { } // Needed for Firebase

    public Chat(String name, String message, String uid, String time) {
        mName = name;
        mMessage = message;
        mUid = uid;
        mTime = time;

    }

    public String getName() { return mName; }

    public void setName(String name) { mName = name; }

    public String getMessage() { return mMessage; }

    public void setMessage(String message) { mMessage = message; }

    public String getUid() { return mUid; }

    public void setUid(String uid) { mUid = uid; }
    public String getTime() { return mTime; }



    public void setTime(String time) { mTime = time; }

    @ServerTimestamp
    public Timestamp getTimestamp() { return mTimestamp; }

    public void setTimestamp(Timestamp timestamp) { mTimestamp = timestamp; }

}
