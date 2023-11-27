package com.example.yourskeeper;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PleaseList extends AppCompatActivity {

    private FirestoreRecyclerAdapter<Store, ListViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_please_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);

        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("storeContent")
                .orderBy("time")
                .limit(50);
        FloatingActionButton goPleaseMainButton = findViewById(R.id.mapBtn);
        // ...
        goPleaseMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goPleaseMain();
            }
        });
        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Store.class instructs the adapter to convert each DocumentSnapshot to a Store object
        FirestoreRecyclerOptions<Store> options = new FirestoreRecyclerOptions.Builder<Store>()
                .setQuery(query, Store.class)
                .build();
        FloatingActionButton goPleaseListButton = findViewById(R.id.fabMenu);
        adapter = new FirestoreRecyclerAdapter<Store, ListViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ListViewHolder holder, int position, Store model) {
                // Bind the Store object to the ListViewHolder
                holder.bind(model);
            }

            @Override
            public ListViewHolder onCreateViewHolder(ViewGroup group, int i) {
                // 사용자 지정 레이아웃을 사용하여 ViewHolder의 새 인스턴스 생성
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.list_item, group, false);

                return new ListViewHolder(view);
            }
        };

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private void showCustomModal(String title,String nickname,  float distance, String userId, String userTime) {
        Dialog dialog = new Dialog(this, R.style.RoundedCornersDialog);
        dialog.setContentView(R.layout.dialog_custom);
        TextView textTitle= dialog.findViewById(R.id.modalTitle);
        TextView textTime = dialog.findViewById(R.id.modalTime);
        TextView modalDistance = dialog.findViewById(R.id.modalDistance);
        textTitle.setText(nickname);
        textTime.setText(userTime);
        modalDistance.setText(String.format("%.0f 미터", distance));
        // Firebase 사용자 정보(userId)를 사용하여 Firestore에서 사용자 정보 가져오기
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            layoutParams.y = 200; // 20픽셀 위에 위치
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private void goPleaseMain() {

        Intent intent = new Intent(this, PleaseMain_act.class);
        startActivity(intent);

    }
}