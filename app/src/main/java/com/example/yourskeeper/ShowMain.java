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
    // 로그아웃하지 않고 firestore에서 db만 삭제한 경우

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
        Intent intent1 = new Intent(this, MainActivity.class);
        startActivity(intent1);
        finish();
    }

    private void goNickname(String userId){
        Intent intent2 = new Intent(this, NicknameActivity.class);
        intent2.putExtra("USER_ID", userId);
        startActivity(intent2);
        finish();
    }

    private void goMainPage(){
        Intent intent3 = new Intent(this, MainPageActivity.class);
        startActivity(intent3);
        finish();
    }
}