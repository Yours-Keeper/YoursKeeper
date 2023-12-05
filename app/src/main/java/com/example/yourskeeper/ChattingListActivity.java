package com.example.yourskeeper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayoutStates;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.List;

public class ChattingListActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private AlertDialog customDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_list);

        ImageView btnMenu = findViewById(R.id.btnMenu);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView textTitle = findViewById(R.id.toolbar_Titie);
        textTitle.setText("내 채팅리스트");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);

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
                .where(Filter.or(Filter.equalTo("createdBy", userId), Filter.equalTo("createdFor", userId)))
                //.whereEqualTo("createdBy", userId)
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


                ChattingListHolder viewHolder = new ChattingListHolder(view);

                // RecyclerView 아이템 클릭 이벤트 처리
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 클릭된 항목의 데이터 가져오기
                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            DocumentSnapshot document = getSnapshots().getSnapshot(position);
                            ChattingList cl = document.toObject(ChattingList.class);

                            goToChatRoom(cl.getRoomId());
                            finish();
                        }
                    }
                });


                return viewHolder;
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view); //같아도 되나?
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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

        customDialog = new AlertDialog.Builder( ChattingListActivity.this, R.style.RoundedCornersDialog_signout)
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


    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void goToChatRoom(String roomId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ROOM_ID", roomId); // Pass the room ID to the chat room activity
        startActivity(intent);
    }

    private void goChattingList() {
        Intent intent = new Intent(this, ChattingListActivity.class);
        startActivity(intent);
    }
}