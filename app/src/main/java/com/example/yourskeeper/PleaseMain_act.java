package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayoutStates;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
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




import java.util.HashMap;
import java.util.Map;

public class PleaseMain_act extends AppCompatActivity
        implements OnMapReadyCallback {
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private FirebaseFirestore db;  // Firebase Firestore 객체
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;
    private double lat, lon;
    private AlertDialog customDialog;

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
        ImageView btnMenu = findViewById(R.id.btnMenu);
        ImageView btnBack = findViewById(R.id.btnBack);
        mAuth = FirebaseAuth.getInstance();
        TextView textTitle = findViewById(R.id.toolbar_Titie);
        textTitle.setText("내 근처의 Keeper");

        // Firebase Firestore 객체 초기화
        db = FirebaseFirestore.getInstance();
        FloatingActionButton goPleaseListButton = findViewById(R.id.fabMenu);
        // ...
        goPleaseListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goPleaseList();
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
        naverMap.setMinZoom(14.0); // 원하는 값으로 조절 가능
        naverMap.setMaxZoom(17.0); // 원하는 값으로 조절 가능
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
                Map<String, Object> data = new HashMap<>();

                Marker myLocationMarker = new Marker();
                myLocationMarker.setPosition(new LatLng(lat, lon));
                myLocationMarker.setIconTintColor(Color.parseColor("#FFFF00")); // 노란색 틴트
                myLocationMarker.setWidth(50);
                myLocationMarker.setHeight(50);
                myLocationMarker.setMap(mNaverMap);
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);


                if (userId != null) {

                    db.collection("storeContent").get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        if (document.exists()) {
                                            double userLat = document.getDouble("lat");
                                            double userLon = document.getDouble("lon");
                                            String nickName = document.getString("nickname");
                                            String userTime = document.getString("time");
                                            String content = document.getString("content");
                                            long point = document.getLong("point");

                                            // 닉네임이 userNICK과 같지 않은 경우에만 마커 추가

                                                Marker marker = new Marker();
                                                marker.setPosition(new LatLng(userLat, userLon));
                                                marker.setMap(naverMap);
                                                float distanceToMarker = location.distanceTo(new Location("Marker") {{
                                                    setLatitude(userLat);
                                                    setLongitude(userLon);
                                                }});
                                                data.put("distance", distanceToMarker);

                                                // Firestore에서 거리 업데이트
                                                db.collection("storeContent").document(document.getId()).update(data);

                                                // 클릭 이벤트 처리
                                                marker.setOnClickListener(overlay -> {
                                                    showCustomModal("마커 정보", nickName, distanceToMarker, userId, userTime, content, point);
                                                    return true;
                                                });

                                            // Add a marker for each document in the "storeContent" collection


                                            // Calculate distance to each marker

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


            }
        });
    }

    private void showCustomModal(String title, String nickname, float distance, String userId, String userTime, String content, long point) {
        Dialog dialog = new Dialog(this, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.dialog_custom);
        TextView textTitle = dialog.findViewById(R.id.modalTitle);
        TextView textTime = dialog.findViewById(R.id.modalTime);
        TextView modalDistance = dialog.findViewById(R.id.modalDistance);
        TextView modalScore = dialog.findViewById(R.id.modalScore);
        textTitle.setText(nickname);
        modalScore.setText(String.valueOf(point));
        textTime.setText(userTime);
        modalDistance.setText(String.format("%.0f m", distance));

        dialog.findViewById(R.id.dialog_custom_root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close current dialog
                showCustomListModal(content); // Deliver appropriate content
            }
        });

        // Firebase 사용자 정보(userId)를 사용하여 Firestore에서 사용자 정보 가져오기
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());

            layoutParams.y = 200; // 20픽셀 위에 위치
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setBackgroundDrawable(new ColorDrawable(Color.argb(128, 0, 0, 0))); // 흰색 배경, 128은 투명도
            window.setAttributes(layoutParams);
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

    private void showCustomListModal(String content) {
        Dialog dialog = new Dialog(this, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.list_detail);

        content = content.replace("\\n", "\n");
        TextView textContent = dialog.findViewById(R.id.list_detail_content);
        textContent.setText(content);
        ImageView backBtn = dialog.findViewById(R.id.list_detail_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다이얼로그 닫기
                dialog.dismiss();
            }
        });

        // Firebase 사용자 정보(userId)를 사용하여 Firestore에서 사용자 정보 가져오기
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.gravity = Gravity.CENTER;
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

    private void goPleaseList() {

        Intent intent = new Intent(this, PleaseList.class);
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

        customDialog = new AlertDialog.Builder(PleaseMain_act.this, R.style.RoundedCornersDialog_signout)
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

