package com.example.yourskeeper;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ShowMain extends AppCompatActivity {
    // 로그아웃하지 않고 firestore에서 db만 삭제한 경우 Authentication에는 데이터가 있기 때문에 로그인 된 걸로 생각 돼서 앱이 실행 안 될 수 있음

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // 애니메이션을 적용할 TextView
        TextView textView = findViewById(R.id.main_text);

        // 애니메이션 리소스 파일 로드
        Animation mainAnim = AnimationUtils.loadAnimation(this, R.anim.main_anim);



        // 애니메이션 리스너를 사용하여 애니메이션이 끝날 때 원하는 작업 수행
        mainAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 애니메이션이 시작될 때 수행할 작업
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 애니메이션이 끝날 때 수행할 작업
                Log.d("JHJ", "onAnimationEnd: ");
                checkStore();
                checkDB();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // 애니메이션이 반복될 때 수행할 작업
            }
        });

        // TextView에 애니메이션 적용
        textView.startAnimation(mainAnim);
    }

    private void checkStore(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        DocumentReference docRef = db.collection("storeContent").document(userId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // A 함수 실행
                    goStoreTextComplete();
                } else {
                    // 해당 문서가 존재하지 않을 때의 처리
                    // 여기에 원하는 로직을 추가하세요
                }
            } else {
                // 작업이 실패한 경우의 처리
                Exception exception = task.getException();
                if (exception != null) {
                    // 에러 로그 출력 또는 다른 처리
                }
            }
        });
    }
    private void goStoreTextComplete(){
        Intent intent = new Intent(this, StoreTextComplete.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private void checkDB(){
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 1. 로그인이 안돼있으면 goMainActivity
        // 2. 로그인이 돼있는데 닉네임이 없으면 goNickname
        // 3. 로그인 돼있고 닉네임도 있으면 goMainPage

        if (currentUser != null) {
            Log.d("JHJ", "if currentUser != null");
            checkNickname(currentUser);
        } else {
            Log.d("JHJ", "else : ");
            goMainActivity();
        }
    }

    private void checkNickname(FirebaseUser user){
        String userId = user.getUid();
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData(); // document를 map으로 가져옴

                        Log.d("JHJ", "DocumentSnapshot data: " + data);
                        //닉네임이 데이터가 들어있는지 확인
                        if ((String) data.get("nickname") != null){
                            goMainPage();
                        }
                        else goNickname(userId);
                    } else {
                        Log.d("JHJ", "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void goMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goNickname(String userId){
        Intent intent = new Intent(this, NicknameActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }

    private void goMainPage(){
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
        finish();
    }
}