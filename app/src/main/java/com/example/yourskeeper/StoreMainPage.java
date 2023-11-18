package com.example.yourskeeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StoreMainPage extends AppCompatActivity {
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_main_page);
        Button completeButton = findViewById(R.id.store_Complete_Btn);
        Toolbar toolbar = findViewById(R.id.toolbar_Store);
        toolbar.setContentInsetsAbsolute(0, 0);
        TextView textTitle = findViewById(R.id.toolbar_Titie);
        textTitle.setText("맡아줄게요");
        initializeCloudFirestore();
        EditText editText1 = findViewById(R.id.edit_text1);
        EditText editText2 = findViewById(R.id.edit_text2);
        EditText editText3 = findViewById(R.id.edit_text3);
        EditText editText4 = findViewById(R.id.edit_text4);
        EditText editContents = findViewById(R.id.edit_contents);

        // Set the initial input type for each EditText
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editContents.getText().toString();
                String timeHour1 = editText1.getText().toString();
                String timeHour2 = editText3.getText().toString();
                String timeMinute1 = editText2.getText().toString();
                String timeMinute2 = editText4.getText().toString();
                content = content.replace("\n", "\\n");
                String resulTime = timeHour1 + ":" + timeMinute1+ " ~ " + timeHour2 + ":"+ timeMinute2 ;
                Intent intent = getIntent();
                String userId = intent.getStringExtra("USER_ID");
                User user = new User(content);
                User userTime = new User(resulTime);
                HashMap<String, Object> data = new HashMap<>();
                data.put("content", content);
                data.put("time", resulTime);


                db.collection("storeContent").document(userId).set(data);



                goBack();
            }
        });

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
        View view = this.getCurrentFocus();
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
        Intent intent = new Intent(this, MainPageActivity.class);
        startActivity(intent);
    }
}
