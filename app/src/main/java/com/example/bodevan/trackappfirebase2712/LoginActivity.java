package com.example.bodevan.trackappfirebase2712;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class LoginActivity extends AppCompatActivity {

    private Button lgn;
    CheckBox driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getSupportActionBar().setTitle(Html.fromHtml("<font color=#1c1c1c>" +
                getString(R.string.app_name) + "</font>"));

        driver = findViewById(R.id.driver);
        lgn = findViewById(R.id.bt_login);


        lgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("state", driver.isChecked());
                startActivity(intent);
                finish();
            }
        });
    }
}
