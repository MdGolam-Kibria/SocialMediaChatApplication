package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button login, resister;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.login);
        resister = findViewById(R.id.resister);
        resister.setOnClickListener(this);
        login.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);//for show resister progress.
        progressDialog.setMessage("Resistering User......");


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.resister:
                startActivity(new Intent(MainActivity.this, ResisterActivity.class));
            case R.id.login:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }
}



