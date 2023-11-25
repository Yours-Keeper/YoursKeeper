package com.example.yourskeeper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Chat {
    private String mRoomid;
    private String mName;
    private String mMessage;
    private String mUid;
    private Timestamp mTimestamp;

    public Chat() { } // Needed for Firebase

    public Chat(String name, String message, String uid, String roomid) {
        mName = name;
        mMessage = message;
        mUid = uid;
        mRoomid = roomid;
    }

    public String getName() { return mName; }

    public void setName(String name) { mName = name; }

    public String getMessage() { return mMessage; }

    public void setMessage(String message) { mMessage = message; }

    public String getUid() { return mUid; }

    public void setUid(String uid) { mUid = uid; }

    @ServerTimestamp
    public Timestamp getTimestamp() { return mTimestamp; }

    public void setTimestamp(Timestamp timestamp) { mTimestamp = timestamp; }

    public String getRoomid() { return mRoomid; }

    public void setRoomid(String roomid) { mRoomid = roomid; }
}
