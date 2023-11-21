package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class StoreMainPage extends AppCompatActivity {
    private FirebaseFirestore db;
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
        TextView textTitle = findViewById(R.id.toolbar_Titie);
        textTitle.setText("맡아줄게요");

        initializeCloudFirestore();
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
        String userId = intent.getStringExtra("USER_ID");
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
                        // Firestore에 데이터 저장
                        db.collection("storeContent").document(userId).update(data);
                        // 액티비티 종료
                        goBack();
                    } else {
                        // 위치 정보를 가져오지 못했을 때 처리 (예: Toast 메시지 등)
                        Toast.makeText(StoreMainPage.this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    private void goBack() {
        Intent intent = new Intent(StoreMainPage.this, MainPageActivity.class);
        startActivity(intent);
    }
}