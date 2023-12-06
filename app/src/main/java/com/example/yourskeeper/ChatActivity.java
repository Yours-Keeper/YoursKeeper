package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayoutStates;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private AlertDialog customDialog;
    private AlertDialog return_customDialog;
    private  String keeperId;
    private  double keeperLat, keeperLon;
    private boolean isOkButtonPressed = false;

    final int TYPE_MY=0;
    final int TYPE_OTHER=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        ImageView btnMenu = findViewById(R.id.btnMenu);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView textTitle = findViewById(R.id.toolbar_Titie);

        db = FirebaseFirestore.getInstance(); //위치 맞는지 확인하기
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");
        String othersName = intent.getStringExtra("OTHERS_NAME");
        Log.d("kuj", othersName);

        textTitle.setText(othersName);

        ImageButton sendButton = findViewById(R.id.send_Btn);
        ImageButton checkBtn = findViewById(R.id.ckeck_Btn);
        ImageButton plusBtn = findViewById(R.id.plus_Btn);

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
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
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


        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showMenuPopup(v); }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        checkReturnComplete();

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

                checkSide();
            }
        });
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup();
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

    private void showChattingPopup() {
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");
        View popupView = LayoutInflater.from(this).inflate(R.layout.chatting_popup, null);
//        View popupView_okNotPressed = LayoutInflater.from(this).inflate(R.layout.chatting_popup_oknotpressed, null);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//        PopupWindow popupWindow_okNotPressed = new PopupWindow(popupView_okNotPressed, width, height, focusable);

        // 팝업 내 TextView 요소에 대한 참조 가져오기
        TextView nicknamePopupTextView = popupView.findViewById(R.id.nickname_popup);
        Button nicknameLocationButton = popupView.findViewById(R.id.nickname_location_button);
        Button returnPopupButton = popupView.findViewById(R.id.return_popup_button);
        Button bigReturnPopupButton = popupView.findViewById(R.id.big_return_popup_button);
        ImageView locationIcon = popupView.findViewById(R.id.location);
        Chronometer chrono = popupView.findViewById(R.id.time_popup);

        startChrono(chrono);
//        chrono.setBase(SystemClock.elapsedRealtime());
//        chrono.start();

        // Firestore에서 사용자 데이터를 가져와 TextView에 값 설정
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        if (roomId != null) {
            DocumentReference userDocRef = db.collection("chattingRoom").document(roomId);
            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 사용자 데이터 가져오기 및 TextView에 값 설정
                            String opponentName = document.getString("opponentName");
                            String createdBy = document.getString("createdBy");

                            if (opponentName != null) {
                                nicknamePopupTextView.setText(opponentName);
                                nicknameLocationButton.setText(opponentName);
                            }

                            if (userId.equals(createdBy)) {
                                nicknameLocationButton.setVisibility(View.VISIBLE);
                                locationIcon.setVisibility(View.VISIBLE);
                                returnPopupButton.setVisibility(View.VISIBLE);
                                bigReturnPopupButton.setVisibility(View.GONE);

                            } else {
                                // userId와 createdBy가 다르면 기본 가시성 설정
                                nicknameLocationButton.setVisibility(View.GONE);
                                locationIcon.setVisibility(View.GONE);
                                returnPopupButton.setVisibility(View.GONE);
                                bigReturnPopupButton.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(ConstraintLayoutStates.TAG, "문서가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(ConstraintLayoutStates.TAG, "데이터 가져오기 실패: ", task.getException());
                    }
                }
            });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your code to show the popup
                if (!isFinishing()) {
                    popupWindow.showAtLocation(popupView, Gravity.BOTTOM | Gravity.END, 100, 100);
                }
            }
        }, 300);

        nicknameLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showlocationDialog();
            }
        });

        returnPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnDialog(chrono);
            }
        });

        bigReturnPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnDialog(chrono);
            }
        });
    }

    private void showPopup(){
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        DocumentReference roomDocRef = db.collection("chattingRoom").document(roomId);
        roomDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    boolean isOkButtonPressed_PleaseSide = document.getBoolean("isOkButtonPressed_PleaseSide");
                    boolean isOkButtonPressed_StoreSide = document.getBoolean("isOkButtonPressed_StoreSide");

                    // 두 필드가 모두 true이면 popupView 실행, 그렇지 않으면 popupView_okNotPressed 실행
                    if (isOkButtonPressed_PleaseSide && isOkButtonPressed_StoreSide) {
                        showChattingPopup();
                    } else {
                        // 두 필드 중 하나라도 false인 경우
                        showChattingPopupOkNotPressed();
                    }
                }
            } else {
                Log.e(TAG, "Error getting room document", task.getException());
            }
        });
    }

    private void updateOkButtonStatusInFirestore(String roomId, boolean isOkButtonPressed_PleaseSide, boolean isOkButtonPressed_StoreSide) {
        Map<String, Object> data = new HashMap<>();
        data.put("isOkButtonPressed_PleaseSide", isOkButtonPressed_PleaseSide);
        data.put("isOkButtonPressed_StoreSide", isOkButtonPressed_StoreSide);
        data.put("ok_timestamp", FieldValue.serverTimestamp());

        db.collection("chattingRoom").document(roomId)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ok Button 상태가 성공적으로 업데이트되었습니다"))
                .addOnFailureListener(e -> Log.e(TAG, "Ok Button 상태 업데이트 중 오류 발생", e));
    }

    private void checkSide() {
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        DocumentReference roomDocRef = db.collection("chattingRoom").document(roomId);
        roomDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String createdBy = document.getString("createdBy");
                    boolean isOkButtonPressed_PleaseSide = document.getBoolean("isOkButtonPressed_PleaseSide");
                    boolean isOkButtonPressed_StoreSide = document.getBoolean("isOkButtonPressed_StoreSide");

                    if (userId.equals(createdBy)) {
                        // PleaseSide (방을 만든 사용자)
                        if (!isOkButtonPressed_PleaseSide) {
                            // 만약 PleaseSide의 버튼이 눌리지 않았다면, 버튼 상태를 업데이트
                            updateOkButtonStatusInFirestore(roomId, true, isOkButtonPressed_StoreSide);
                        }
                    } else {
                        // StoreSide (방에 초대된 사용자)
                        if (!isOkButtonPressed_StoreSide) {
                            // 만약 StoreSide의 버튼이 눌리지 않았다면, 버튼 상태를 업데이트
                            updateOkButtonStatusInFirestore(roomId, isOkButtonPressed_PleaseSide, true);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error getting room document", task.getException());
            }
        });
    }

    private void showChattingPopupOkNotPressed(){
        View popupView_okNotPressed = LayoutInflater.from(this).inflate(R.layout.chatting_popup_oknotpressed, null);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        PopupWindow popupWindow_okNotPressed = new PopupWindow(popupView_okNotPressed, width, height, focusable);

        popupWindow_okNotPressed.showAtLocation(popupView_okNotPressed, Gravity.BOTTOM | Gravity.END, 100, 100);
    }

    private void checkOkPressed(){
        currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        ImageButton checkBtn = findViewById(R.id.ckeck_Btn);
        ImageButton plusBtn = findViewById(R.id.plus_Btn);

        DocumentReference roomDocRef = db.collection("chattingRoom").document(roomId);
        roomDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String createdBy = document.getString("createdBy");
                    boolean isOkButtonPressed_PleaseSide = document.getBoolean("isOkButtonPressed_PleaseSide");
                    boolean isOkButtonPressed_StoreSide = document.getBoolean("isOkButtonPressed_StoreSide");

                    // 내가 부탁하는 사람이고 체크 버튼을 이전에 눌렀으면 + 버튼이 나와야하고
                    // 내가 부탁하는 사람이고 체크 버튼을 이전에 안눌렀으면 + 버튼이 안나와야하고
                    // 내가 부탁하는 사람이 아니고 체크 버튼을 이전에 눌렀으면 + 버튼이 나와야하고
                    // 내가 부탁하는 사람이 아니고 체크 버튼을 이전에 안눌렀으면 + 버튼이 안나와야함
                    if (userId.equals(createdBy)) {
                        if (isOkButtonPressed_PleaseSide) {
                            checkBtn.setVisibility(View.GONE);
                            plusBtn.setVisibility(View.VISIBLE);
                        }
                        else {
                            checkBtn.setVisibility(View.VISIBLE);
                            plusBtn.setVisibility(View.GONE);
                        }
                    } else {
                        if (isOkButtonPressed_StoreSide) {
                            checkBtn.setVisibility(View.GONE);
                            plusBtn.setVisibility(View.VISIBLE);
                        }
                        else {
                            checkBtn.setVisibility(View.VISIBLE);
                            plusBtn.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                Log.e(TAG, "Error getting room document", task.getException());
            }
        });
    }

    private void startChrono(Chronometer chrono){
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        DocumentReference userDocRef = db.collection("chattingRoom").document(roomId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Firebase에서 가져온 Date 객체 (예시)
                        Date firebaseTimestamp = document.getDate("ok_timestamp");

                        // Date를 milliseconds로 변환
                        long timestampMilliseconds = firebaseTimestamp.getTime();

                        // Chronometer의 시작 시간으로 설정
                        chrono.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timestampMilliseconds));
                        chrono.start();

                    } else {
                        Log.d(ConstraintLayoutStates.TAG, "문서가 존재하지 않습니다.");
                    }
                } else {
                    Log.d(ConstraintLayoutStates.TAG, "데이터 가져오기 실패: ", task.getException());
                }
            }
        });
    }

    private void returnDialog(Chronometer chrono){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.return_dialog, null); // null 자리는 거의 null로만 씀

        return_customDialog = new AlertDialog.Builder(ChatActivity.this, R.style.RoundedCornersDialog_signout)
                .setView(dialogView)
                .create();
        return_customDialog.show();

        ImageView backButton = return_customDialog.findViewById(R.id.return_back);
        Button okButton = return_customDialog.findViewById(R.id.return_ok_button);
        ImageButton plusBtn = findViewById(R.id.plus_Btn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return_customDialog.dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return_customDialog.dismiss();
                chrono.stop();
                updateReliabilityPoint();
                updateReturnComplete();
                finish();
            }
        });
    }

    private void updateReliabilityPoint(){
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        // 아이디랑 createdby랑 같으면 부탁해요 쪽이니까 +0.3 다르면 맡아줄게요 쪽이니까 +1

        DocumentReference userDocRef = db.collection("chattingRoom").document(roomId);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 사용자 데이터 가져오기 및 TextView에 값 설정
                        String createdBy = document.getString("createdBy");

                        if (userId.equals(createdBy)) {
                            addPleaseSidePoint();
                        } else {
                            addStoreSidePoint();
                        }
                    } else {
                        Log.d(ConstraintLayoutStates.TAG, "문서가 존재하지 않습니다.");
                    }
                } else {
                    Log.d(ConstraintLayoutStates.TAG, "데이터 가져오기 실패: ", task.getException());
                }
            }
        });
    }

    private void addPleaseSidePoint() {
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        // users 컬렉션 업데이트
        DocumentReference userDocRef = db.collection("users").document(userId);
        updateReliabilityPoint(userDocRef);

        // storeContent 컬렉션 업데이트
        DocumentReference storeContentDocRef = db.collection("storeContent").document(userId);
        updatePointInStoreContent(storeContentDocRef);
    }

    private void updateReliabilityPoint(DocumentReference userDocRef) {
        userDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Long reliabilityPoint = document.getLong("reliability_point");

                    if (reliabilityPoint != null) {
                        double updatedValue = reliabilityPoint + 1;
                        userDocRef.update("reliability_point", updatedValue)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Reliability point successfully updated for users collection!"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating reliability point for users collection", e));
                    }
                } else {
                    Log.d(ConstraintLayoutStates.TAG, "User document does not exist.");
                }
            } else {
                Log.d(ConstraintLayoutStates.TAG, "Error getting user document: ", task.getException());
            }
        });
    }

    private void updatePointInStoreContent(DocumentReference storeContentDocRef) {
        storeContentDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Long point = document.getLong("point");

                    if (point != null) {
                        double updatedValue = point + 1;
                        storeContentDocRef.update("point", updatedValue)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Point successfully updated for storeContent collection!"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating point for storeContent collection", e));
                    }
                } else {
                    Log.d(ConstraintLayoutStates.TAG, "StoreContent document does not exist.");
                }
            } else {
                Log.d(ConstraintLayoutStates.TAG, "Error getting storeContent document: ", task.getException());
            }
        });
    }

    private void addStoreSidePoint(){
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        // Firestore에서 문서 가져오기
        DocumentReference roomDocRef = db.collection("chattingRoom").document(roomId);

        roomDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot roomDocument = task.getResult();
                if (roomDocument.exists()) {
                    // createdfor 필드의 문자열 가져오기
                    String createdForUserId = roomDocument.getString("createdFor");

                    if (createdForUserId != null) {
                        // users 컬렉션에서 해당하는 사용자 문서 찾기
                        DocumentReference userDocRef = db.collection("users").document(createdForUserId);

                        userDocRef.get().addOnCompleteListener(userTask -> {
                            if (userTask.isSuccessful()) {
                                DocumentSnapshot userDocument = userTask.getResult();
                                if (userDocument.exists()) {
                                    // reliability_point 필드의 현재 값을 가져오기
                                    Long reliabilityPoint = userDocument.getLong("reliability_point");

                                    if (reliabilityPoint != null) {
                                        double updatedValue = reliabilityPoint + 2;

                                        // 새로운 값을 Firestore에 업데이트
                                        userDocRef.update("reliability_point", updatedValue)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Reliability point successfully updated for users collection!");

                                                    // storeContent 컬렉션에서 해당하는 사용자 문서 찾기
                                                    DocumentReference storeContentDocRef = db.collection("storeContent").document(createdForUserId);

                                                    storeContentDocRef.get().addOnCompleteListener(storeContentTask -> {
                                                        if (storeContentTask.isSuccessful()) {
                                                            DocumentSnapshot storeContentDocument = storeContentTask.getResult();
                                                            if (storeContentDocument.exists()) {
                                                                // point 필드의 현재 값을 가져오기
                                                                Long point = storeContentDocument.getLong("point");

                                                                if (point != null) {
                                                                    // point 값에 2 더하기 (혹은 원하는 만큼 더하기)
                                                                    double updatedPoint = point + 2;

                                                                    // 새로운 값을 Firestore에 업데이트
                                                                    storeContentDocRef.update("point", updatedPoint)
                                                                            .addOnSuccessListener(aVoid1 -> Log.d(TAG, "Point successfully updated for storeContent collection!"))
                                                                            .addOnFailureListener(e -> Log.e(TAG, "Error updating point for storeContent collection", e));
                                                                } else {
                                                                    // point 필드가 존재하지 않을 때의 처리
                                                                }
                                                            } else {
                                                                // storeContent 문서가 존재하지 않을 때의 처리
                                                            }
                                                        } else {
                                                            // storeContent 문서 가져오기 실패 시의 처리
                                                            Log.e(TAG, "Error getting storeContent document: ", storeContentTask.getException());
                                                        }
                                                    });
                                                })
                                                .addOnFailureListener(e -> Log.e(TAG, "Error updating reliability point", e));
                                    } else {
                                        // reliability_point 필드가 존재하지 않을 때의 처리
                                    }
                                } else {
                                    // 사용자 문서가 존재하지 않을 때의 처리
                                }
                            } else {
                                // 사용자 문서 가져오기 실패 시의 처리
                                Log.e(TAG, "Error getting user document: ", userTask.getException());
                            }
                        });
                    } else {
                        // createdfor 필드가 존재하지 않을 때의 처리
                    }
                } else {
                    // 방 문서가 존재하지 않을 때의 처리
                }
            } else {
                // 방 문서 가져오기 실패 시의 처리
                Log.e(TAG, "Error getting room document: ", task.getException());
            }
        });
    }

    private void updateReturnComplete(){
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        Map<String, Object> data = new HashMap<>();
        data.put("return_complete", true);

        db.collection("chattingRoom").document(roomId)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ok Button 상태가 성공적으로 업데이트되었습니다"))
                .addOnFailureListener(e -> Log.e(TAG, "Ok Button 상태 업데이트 중 오류 발생", e));
    }

    private void checkReturnComplete(){
        checkOkPressed();
        Intent intent = getIntent();
        String roomId = intent.getStringExtra("ROOM_ID");

        ImageButton checkBtn = findViewById(R.id.ckeck_Btn);
        ImageButton plusBtn = findViewById(R.id.plus_Btn);

        DocumentReference roomDocRef = db.collection("chattingRoom").document(roomId);
        roomDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String createdBy = document.getString("createdBy");
                    boolean return_complete = document.getBoolean("return_complete");

                    if (return_complete) {
                        checkBtn.setVisibility(View.VISIBLE);
                        plusBtn.setVisibility(View.GONE);
                    }
                }
            } else {
                Log.e(TAG, "Error getting room document", task.getException());
            }
        });

        Map<String, Object> data = new HashMap<>();
        data.put("isOkButtonPressed_PleaseSide", false);
        data.put("isOkButtonPressed_StoreSide", false);
        data.put("ok_timestamp", FieldValue.serverTimestamp());

        db.collection("chattingRoom").document(roomId)
                .update(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Ok Button 상태가 성공적으로 업데이트되었습니다"))
                .addOnFailureListener(e -> Log.e(TAG, "Ok Button 상태 업데이트 중 오류 발생", e));
    }



    private void showMenuPopup(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.menu, null);
        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // 팝업 내 TextView 요소에 대한 참조 가져오기
        TextView nicknameTextView = popupView.findViewById(R.id.nickname);
        TextView reliabilityPointTextView = popupView.findViewById(R.id.reliability_point);
        TextView logoutTextView = popupView.findViewById(R.id.menu_signout);
        TextView myChattingList = popupView.findViewById(R.id.my_chatting);

        // Firestore에서 사용자 데이터를 가져와 TextView에 값 설정
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 사용자 데이터 가져오기 및 TextView에 값 설정
                            String nicknames = document.getString("nickname");
                            Long reliabilityPoint = document.getLong("reliability_point");

                            if (nicknames != null) {
                                nicknameTextView.setText(nicknames);
                            }

                            if (reliabilityPoint != null) {
                                reliabilityPointTextView.setText(String.valueOf(reliabilityPoint) + "점");
                            }
                        } else {
                            Log.d(ConstraintLayoutStates.TAG, "문서가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(ConstraintLayoutStates.TAG, "데이터 가져오기 실패: ", task.getException());
                    }
                }
            });
        }

        myChattingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goChattingList();
            }
        });

        // logoutTextView에 대한 onClickListener 설정
        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그아웃 액션 처리
                showSignoutDialog();
                // 필요에 따라 추가적인 로그아웃 로직을 추가할 수 있습니다.
                // 예를 들어, 사용자를 로그인 페이지로 리디렉션할 수 있습니다.
            }
        });

        // 팝업 창 표시
        popupWindow.showAtLocation(popupView, Gravity.TOP | Gravity.END, 30, 100);
    }

    private void showSignoutDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.signout_dialog, null); // null 자리는 거의 null로만 씀

        customDialog = new AlertDialog.Builder( ChatActivity.this, R.style.RoundedCornersDialog_signout)
                .setView(dialogView)
                .create();

        customDialog.show();

        ImageView back = customDialog.findViewById(R.id.sign_out_back);
        Button signOutButton = customDialog.findViewById(R.id.sign_out_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // db에 store 정보를 삭제해야함
                signOut();
            }
        });
    }

    private void goChattingList() {
        Intent intent = new Intent(this, ChattingListActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}