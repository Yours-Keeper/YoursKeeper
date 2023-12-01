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
    private final TextView mTimeField;


    public ChatHolder(@NonNull View itemView) {
        super(itemView);
        mNameField = itemView.findViewById(R.id.message_nickname);  //닉네임 텍스트뷰의 레이아웃을 정하는 문장
        mTextField = itemView.findViewById(R.id.message_text);  //메세지 내용 텍스트뷰의 레이아웃을 정하는 문장
        mTimeField = itemView.findViewById(R.id.message_time);  //전송 시간 텍스트뷰의 레이아웃을 정하는 문장
    }

    public void bind(@NonNull Chat chat) {
        setName(chat.getName());
        setMessage(chat.getMessage());
        setTime(chat.getTime());
    }

    private void setName(@Nullable String name) {
        mNameField.setText(name);
    }

    private void setMessage(@Nullable String text) {
        mTextField.setText(text);
    }

    private void setTime(@Nullable String time) {
        mTimeField.setText(time);
    }

}
