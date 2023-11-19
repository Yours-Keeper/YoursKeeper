package com.example.yourskeeper;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;

public class PleaseListMain extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_please_list_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        Intent intent = getIntent();
        String userId = intent.getStringExtra("USER_ID");
        db = FirebaseFirestore.getInstance();
        ArrayList<String> testDataSet = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testDataSet.add("sk");
        }
        db.collection("storeContent").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                double userLat = document.getDouble("lat");
                                double userLon = document.getDouble("lon");
                                String nickName = document.getString("nickname");
                                String userTime = document.getString("time");

                                testDataSet.add("준하");


                            } else {
                                // If the Firestore document does not exist
                                Log.d(TAG, "Document does not exist");
                            }
                        }
                    } else {
                        // If the operation fails with an exception
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.e(TAG, "Error retrieving documents", exception);
                        }
                    }
                });


        //===== 테스트를 위한 더미 데이터 생성 ===================


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager((Context) this);
        recyclerView.setLayoutManager(linearLayoutManager);

        CustomAdapter customAdapter = new CustomAdapter(testDataSet);
        recyclerView.setAdapter(customAdapter);
    }
}
