package com.example.bodevan.trackappfirebase2712;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class FeedbackFragment extends Fragment {

    private EditText userName;
    private EditText feedBack;
    private Button send;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFeedbackReferenceDatabase;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFeedbackReferenceDatabase = mFirebaseDatabase.getReference().child("feedbacks");

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
        email = user.getEmail();

        Map feedbackMap = new HashMap();
        feedbackMap.put("feedback", feedback);
        feedbackMap.put("name", name);
        feedbackMap.put("username", email);

        mFeedbackReferenceDatabase.push().setValue(feedbackMap);

        userName.getText().clear();
        feedBack.getText().clear();

//        Toast.makeText(getActivity(), "Отзыв отправлен", Toast.LENGTH_LONG).show();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        afterMath();
    }

    private void afterMath() {
        // inflate the layout of the popup window
        View popupView = getLayoutInflater().inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
}
