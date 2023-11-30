package com.example.yourskeeper;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PleaseList extends AppCompatActivity {
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private FirebaseFirestore db;  // Firebase Firestore 객체
    private FirestoreRecyclerAdapter<Store, ListViewHolder> adapter;
    private AlertDialog customDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_please_list);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        ImageView btnBack = findViewById(R.id.btnBack);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView textTitle = findViewById(R.id.toolbar_Titie);
        textTitle.setText("내 근처의 Keeper");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);

        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("storeContent")
                .orderBy("time")
                .limit(50);
        FloatingActionButton goPleaseMainButton = findViewById(R.id.mapBtn);
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
        goPleaseMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPleaseMain();
            }
        });

        FirestoreRecyclerOptions<Store> options = new FirestoreRecyclerOptions.Builder<Store>()
                .setQuery(query, Store.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Store, ListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ListViewHolder holder, int position, Store model) {
                holder.bind(model);
            }

            @Override
            public ListViewHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_item, group, false);
                ListViewHolder viewHolder = new ListViewHolder(view);

                // RecyclerView 아이템 클릭 이벤트 처리
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 클릭된 항목의 데이터 가져오기
                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            DocumentSnapshot document = getSnapshots().getSnapshot(position);
                            Store store = document.toObject(Store.class);

                            // 사용자 지정 다이얼로그 표시
                            showCustomModal( store.getContent());
                        }
                    }
                });

                return viewHolder;
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void showCustomModal(String content) {
        Dialog dialog = new Dialog(this, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.list_detail);

        content = content.replace("\\n", "\n");
        TextView textContent= dialog.findViewById(R.id.list_detail_content);
        textContent.setText(content);
        // Firebase 사용자 정보(userId)를 사용하여 Firestore에서 사용자 정보 가져오기
        ImageView backBtn = dialog.findViewById(R.id.list_detail_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 닫기
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

            // 배경 색상 및 투명도 설정
            window.setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0))); // 흰색 배경, 128은 투명도
            window.setAttributes(layoutParams);
            dialog.findViewById(R.id.list_detail_dialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();

                }
            });
            window.getDecorView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // 다이얼로그 닫기
                    dialog.dismiss();
                    return true;
                }
            });
        }

        dialog.show();
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

    private void goPleaseMain() {
        Intent intent = new Intent(this, PleaseMain_act.class);
        startActivity(intent);
        finish();
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

        customDialog = new AlertDialog.Builder( PleaseList.this, R.style.RoundedCornersDialog_signout)
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
}