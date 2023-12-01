package com.example.yourskeeper;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Chatting extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체

    final int TYPE_MY=0;
    final int TYPE_OTHER=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        db = FirebaseFirestore.getInstance(); //위치 맞는지 확인하기
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

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
            public int getItemViewType(int position) {
                Chat chat = getItem(position);
                if(chat != null && chat.getUid().equals(userId)) {
                    //내가 쓴 글
                    return TYPE_MY;
                } else {
                    return TYPE_OTHER;
                }
            }

            @NonNull
            @Override
            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                //두 레이아웃 중 뭘 넣어야할지 몰라 우선 null 참조
                //파이어베이스에 저장된 name이 내 static name에 있는 것과 같으면 내거 아님 상대방거임
                //두번째 파라미터 int viewType을 사용해서 분기처리 해보자
                //타입은 낸 맘대로 정할 수 있음
                View itemView = null;
                if(viewType == TYPE_MY) itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mychat,parent,false);
                else itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat,parent,false);

                //카톡 날짜 구분선도 이 타입으로 구분한것임

                return new ChatHolder(itemView);
            }

//            @Override
//            public ChatHolder onCreateViewHolder(ViewGroup group, int i) {
//                // Create a new instance of the ViewHolder, in this case we are using a custom
//                // layout called R.layout.message for each item
//
////                DocumentReference chatsdocRef = db.collection("chats").document(userId);
////                chatsdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
////                    @Override
////                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
////                        if (task.isSuccessful()) {
////                            DocumentSnapshot document = task.getResult();
////                            if (document.exists()) {
////                                // 문서(document)에서 필요한 데이터를 가져옴
////                                String uid = document.getString("uid");
////
////                                // 가져온 데이터로 작업 수행
////                            } else {
////                                // 문서가 존재하지 않음
////                            }
////                        } else {
////                            // 데이터 가져오기 실패 시 처리
////                        }
////                    }
////                });
//
//                //if() {
//                View view = LayoutInflater.from(group.getContext())
//                        .inflate(R.layout.chat, group, false); //사용자 지정 chat 레이아웃 적용
//
//                return new ChatHolder(view);
//                //}
//            }
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
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"+calendar.get(Calendar.MINUTE);
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
                        data.put("time", time);
                        data.put("timestamp", FieldValue.serverTimestamp());
                        //chats.document("MSG_").set(data);
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

//        DocumentReference chatsdocRef = db.collection("chats").document(userId);
//        chatsdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        // 문서(document)에서 필요한 데이터를 가져옴
//                        Timestamp timestamp = document.getTimestamp("timestamp");
//
//                        // timestamp를 원하는 형식으로 변환하여 표시 (예: "14:30")
//                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
//                        String formattedDate = sdf.format(timestamp.toDate());
//                        // 가져온 데이터로 작업 수행
//                    } else {
//                        // 문서가 존재하지 않음
//                    }
//                } else {
//                    // 데이터 가져오기 실패 시 처리
//                }
//            }
//        });

//        Intent intent = getIntent();
//        String userId = intent.getStringExtra("USER_ID");

//        data.put("message", text);
//        data.put("uid", userId);
//        data.put("timestamp", FieldValue.serverTimestamp());
//        chats.document("MSG_"+ System.currentTimeMillis()).set(data);
    }



}