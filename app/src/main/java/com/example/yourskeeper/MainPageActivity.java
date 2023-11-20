package com.example.yourskeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.naver.maps.map.LocationTrackingMode;

public class MainPageActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
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

}