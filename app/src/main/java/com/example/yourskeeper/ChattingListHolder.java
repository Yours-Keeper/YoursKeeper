package com.example.yourskeeper;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ChattingListHolder extends RecyclerView.ViewHolder {
    private final TextView mNameField;
    private final TextView mTimeField;

    public ChattingListHolder(@NonNull View itemView) {
        super(itemView);
        mNameField = itemView.findViewById(R.id.chatting_list_nickname);
        mTimeField = itemView.findViewById(R.id.chatting_list_time);
    }

    public void bind(@NonNull ChattingList chattingList) {
        setName(chattingList.getMyName());
        setTime(chattingList.getTime());
    }

    private void setName(@Nullable String name) {
        mNameField.setText(name);
    }

    private void setTime(@Nullable String time) {
        mTimeField.setText(time);
    }
}
