package com.example.yourskeeper;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StoreTextComplete extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog customDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_text_complete);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button changeButton = findViewById(R.id.change_btn);
        Button deleteButton = findViewById(R.id.delete_btn);
        ImageView mainPageBtnMenu = findViewById(R.id.mainPage_btnMenu);

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteToChange();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        mainPageBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showMenuPopup(v); }
        });
    }
    // 수정을 누르면 일단 db에서 지우고
    private void deleteToChange(){
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        db.collection("storeContent") // 컬렉션 이름
                .document(userId)      // 삭제할 문서의 ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 삭제 성공 시 실행할 코드
                    // 예: Toast 메시지 출력
                    showToast("수정 정보를 다시 입력해 주세요.");
                    goStoreMain(userId);
                })
                .addOnFailureListener(e -> {
                    // 삭제 실패 시 실행할 코드
                    // 예: Toast 메시지 출력
                    showToast("문서 삭제 중 오류가 발생했습니다.");
                });
    }
    private void goStoreMain(String userId){
        Intent intent = new Intent(this, StoreMainPage.class);
        intent.putExtra("USER_ID_from_StoreTextComplete", userId);
        startActivity(intent);
        finish();
    }
    private void showDeleteDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.delete_dialog, null); // null 자리는 거의 null로만 씀

        customDialog = new AlertDialog.Builder(StoreTextComplete.this, R.style.RoundedCornersDialog_signout)
                .setView(dialogView)
                .create();

        customDialog.show();

        ImageView back = customDialog.findViewById(R.id.delete_back);
        Button deleteButton = customDialog.findViewById(R.id.delete_confirm_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // db에 store 정보를 삭제해야함
                deleteDocument();
            }
        });
    }

    private void deleteDocument(){
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        db.collection("storeContent") // 컬렉션 이름
                .document(userId)      // 삭제할 문서의 ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 삭제 성공 시 실행할 코드
                    // 예: Toast 메시지 출력
                    showToast("문서가 성공적으로 삭제되었습니다.");
                    goMainPage();
                })
                .addOnFailureListener(e -> {
                    // 삭제 실패 시 실행할 코드
                    // 예: Toast 메시지 출력
                    showToast("문서 삭제 중 오류가 발생했습니다.");
                });
    }
    private void goMainPage(){
        Intent intent = new Intent(this, MainPageActivity.class);
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

        customDialog = new AlertDialog.Builder(StoreTextComplete.this, R.style.RoundedCornersDialog_signout)
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
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}