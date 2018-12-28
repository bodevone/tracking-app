package com.example.bodevan.trackappfirebase2712;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button lgn;

    private EditText username;
    private EditText password;
    private String stateRole;
    private String usernameString;
    private ProgressBar mProgressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mAccountDatabeReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getSupportActionBar().setTitle(Html.fromHtml("<font color=#1c1c1c>" +
                getString(R.string.app_name) + "</font>"));

        lgn = findViewById(R.id.bt_login);

        username = findViewById(R.id.input_username);
        password = findViewById(R.id.input_password);


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAccountDatabeReference = mFirebaseDatabase.getReference().child("auth").child("accounts");


        lgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWithDatabase();
            }
        });
    }

    private void checkWithDatabase(){
        Query query = mAccountDatabeReference.orderByChild("username").equalTo(username.getText().toString().trim());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0

                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        Account account = user.getValue(Account.class);

                        if (account.password.equals(password.getText().toString().trim())) {
                            usernameString = account.username;
                            stateRole = account.role;
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("role", stateRole).putExtra("username", usernameString);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Password is wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
