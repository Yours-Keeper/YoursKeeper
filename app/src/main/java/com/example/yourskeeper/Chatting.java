package com.example.yourskeeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class Chatting extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        db = FirebaseFirestore.getInstance(); //위치 맞는지 확인하기

        ImageButton sendButton = findViewById(R.id.send_Btn);

        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("chats")
                .orderBy("timestamp")
                .limit(50);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Chat, ChatHolder>(options) {
            @Override
            public void onBindViewHolder(ChatHolder holder, int position, Chat model) {
                // Bind the Chat object to the ChatHolder
                // ...
                holder.bind(model);
            }

            @Override
            public ChatHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(group.getContext())
                        .inflate(android.R.layout.simple_list_item_2, group, false);

                return new ChatHolder(view);
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addData();
            }
        });
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

    private void addData() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = String.valueOf(currentUser); //자동완성함 주의 요망
        EditText chatText = findViewById(R.id.chat_text);

        CollectionReference chats = db.collection("chats");
        Map<String, Object> data = new HashMap<>();
        String text = chatText.getText().toString();
        CollectionReference collectionRef = db.collection("users"); //확인해보기 안된다면 컬렉션의 필드 값 어떻게 가져오는지 다시 찾기
        collectionRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object nicknameFromDB = documentSnapshot.get("nickname");
                        // 필드 값(value)을 사용하여 작업 수행
                        data.put("name", nicknameFromDB);
                    } else {
                        // 해당 문서가 존재하지 않음
                    }
                })
                .addOnFailureListener(e -> {
                    // 실패 시 처리
                });

//        Intent intent = getIntent();
//        String userId = intent.getStringExtra("USER_ID");

        //data.put("name", );
        data.put("message", text);
        data.put("uid", userId);
        data.put("timestamp", FieldValue.serverTimestamp());
        chats.document("MSG_"+ System.currentTimeMillis()).set(data);

    }
}