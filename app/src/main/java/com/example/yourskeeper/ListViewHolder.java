package com.example.yourskeeper;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ListViewHolder extends RecyclerView.ViewHolder {
    private final TextView mNameField;
    private final TextView mTextField;
//    private final TextView mScoreField;
//    private final TextView mDistanceField;
    public ListViewHolder(@NonNull View itemView) {
        super(itemView);

        mNameField = itemView.findViewById(R.id.item_nickname);
        mTextField = itemView.findViewById(R.id.item_time);
//        mDistanceField = itemView.findViewById(R.id.item_distance);
//       mScoreField = itemView.findViewById(R.id.item_score);
    }

    public void bind(@NonNull Store store) {
        setName(store.getNickname());
        setTime(store.getTime());
//        setScore(store.getScore().toString());
//        setDistance(store.getDistance());
    }

    private void setName(@Nullable String name) {
        mNameField.setText(name);
    }
//    private void setScore(@Nullable String score) {
//        mScoreField.setText(score);
//    }
//    private void setDistance(@Nullable String distance) {
//        mDistanceField.setText(distance);
//    }


    private void setTime(@Nullable String text) {
        mTextField.setText(text);
    }
}
