package com.example.yourskeeper;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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

        // Configure recycler adapter options:
        //  * query is the Query object defined above.
        //  * Store.class instructs the adapter to convert each DocumentSnapshot to a Store object
        FirestoreRecyclerOptions<Store> options = new FirestoreRecyclerOptions.Builder<Store>()
                .setQuery(query, Store.class)
                .build();

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
}