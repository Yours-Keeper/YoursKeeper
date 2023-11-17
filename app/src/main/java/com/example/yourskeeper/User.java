package com.example.yourskeeper;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

//Each custom class must have a public constructor that takes no arguments. In addition, the class must include a public getter for each property.
public class User {

    private String nickname;
    @ServerTimestamp private Timestamp timestamp; // server timestamp 이렇게 넣어주면 서버시간은 그냥 들어감

    public User() {}

    public User(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return nickname;
    }

    public Timestamp getTimestamp() { return timestamp; }
}

