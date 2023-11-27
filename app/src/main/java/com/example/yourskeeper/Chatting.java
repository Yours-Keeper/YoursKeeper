package com.example.yourskeeper;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
        String userId = currentUser.getUid();

//        String userName = currentUser.getDisplayName();
        EditText chatText = findViewById(R.id.chat_text);

        CollectionReference chats = db.collection("chats");
        Map<String, Object> data = new HashMap<>();
        String text = chatText.getText().toString();
        DocumentReference userdocRef = db.collection("users").document(userId);
        userdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 문서(document)에서 필요한 데이터를 가져옴
                        String name = document.getString("nickname");
                        data.put("name", name);
                        data.put("message", text);
                        data.put("uid", userId);
                        data.put("timestamp", FieldValue.serverTimestamp());
                        chats.document("MSG_"+ System.currentTimeMillis()).set(data);
                        // 가져온 데이터로 작업 수행
                    } else {
                        // 문서가 존재하지 않음
                    }
                } else {
                    // 데이터 가져오기 실패 시 처리
                }
            }
        });

//        Intent intent = getIntent();
//        String userId = intent.getStringExtra("USER_ID");

//        data.put("message", text);
//        data.put("uid", userId);
//        data.put("timestamp", FieldValue.serverTimestamp());
//        chats.document("MSG_"+ System.currentTimeMillis()).set(data);
    }
}