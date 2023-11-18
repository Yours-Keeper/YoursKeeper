package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TotpSecret;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class PleaseMain_act extends AppCompatActivity
        implements OnMapReadyCallback {
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private FirebaseFirestore db;  // Firebase Firestore 객체
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    private double lat, lon;
    private static final int PERMISSION_REQUEST_CODE = 1000;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_please_main);
        // Firebase 인증 객체 초기화
        mAuth = FirebaseAuth.getInstance();

        // Firebase Firestore 객체 초기화
        db = FirebaseFirestore.getInstance();

        // ...

        // 위 코드 아래에 Firebase 사용자 정보 가져오기
        currentUser = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        setSupportActionBar(toolbar);
        // 지도 객체 생성하기
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }

        // getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음.
        mapFragment.getMapAsync(this);

        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource);
        String userId = currentUser != null ? currentUser.getUid() : null;

        CircleOverlay incircle = new CircleOverlay();
        CircleOverlay outcircle = new CircleOverlay();
        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                incircle.setOutlineWidth(7);
                incircle.setOutlineColor(Color.YELLOW);
                incircle.setCenter(new LatLng(lat, lon));
                incircle.setRadius(100);
                incircle.setMap(mNaverMap);
                incircle.setColor(Color.argb(0, 0, 0, 0));

                outcircle.setOutlineWidth(7);
                outcircle.setOutlineColor(Color.YELLOW);
                outcircle.setCenter(new LatLng(lat, lon));
                outcircle.setRadius(200);
                outcircle.setMap(mNaverMap);
                outcircle.setColor(Color.argb(0, 0, 0, 0));

                if (userId != null) {
                    db.collection("storeContent").get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        if (document.exists()) {
                                            double userLat = document.getDouble("lat");
                                            double userLon = document.getDouble("lon");

                                            // Add a marker for each document in the "storeContent" collection
                                            Marker marker = new Marker();
                                            marker.setPosition(new LatLng(userLat, userLon));
                                            marker.setMap(naverMap);

                                            // Handle click event
                                            marker.setOnClickListener(overlay -> {
                                                float distanceToMarker = location.distanceTo(new Location("Marker") {{
                                                    setLatitude(userLat);
                                                    setLongitude(userLon);
                                                }});
                                                showCustomModal("Marker information", "", distanceToMarker, userId);
                                                return true;
                                            });
                                        } else {
                                            // If the Firestore document does not exist
                                            Log.d(TAG, "Document does not exist");
                                        }
                                    }
                                } else {
                                    // If the operation fails with an exception
                                    Exception exception = task.getException();
                                    if (exception != null) {
                                        Log.e(TAG, "Error retrieving documents", exception);
                                    }
                                }
                            });
                }

                Marker myLocationMarker = new Marker();
                myLocationMarker.setPosition(new LatLng(lat, lon));
                myLocationMarker.setIconTintColor(Color.parseColor("#FFFF00")); // 노란색 틴트
                myLocationMarker.setWidth(50);
                myLocationMarker.setHeight(50);
                myLocationMarker.setMap(mNaverMap);
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
                Toast.makeText(getApplicationContext(),
                        lat + ", " + lon, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomModal(String title, String content, float distance, String userId) {
        Dialog dialog = new Dialog(this, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.dialog_custom);

        TextView textModalContent = dialog.findViewById(R.id.modalTitle);
        TextView textTime = dialog.findViewById(R.id.modalTime);
        TextView modalDistance = dialog.findViewById(R.id.modalDistance);

        textModalContent.setText(content);


        modalDistance.setText(String.format("거리: %.0f 미터", distance));

        // Firebase 사용자 정보(userId)를 사용하여 Firestore에서 사용자 정보 가져오기
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String userNickname = document.getString("name");
                                // 사용자의 닉네임으로 모달 텍스트 설정
                                textModalContent.setText(userNickname);
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
            db.collection("storeContent").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String userTime = document.getString("time");
                                // 사용자의 닉네임으로 모달 텍스트 설정
                                textTime.setText(userTime);
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
        }

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            layoutParams.y = 200; // 20픽셀 위에 위치

            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            window.setAttributes(layoutParams);
        }

        dialog.show();
    }

}