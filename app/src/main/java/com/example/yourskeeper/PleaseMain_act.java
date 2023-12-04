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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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


import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PleaseMain_act extends AppCompatActivity
        implements OnMapReadyCallback {
    private FirebaseAuth mAuth;  // Firebase 인증 객체
    private FirebaseUser currentUser;  // 현재 Firebase 사용자 객체
    private FirebaseFirestore db;  // Firebase Firestore 객체
    private FusedLocationSource locationSource;
    private NaverMap mNaverMap;

    private String nickNAME;
    private String chat;
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

        currentUser = mAuth.getCurrentUser();

        String userId = currentUser != null ? currentUser.getUid() : null;
        // 권한 확인, 결과는 onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        naverMap.addOnLocationChangeListener(new NaverMap.OnLocationChangeListener() {
            @Override
            public void onLocationChange(@NonNull Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                Map<String, Object> data = new HashMap<>();
                CircleOverlay circle = new CircleOverlay();
                circle.setCenter(new LatLng(lat, lon));
                circle.setRadius(0); // 원의 반지름을 미터로 설정하십시오. 필요에 따라 조절하십시오

// 위치 추적 모드를 NoFollow로 설정합니다
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);

// 원을 지도에 추가합니다
                circle.setMap(mNaverMap);

                if (userId != null) {
                    db.collection("users").document(userId).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null && document.exists()) {
                                        nickNAME = document.getString("nickname");

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


                                            marker.setOnClickListener(overlay -> {
                                                if(nickName.equals(nickNAME)){
                                                    chat = "내 채팅 목록으로 가기";
                                                }else{
                                                    chat="채팅하기";
                                                }
                                                showCustomModal("마커 정보", nickName, distanceToMarker, userId, userTime, content, point, chat);
                                                return true;
                                            });

                                                // 클릭 이벤트 처리


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


    private void showCustomModal(String title, String nickname, float distance, String userId, String userTime, String content, long point, String chat) {
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
                showCustomListModal(content, chat, nickname, userId); // Deliver appropriate content
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

    private void showCustomListModal(String content, String chat, String nickname, String uid) {
        Dialog dialog = new Dialog(this, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.list_detail);


        content = content.replace("\\n", "\n");
        TextView textContent = dialog.findViewById(R.id.list_detail_content);
        textContent.setText(content);
        Button chatBtn = dialog.findViewById(R.id.list_detail_Btn);

        if(chat.equals("내 채팅 목록으로 가기")){
            chatBtn.setText(chat);
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goChatList();
                    finish();
                }
            });
        }


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
    private void goChatList() {
        Intent intent = new Intent(this, ChattingListActivity.class);
        startActivity(intent);
    }
    private void goChattingList() {
        Intent intent = new Intent(this, ChattingListActivity.class);
        startActivity(intent);
    }
    private void goToChatRoom(String roomId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ROOM_ID", roomId); // Pass the room ID to the chat room activity
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


    private void createChatRoom(String opponentUid, String opponentNickname) {
        String currentUserUid = mAuth.getCurrentUser().getUid();


        DocumentReference userDocRef = db.collection("storeContent").document(currentUserUid);
        userDocRef.get().addOnCompleteListener(uidtask -> {
            if (uidtask.isSuccessful()) {
                DocumentSnapshot document = uidtask.getResult();
                if (document.exists()) {
                    // Access the "nickname" field value
                    String nickname = document.getString("nickname");
                    double keeperLat= document.getDouble("lat");
                    double keeperLon= document.getDouble("lon");

                    // Sort the UIDs alphabetically to ensure consistency in generating the chat room ID
                    String[] userIds = {currentUserUid, opponentUid};
                    Arrays.sort(userIds);

                    String chatRoomId = userIds[0] + "_" + userIds[1]; // Unique chat room ID

                    // Check if the chat room already exists
                    db.collection("chattingRoom")
                            .document(chatRoomId)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()) {
                                        // Chat room already exists between these users
                                        goToChatRoom(chatRoomId);
                                        finish();
                                    } else {
                                        // Create a new chat room if it doesn't exist
                                        Calendar calendar = Calendar.getInstance();
                                        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);

                                        Map<String, Object> roomData = new HashMap<>();
                                        roomData.put("roomId", chatRoomId);

                                        roomData.put("opponentName", opponentNickname);
                                        roomData.put("myName", nickname);

                                        roomData.put("time", time);
                                        roomData.put("createdBy", currentUserUid);
                                        roomData.put("createdFor", opponentUid);
                                        roomData.put("timestamp", FieldValue.serverTimestamp());
                                        roomData.put("keeperLat", keeperLat);
                                        roomData.put("keeperLon", keeperLon);

                                        db.collection("chattingRoom")
                                                .document(chatRoomId)
                                                .set(roomData)
                                                .addOnSuccessListener(aVoid -> {
                                                    // New chat room created successfully
                                                    Log.d(TAG, "Chat room created with ID: " + chatRoomId);

                                                    // Redirect to the chat room with the created room ID
                                                    goToChatRoom(chatRoomId);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Failed to create chat room
                                                    Log.e(TAG, "Error creating chat room", e);
                                                    // Handle failure if necessary
                                                });
                                    }
                                } else {
                                    Log.e(TAG, "Error getting chat room document", task.getException());
                                    // Handle error if necessary
                                }
                            });

                    if (nickname != null) {
                        // Use the retrieved nickname
                        Log.d(TAG, "Nickname: " + nickname);
                    } else {
                        // Handle null value if needed
                        Log.d(TAG, "Nickname is null");
                    }
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", uidtask.getException());
            }
        });


//        // Sort the UIDs alphabetically to ensure consistency in generating the chat room ID
//        String[] userIds = {currentUserUid, opponentUid};
//        Arrays.sort(userIds);
//
//        String chatRoomId = userIds[0] + "_" + userIds[1]; // Unique chat room ID
//
//        // Check if the chat room already exists
//        db.collection("chattingRoom")
//                .document(chatRoomId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot documentSnapshot = task.getResult();
//                        if (documentSnapshot.exists()) {
//                            // Chat room already exists between these users
//                            goToChatRoom(chatRoomId);
//                        } else {
//                            // Create a new chat room if it doesn't exist
//                            Calendar calendar = Calendar.getInstance();
//                            String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
//
//                            Map<String, Object> roomData = new HashMap<>();
//                            roomData.put("opponentName", opponentNickname);
//                            roomData.put("myName", opponentNickname);
//                            roomData.put("time", time);
//                            roomData.put("createdBy", currentUserUid);
//                            roomData.put("createdFor", opponentUid);
//                            roomData.put("timestamp", FieldValue.serverTimestamp());
//
//                            db.collection("chattingRoom")
//                                    .document(chatRoomId)
//                                    .set(roomData)
//                                    .addOnSuccessListener(aVoid -> {
//                                        // New chat room created successfully
//                                        Log.d(TAG, "Chat room created with ID: " + chatRoomId);
//
//                                        // Redirect to the chat room with the created room ID
//                                        goToChatRoom(chatRoomId);
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        // Failed to create chat room
//                                        Log.e(TAG, "Error creating chat room", e);
//                                        // Handle failure if necessary
//                                    });
//                        }
//                    } else {
//                        Log.e(TAG, "Error getting chat room document", task.getException());
//                        // Handle error if necessary
//                    }
//                });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}