package com.example.yourskeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTochange();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
    }
    // 수정을 누르면 일단 db에서 지우고
    private void deleteTochange(){
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
    }
    private void showDeleteDialog(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.delete_dialog, null); // null 자리는 거의 null로만 씀

        customDialog = new AlertDialog.Builder(StoreTextComplete.this, R.style.RoundedCornersDialog_signout)
                .setView(dialogView)
                .create();

        customDialog.show();

        ImageView back = customDialog.findViewById(R.id.delete_back);
        Button signOutButton = customDialog.findViewById(R.id.delete_confirm_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
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
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}