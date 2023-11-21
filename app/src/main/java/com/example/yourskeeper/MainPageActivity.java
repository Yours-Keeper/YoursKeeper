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

        mainPageBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopup();
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

    private void showMenuPopup() {
        // 사용자 정의 레이아웃을 인플레이트합니다.
        View popupView = getLayoutInflater().inflate(R.layout.menu, null);

        // PopupWindow를 생성합니다.
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // 투명성 문제를 방지하기 위해 단색의 배경 드로어블을 설정합니다.
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // 포커스 가능하게 설정하고 팝업을 화면 중앙에 표시합니다.
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.TOP | Gravity.END, 30, 100);

        // 팝업 외부의 뷰를 클릭하면 팝업을 닫습니다.
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        // 사용자 정의 레이아웃 내부의 뷰에 액세스하고 데이터 또는 동작을 설정합니다.

//        TextView nicknameTextView = popupView.findViewById(R.id.nickname);
//        nicknameTextView.setText("nickname");
    }
}