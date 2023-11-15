package com.example.yourskeeper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.yourskeeper.databinding.ActivityChatingListBinding;

public class ChatingList_act extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityChatingListBinding binding = ActivityChatingListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}