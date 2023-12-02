package com.example.yourskeeper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ChattingListActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_list);

        db = FirebaseFirestore.getInstance(); //위치 맞는지 확인하기
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();




        String userId = currentUser.getUid();

        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("chattingRoom")
                .whereEqualTo("createdBy", userId)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(50);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<ChattingList> options = new FirestoreRecyclerOptions.Builder<ChattingList>()
                .setQuery(query, ChattingList.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChattingList, ChattingListHolder>(options) {
            @Override
            public void onBindViewHolder(ChattingListHolder holder, int position, ChattingList model) {
                // Bind the Chat object to the ChatHolder
                // ...
                holder.bind(model);
            }

            @Override
            public ChattingListHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.chatting_list, group, false);

                return new ChattingListHolder(view);
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view); //같아도 되나?
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}