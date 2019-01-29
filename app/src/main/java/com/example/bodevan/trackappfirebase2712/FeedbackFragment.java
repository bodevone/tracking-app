package com.example.bodevan.trackappfirebase2712;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class FeedbackFragment extends Fragment {

    private EditText userName;
    private EditText feedBack;
    private Button send;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFeedbackReferenceDatabase;
    private DatabaseReference mAccountsReferenceDatabase;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String uid;
    private String email;

    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFeedbackReferenceDatabase = mFirebaseDatabase.getReference().child("feedbacks");
        mAccountsReferenceDatabase = mFirebaseDatabase.getReference().child("accounts");


        userName = v.findViewById(R.id.name);
        feedBack = v.findViewById(R.id.feedback);

        //Text colors
        TextView nameView = v.findViewById(R.id.name_color);
        String star = getColoredSpanned("*", "#FF0000");
        String name = getColoredSpanned("Ваше Имя", "#666");
        nameView.setText(Html.fromHtml(star + "" + name));

        TextView feedView = v.findViewById(R.id.feed_color);
        String feed = getColoredSpanned("Ваш Отзыв", "#666");
        feedView.setText(Html.fromHtml(star + "" + feed));


        send = v.findViewById(R.id.bt_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameUser = userName.getText().toString();
                String feedbackUser = feedBack.getText().toString();

                if (TextUtils.isEmpty(nameUser)) {
                    Toast.makeText(getActivity(), "Введите Ваше Имя!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(feedbackUser)) {
                    Toast.makeText(getActivity(), "Введите Отзыв!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    sendFeedback(nameUser, feedbackUser);
                }
            }
        });


        return v;
    }

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    private void sendFeedback(final String name, final String feedback) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        email = user.getEmail();

        Map feedbackMap = new HashMap();
        feedbackMap.put("feedback", feedback);
        feedbackMap.put("name", name);
        feedbackMap.put("username", email);

        mFeedbackReferenceDatabase.push().setValue(feedbackMap);

        userName.getText().clear();
        feedBack.getText().clear();

        Toast.makeText(getActivity(), "Отзыв отправлен", Toast.LENGTH_LONG).show();

    }
}
