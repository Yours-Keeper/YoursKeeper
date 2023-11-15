package com.example.yourskeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class NicknameActivity extends AppCompatActivity {

    Button signOutButton;
    Button nicknameRegistrationButton;
    EditText nicknameTextField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        signOutButton = findViewById(R.id.signout);
        nicknameRegistrationButton = findViewById(R.id.nickname_registration_btn);
        nicknameTextField = findViewById(R.id.nickname_textField);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                finish();
            }
        });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}