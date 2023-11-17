package com.example.yourskeeper;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "UILab";

    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> oneTapUILauncher;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configOneTapSignUpOrSignInClient();
        initFirebaseAuth();

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "이미 로그인 했음");
            updateUI(currentUser);
        } else {
            Log.d(TAG, "아직 로그인 안했음");
        }
    }

    private void configOneTapSignUpOrSignInClient() {
        oneTapClient = Identity.getSignInClient(this);

        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id)) // 버그임. 이 부분에서 그냥 빨간줄 떠도 무시 해도 됨
                        //.setFilterByAuthorizedAccounts(true) // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false) // Show all accounts on the device.
                        .build())
                .build();

        // 공식 문서 코드가 조금 옛날 스타일이라 launcher 스타일로 교수님이 변경해 놓으신거
        // onTapUI를 실행시키는 launcher
        oneTapUILauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithCredential:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            updateUI(user);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                                            updateUI(null);
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    // https://developers.google.com/identity/one-tap/android/get-saved-credentials
    private void signIn() {
        // check whether the user has any saved credentials for your app. -> onSuccess or onFailure
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() { // 기기에 구글계정이 있는 경우
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        IntentSender intentSender = beginSignInResult.getPendingIntent().getIntentSender();
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
                        oneTapUILauncher.launch(intentSenderRequest); // 런처를 띄움
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() { // 기기에 구글계정이 없는 경우
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid(); // 사용자 ID 가져오기

            Intent intent = new Intent(this, NicknameActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        }
    }
}