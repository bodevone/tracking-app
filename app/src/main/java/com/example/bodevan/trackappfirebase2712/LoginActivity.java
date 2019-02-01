package com.example.bodevan.trackappfirebase2712;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button lgn;
    private EditText usernameText;
    private EditText passwordText;
    private ProgressBar progressBar;
    private ProgressBar progressBarInit;
    private RelativeLayout background;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriversDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    private String currentEmail;
    private String driverForUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Change Action Bar Color
        getSupportActionBar().setTitle(Html.fromHtml("<font color=#1c1c1c>" +
                getString(R.string.app_name) + "</font>"));

        lgn = findViewById(R.id.bt_login);
        usernameText = findViewById(R.id.input_username);
        passwordText = findViewById(R.id.input_password);
        progressBar = findViewById(R.id.progressBar);
        progressBarInit = findViewById(R.id.progressBarInit);
        background = findViewById(R.id.relLayout);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.goOnline();
        mDriversDatabaseReference = mFirebaseDatabase.getReference().child("auth").child("drivers");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("auth").child("users");

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            background.setVisibility(View.GONE);
            progressBarInit.setVisibility(View.VISIBLE);
            currentEmail = mAuth.getCurrentUser().getEmail();
            findRoleFromDatabase(currentEmail);
        }

        lgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = usernameText.getText().toString();
                final String password = passwordText.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Введите имя пользователя!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Введите пароль!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //Authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    findRoleFromDatabase(email);
                                }
                            }
                        });
            }
        });
    }

    private void findRoleFromDatabase(final String email) {
        //Checking if your email in a list of drivers
        mDriversDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String value = String.valueOf(childSnapshot.child("driver").getValue());

                    if (value.equals(email)) {
                        enterDriverActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //Checking if your username in a list of users
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String value = String.valueOf(childSnapshot.child("user").getValue());
                    if (value.equals(email)) {
                        driverForUserEmail = String.valueOf(childSnapshot.child("driver").getValue());
                        enterUserActivity(driverForUserEmail);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void enterDriverActivity() {
        Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
        progressBar.setVisibility(View.GONE);
        startActivity(intent);
        finish();
    }

    public void enterUserActivity(String driverEmail) {
        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
        intent.putExtra("email", driverEmail);
        progressBar.setVisibility(View.GONE);
        startActivity(intent);
        finish();
    }
}
