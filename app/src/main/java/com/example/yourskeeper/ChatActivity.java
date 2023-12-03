package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private AlertDialog customDialog;
    private  String keeperId;
    private  double keeperLat, keeperLon;

    final int TYPE_MY=0;
    final int TYPE_OTHER=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);
        ImageButton checkBtn = findViewById(R.id.ckeck_Btn);

        db = FirebaseFirestore.getInstance(); //위치 맞는지 확인하기
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        ImageButton sendButton = findViewById(R.id.send_Btn);

        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("chattingRoom")
                .document(roomId)
                .collection("chats")
                .orderBy("timestamp")
                .limit(50);

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Chat.class instructs the adapter to convert each DocumentSnapshot to a Chat object
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query, Chat.class)
                .build();
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialog();
            }
        });

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
                    //내가 쓴 채팅
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
                if(viewType == TYPE_MY) itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mychat, parent,false);
                else itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat, parent,false);

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
    private void startDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.start_keep, null); // null 자리는 거의 null로만 씀
        Intent intent = getIntent();
        ImageButton changeBtn = findViewById(R.id.ckeck_Btn);
        ImageButton plusBtn = findViewById(R.id.plus_Btn);
        String roomId = intent.getStringExtra("ROOM_ID");

        customDialog = new AlertDialog.Builder(ChatActivity.this, R.style.RoundedCornersDialog_signout)
                .setView(dialogView)
                .create();

        customDialog.show();
        ImageView backButton = customDialog.findViewById(R.id.start_back);
        Button okButton = customDialog.findViewById(R.id.start_button);
        db.collection("chattingRoom").document(roomId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            keeperLat = document.getDouble("keeperLat");
                            keeperLon = document.getDouble("keeperLon");
                        } else {
                            // Firestore 문서가 없거나 null인 경우
                            Log.d(TAG, "문서 존재하지 않음");
                        }
                    } else {
                        // 작업이 예외와 함께 실패한 경우
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e(TAG, "문서 가져오기 오류", exception);
                        }
                    }
                });



        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                changeBtn.setVisibility(View.GONE);
                plusBtn.setVisibility(View.VISIBLE);
            }
        });
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showlocationDialog();
            }
        });
    }
    private void showlocationDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.keeper_location, null); // null 자리는 거의 null로만 씀
        Intent intent = getIntent();

        String roomId = intent.getStringExtra("ROOM_ID");
        customDialog = new AlertDialog.Builder(ChatActivity.this, R.style.RoundedCornersDialog_signout)
                .setView(dialogView)
                .create();

        customDialog.show();
        TextView locationText = customDialog.findViewById(R.id.location_text);
        Button okButton = customDialog.findViewById(R.id.location_Btn);
        db.collection("chattingRoom").document(roomId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            keeperId = document.getString("createdFor");

                            db.collection("storeContent").document(keeperId).get()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot locationDocument = task1.getResult();
                                            if (locationDocument != null && locationDocument.exists()) {
                                                double currLat = locationDocument.getDouble("lat");
                                                double currLon = locationDocument.getDouble("lon");

                                                // 검색된 값으로 locationText를 업데이트합니다.

                                                double latDifference = Math.abs(keeperLat - currLat);
                                                double lonDifference = Math.abs(keeperLon - currLon);

                                                // 위치 차이가 0.003 이상인 경우에 대한 처리
                                                if (latDifference >= 0.001 || lonDifference >= 0.001) {
                                                    // 위치가 변경된 경우에 대한 처리
                                                    String locationString = "위도: " + currLat + "\n경도: " + currLon;
                                                    locationText.setText("Keeper가 \n" +
                                                            "원래 위치에서 벗어났어요!");
                                                } else {
                                                    // 위치가 변경되지 않은 경우에 대한 처리
                                                    locationText.setText("Keeper가 \n" +
                                                            "원래 위치에 있어요");
                                                }
                                            } else {
                                                // Firestore 문서가 없거나 null인 경우
                                                Log.d(TAG, "문서 존재하지 않음");
                                            }
                                        } else {
                                            // 작업이 예외와 함께 실패한 경우
                                            Exception exception = task1.getException();
                                            if (exception != null) {
                                                Log.e(TAG, "문서 가져오기 오류", exception);
                                            }
                                        }
                                    });
                        } else {
                            // Firestore 문서가 없거나 null인 경우
                            Log.d(TAG, "문서 존재하지 않음");
                        }
                    } else {
                        // 작업이 예외와 함께 실패한 경우
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e(TAG, "문서 가져오기 오류", exception);
                        }
                    }
                });


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
    }

    private void addData() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

//        String userName = currentUser.getDisplayName();
        EditText chatText = findViewById(R.id.chat_text);

        String text = chatText.getText().toString();
        Calendar calendar = Calendar.getInstance(); //시간표시
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"+calendar.get(Calendar.MINUTE);

        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        Map<String, Object> data = new HashMap<>();


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
                        //chats.document("MSG_"+ System.currentTimeMillis()).set(data);

                        db.collection("chattingRoom")
                                .document(roomId)
                                .collection("chats")
                                .add(data);
                        // 가져온 데이터로 작업 수행
                    } else {
                        // 문서가 존재하지 않음
                    }
                } else {
                    // 데이터 가져오기 실패 시 처리
                }
            }
        });

//        CollectionReference chats = db.collection("chats");
//        Map<String, Object> data = new HashMap<>();
//
//        String text = chatText.getText().toString();
//        Calendar calendar = Calendar.getInstance(); //시간표시
//        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":"+calendar.get(Calendar.MINUTE);
//
//        DocumentReference userdocRef = db.collection("users").document(userId);
//        userdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        // 문서(document)에서 필요한 데이터를 가져옴
//                        String name = document.getString("nickname");
//
//                        data.put("name", name);
//                        data.put("message", text);
//                        data.put("uid", userId);
//                        data.put("time", time);
//                        data.put("timestamp", FieldValue.serverTimestamp());
//                        //chats.document("MSG_").set(data);
//                        chats.document("MSG_"+ System.currentTimeMillis()).set(data);
//
//                        // 가져온 데이터로 작업 수행
//                    } else {
//                        // 문서가 존재하지 않음
//                    }
//                } else {
//                    // 데이터 가져오기 실패 시 처리
//                }
//            }
//        });

        EditText et = findViewById(R.id.chat_text);
        et.setText("");  //입력란 초기화
        int itemCount = adapter.getItemCount();
        if (itemCount > 0) {
            RecyclerView recyclerView = findViewById(R.id.recycler_view);
            recyclerView.scrollToPosition(itemCount - 1);
        } //리사이클러뷰 가장 밑으로

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


//        Intent intent = new Intent(this, PleaseMain_act.class);
//        intent.putExtra("USER_ID", userId);
//        startActivity(intent);

//        Intent intent = getIntent();
//        String roomId = intent.getStringExtra("ROOM_ID");

//        data.put("message", text);
//        data.put("uid", userId);
//        data.put("timestamp", FieldValue.serverTimestamp());
//        chats.document("MSG_"+ System.currentTimeMillis()).set(data);
    }

}