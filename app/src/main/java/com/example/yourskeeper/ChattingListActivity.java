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

import java.util.Arrays;
import java.util.List;

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


//        Query queryCreatedBy = FirebaseFirestore.getInstance()
//                .collection("chattingRoom")
//                .whereEqualTo("createdBy", userId);
//
//        Query queryCreatedFor = FirebaseFirestore.getInstance()
//                .collection("chattingRoom")
//                .whereEqualTo("createdFor", userId);
//
//// Combine both queries locally in your app
//        List<Query> queries = Arrays.asList(queryCreatedBy, queryCreatedFor);
//
//        FirestoreRecyclerOptions<ChattingList> options = new FirestoreRecyclerOptions.Builder<ChattingList>()
//                .setQuery(mergeQueries(queries), ChattingList.class)
//                .build();



        Query query = FirebaseFirestore.getInstance()
                .collection("chattingRoom")
                .whereEqualTo("createdBy", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<ChattingList> options = new FirestoreRecyclerOptions.Builder<ChattingList>()
                .setQuery(query, ChattingList.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChattingList, ChattingListHolder>(options) {
//            @Override
//            public void onBindViewHolder(ChattingListHolder holder, int position, ChattingList model) {
//                // Bind the Chat object to the ChatHolder
//                // ...
//                holder.bind(model);
//            }

            //채팅리스트에 상대방 닉네임 표시를 위한 처리
            @Override
            public void onBindViewHolder(ChattingListHolder holder, int position, ChattingList model) {
                // Fetch names of users based on their IDs from Firestore
                String otherUserId = model.getCreatedBy().equals(userId) ? model.getCreatedFor() : model.getCreatedBy();

                FirebaseFirestore.getInstance().collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String otherPersonNickname = documentSnapshot.getString("nickname");
                                if (otherPersonNickname != null) {
                                    // Update ChattingList object with the other person's name
                                    model.setName(otherPersonNickname);
                                    // Bind the updated model to the ChatHolder
                                    holder.bind(model);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                        });
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

    private Query mergeQueries(List<Query> queries) {
        Query result = null;
        for (Query query : queries) {
            if (result == null) {
                result = query;
            } else {
                // Combine the queries with a logical OR
                result = result.startAfter(query);
            }
        }
        return result;
    }
}