package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class ResisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText email, password;
    TextView haveAccountTv;
    Button resisterBtn;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resister);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Resister activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        resisterBtn = findViewById(R.id.resisterBtn);
        haveAccountTv = findViewById(R.id.haveAccountTv);
        resisterBtn.setOnClickListener(this);
        haveAccountTv.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Resistering User........");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//for go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.haveAccountTv:
                startActivity(new Intent(ResisterActivity.this, LoginActivity.class));
                finish();
            case R.id.resisterBtn:
                if (email.getText().toString().trim().isEmpty()) {
                    email.setError("Email Address is Empty");
                }
                if (isValidEmailId(email.getText().toString().trim()) == false) {
                    email.setError("InValid Email Address.");
                    email.requestFocus();
                    return;
                }
                if (password.getText().toString().trim().isEmpty()) {
                    password.setError("Password is Empty");
                    password.requestFocus();
                    return;
                }
                if (password.getText().toString().trim().length() <= 7) {
                    password.setError("Password Should be 2 or More");
                    password.requestFocus();
                    return;
                }
                resisterUser(email.getText().toString().trim(), password.getText().toString().trim());
        }
    }

    private void resisterUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            // get user id and email from firebase auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            // when user resisteried store user info to realtime database
                            //using hasmap
                            HashMap<Object,String> hashMap = new HashMap<>();
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");// will add later
                            hashMap.put("onlineStatus","online");// will add later
                            hashMap.put("phone","");// will add later
                            hashMap.put("image","");// will add later
                            hashMap.put("cover","");// will add later
                            //Firebase database instance
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            //path to store user data named "Users";
                            DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
                            //put data within hasmap in database
                            databaseReference.child(uid).setValue(hashMap);


                            Toast.makeText(ResisterActivity.this, "successfully signin" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(ResisterActivity.this, ProfileActivity.class));
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(ResisterActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("FFF", task.getException().getMessage());
                            progressDialog.dismiss();
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (!user.getEmail().equals(null)) {
            Toast.makeText(this, "" + user.getEmail(), Toast.LENGTH_LONG).show();
            startActivity(new Intent(ResisterActivity.this, DashBoardActivity.class));
//            finish();
        } else {
            Toast.makeText(this, "no resister", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmailId(String email) {

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
