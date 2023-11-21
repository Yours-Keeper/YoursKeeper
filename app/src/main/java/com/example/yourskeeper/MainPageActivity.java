package com.example.yourskeeper;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.overlay.Marker;

import java.util.Map;

public class MainPageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Button pleaseButton = findViewById(R.id.please_Btn);
        Button storeButton = findViewById(R.id.store_Btn);
        ImageView mainPageBtnMenu = findViewById(R.id.mainPage_btnMenu);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mainPageBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopup(v);
            }
        });


        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
        pleaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                goPlease(user);
            }
        });
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                goStore(user);
            }
        });
    }
    private void goPlease(FirebaseUser user) {
        String userId = user.getUid(); // 사용자 ID 가져오기
        Intent intent = new Intent(this, PleaseMain_act.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
    private void goStore(FirebaseUser user) {
        String userId = user.getUid(); // 사용자 ID 가져오기
        Intent intent = new Intent(this, StoreMainPage.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
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
                            String nickname = document.getString("nickname");
                            Long reliabilityPoint = document.getLong("reliability_point");

                            if (nickname != null) {
                                nicknameTextView.setText(nickname);
                            }

                            if (reliabilityPoint != null) {
                                reliabilityPointTextView.setText(String.valueOf(reliabilityPoint) + "점");
                            }
                        } else {
                            Log.d(TAG, "문서가 존재하지 않습니다.");
                        }
                    } else {
                        Log.d(TAG, "데이터 가져오기 실패: ", task.getException());
                    }
                }
            });
        }

        // logoutTextView에 대한 onClickListener 설정
        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그아웃 액션 처리
                signOut();
                // 필요에 따라 추가적인 로그아웃 로직을 추가할 수 있습니다.
                // 예를 들어, 사용자를 로그인 페이지로 리디렉션할 수 있습니다.
            }
        });

        // 팝업 창 표시
        popupWindow.showAtLocation(popupView, Gravity.TOP | Gravity.END, 30, 100);
    }
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}