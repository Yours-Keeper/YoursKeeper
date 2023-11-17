package com.example.yourskeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class NicknameActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        initializeCloudFirestore();

        Button signOutButton = findViewById(R.id.signout);
        Button registButton = findViewById(R.id.nickname_registration_btn);
        EditText nicknameEditText = findViewById(R.id.nickname_editText);

        registButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nicknameEditText.getText().toString();
                Intent intent = getIntent();
                String userId = intent.getStringExtra("USER_ID");
                User user = new User(nickname);
                db.collection("users").document(userId).set(user);
                updateUI();
                }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                finish();
            }
        });
    }

    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
    private void updateUI() {
            Intent intent = new Intent(this, MainPageActivity.class);
            startActivity(intent);
    }
}
