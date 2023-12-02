package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayoutStates;

import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class StoreMainPage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog customDialog;
    private FusedLocationProviderClient fusedLocationClient;
    private double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_store_main_page);

        Button completeButton = findViewById(R.id.store_Complete_Btn);
        Toolbar toolbar = findViewById(R.id.toolbar_Store);
        toolbar.setContentInsetsAbsolute(0, 0);
        ImageView btnMenu = findViewById(R.id.btnMenu);
        ImageView btnBack = findViewById(R.id.btnBack);

        TextView textTitle = findViewById(R.id.toolbar_Titie);
        textTitle.setText("맡아줄게요");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        EditText editText1 = findViewById(R.id.edit_text1);
        EditText editText2 = findViewById(R.id.edit_text2);
        EditText editText3 = findViewById(R.id.edit_text3);
        EditText editText4 = findViewById(R.id.edit_text4);
        EditText editContents = findViewById(R.id.edit_contents);

        // Set OnFocusChangeListener for editText1
        // Set OnFocusChangeListener for editText2, editText3, editText4, and editContents
        editText1.setOnFocusChangeListener(getFocusChangeListener(editText1));
        editText2.setOnFocusChangeListener(getFocusChangeListener(editText2));
        editText3.setOnFocusChangeListener(getFocusChangeListener(editText3));
        editText4.setOnFocusChangeListener(getFocusChangeListener(editText4));
        editContents.setOnFocusChangeListener(getFocusChangeListener(editContents));

        LinearLayout rootLayout = findViewById(R.id.store_main);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                return false;
            }
        });
        Intent intent = getIntent();
        String userId;
        String fromMainPage = intent.getStringExtra("USER_ID");
        String fromStoreTextComplete = intent.getStringExtra("USER_ID_from_StoreTextComplete");
        if (fromMainPage != null){
            userId = fromMainPage;
        }
        else {
            userId = fromStoreTextComplete;
        }
        HashMap<String, Object> data = new HashMap<>();
        if(userId !=null) {
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String userNickname = document.getString("nickname");
                                data.put("nickname", userNickname);
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

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editContents.getText().toString();
                String timeHour1 = editText1.getText().toString();
                String timeHour2 = editText3.getText().toString();
                String timeMinute1 = editText2.getText().toString();
                String timeMinute2 = editText4.getText().toString();
                content = content.replace("\n", "\\n");
                String resultTime = timeHour1 + ":" + timeMinute1 + " ~ " + timeHour2 + ":" + timeMinute2;
                data.put("content", content);
                data.put("time", resultTime);
                // Get the last known location
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    // Got last known location
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        data.put("lat", lat);
                        data.put("lon", lon);
                        data.put("point", 80);
                        data.put("uid", userId);
                        // Firestore에 데이터 저장
                        db.collection("storeContent").document(userId).set(data);
                        goStoreTextComplete();
                    } else {
                        // 위치 정보를 가져오지 못했을 때 처리 (예: Toast 메시지 등)
                        Toast.makeText(StoreMainPage.this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private View.OnFocusChangeListener getFocusChangeListener(final EditText editText) {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // EditText has gained focus
                    if (editText.getId() != R.id.edit_contents) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                } else {
                    if (editText.getId() != R.id.edit_contents) {
                        editText.setInputType(InputType.TYPE_NULL);
                    }
                    // EditText has lost focus, set inputType to none
                }
            }
        };
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void goStoreTextComplete() {
        Intent intent = new Intent(this, StoreTextComplete.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
                            String nickname = document.getString("nickname");
                            Long reliabilityPoint = document.getLong("reliability_point");

                            if (nickname != null) {
                                nicknameTextView.setText(nickname);
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

        customDialog = new AlertDialog.Builder(StoreMainPage.this, R.style.RoundedCornersDialog_signout)
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