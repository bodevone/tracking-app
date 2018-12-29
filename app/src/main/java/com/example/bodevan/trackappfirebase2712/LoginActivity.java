package com.example.bodevan.trackappfirebase2712;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
    private String stateRole;
    private String usernameString;
    private String driverForUser;
    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mAccountDatabeReference;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getSupportActionBar().setTitle(Html.fromHtml("<font color=#1c1c1c>" +
                getString(R.string.app_name) + "</font>"));

        lgn = findViewById(R.id.bt_login);

        usernameText = findViewById(R.id.input_username);
        passwordText = findViewById(R.id.input_password);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAccountDatabeReference = mFirebaseDatabase.getReference().child("auth").child("accounts");

        mAuth = FirebaseAuth.getInstance();

//        lgn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                checkWithDatabase();
//            }
//        });

        lgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameText.getText().toString();
                final String password = passwordText.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }


                //authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });

    }

//    private void checkWithDatabase(){
//        Query query = mAccountDatabeReference.orderByChild("username").equalTo(username.getText().toString().trim());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // dataSnapshot is the "issue" node with all children with id 0
//
//                    for (DataSnapshot user : dataSnapshot.getChildren()) {
//                        // do something with the individual "issues"
//                        Account account = user.getValue(Account.class);
//
//                        if (account.password.equals(password.getText().toString().trim())) {
//                            usernameString = account.username;
//                            stateRole = account.role;
//                            driverForUser = account.driver;
//
//                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                            intent.putExtra("role", stateRole).putExtra("username", usernameString);
//                            if (stateRole.equals("user")) {
//                                intent.putExtra("driver", driverForUser);
//                            }
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            Toast.makeText(LoginActivity.this, "Password is wrong", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                } else {
//                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
}
