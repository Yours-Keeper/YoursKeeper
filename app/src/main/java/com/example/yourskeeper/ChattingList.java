package com.example.yourskeeper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class ChattingList {
    private String mName;
    private String mMessage;
    private String mCreatedBy;
    private String mCreatedFor;
    private String mRoomId;
    private double mLat;
    private double mLon;
    private String mUid;
    private String mTime;
    private Timestamp mTimestamp;

    public ChattingList() { } // Needed for Firebase

    public ChattingList(String name, String time, String roomid, String createdby, String createdfor, double lat, double lon) {
        mName = name;
        mTime = time;
        mCreatedBy = createdby;
        mCreatedFor = createdfor;
        mRoomId = roomid;
        mLat =lat;
        mLon = lon;
    }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public String getMessage() { return mMessage; }
    public void setMessage(String message) { mMessage = message; }

    public String getUid() { return mUid; }
    public void setUid(String uid) { mUid = uid; }
    public void setLat(double lat) {mLat =lat;}
    public double getLat() {
        return mLat;
    }
    public void setLon(double lon) {mLon =lon;}
    public double getLon() {
        return mLon;
    }
    public String getCreatedBy() { return mCreatedBy; }
    public void setCreatedBy(String createdby) { mCreatedBy = createdby; }

    public String getCreatedFor() { return mCreatedFor; }
    public void setCreatedFor(String createdfor) { mCreatedFor = createdfor; }

    public String getTime() { return mTime; }
    public void setTime(String time) { mTime = time; }

    public String getRoomId() { return mRoomId; }
    public void setRoomId(String roomid) { mRoomId = roomid; }

    @ServerTimestamp
    public Timestamp getTimestamp() { return mTimestamp; }
    public void setTimestamp(Timestamp timestamp) { mTimestamp = timestamp; }
}
