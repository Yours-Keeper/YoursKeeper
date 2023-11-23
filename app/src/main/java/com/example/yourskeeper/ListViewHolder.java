package com.example.yourskeeper;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {
    private final TextView mNameField;
    private final TextView mTextField;
   private final TextView mScoreField;
   private final TextView mDistanceField;
    public ListViewHolder(@NonNull View itemView) {
        super(itemView);
        mNameField = itemView.findViewById(R.id.item_nickname);
        mTextField = itemView.findViewById(R.id.item_time);
        mDistanceField = itemView.findViewById(R.id.item_distance);
        mScoreField = itemView.findViewById(R.id.item_point);
    }

    public void bind(@NonNull Store store) {
        setName(store.getNickname());
        setTime(store.getTime());
        float distance = store.getDistance();
        int intValue = (int) distance;
// Get the ones digit
        int onesDigit = intValue % 10000;

        setDistance(String.valueOf(onesDigit)+"m");


        if (store.getPoint() != null) {
            setPoint(store.getPoint());
        }


    }

    private void setName(@Nullable String name) {
        mNameField.setText(name);
    }
    private void setPoint(@Nullable Integer score) {
        // score가 null이 아닌 경우에만 설정
        if (score != null) {
            mScoreField.setText(String.valueOf(score)+"점");
        } else {
            // null인 경우에 대한 처리 추가
            mScoreField.setText("N/A");
        }
    }
    private void setDistance(@Nullable String distance) {
        mDistanceField.setText(distance);
    }


    private void setTime(@Nullable String text) {
        mTextField.setText(text);
    }
}
