package com.example.yourskeeper;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;

public class ChatHolder extends RecyclerView.ViewHolder {
    private final TextView mNameField;
    private final TextView mTextField;

    public ChatHolder(@NonNull View itemView) {
        super(itemView);
        mNameField = itemView.findViewById(android.R.id.text1);
        mTextField = itemView.findViewById(android.R.id.text2);
    }

    public void bind(@NonNull Chat chat) {
        setName(chat.getName());
        setMessage(chat.getMessage());
    }

    private void setName(@Nullable String name) {
        mNameField.setText(name);
    }

    private void setMessage(@Nullable String text) {
        mTextField.setText(text);
    }

}
