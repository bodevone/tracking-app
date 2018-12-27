package com.example.bodevan.trackappfirebase2712;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class FeedbackFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        TextView nameView = v.findViewById(R.id.name_color);
        String star = getColoredSpanned("*", "#FF0000");
        String name = getColoredSpanned("Ваше Имя","#666");
        nameView.setText(Html.fromHtml(star + "" + name));

        TextView feedView = v.findViewById(R.id.feed_color);
        String feed = getColoredSpanned("Ваш Отзыв","#666");
        feedView.setText(Html.fromHtml(star + "" + feed));


        Button send = v.findViewById(R.id.bt_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Отзыв отправлен", Toast.LENGTH_LONG).show();
            }
        });



        return v;
    }

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }
}
