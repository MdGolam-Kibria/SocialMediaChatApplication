package com.example.chatapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText emailLogin, passwordLogin;
    TextView haveAccountTvLogin, forgetPasswordTv;
    Button loginBtn;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        forgetPasswordTv = findViewById(R.id.forgetPasswordTv);
        loginBtn = findViewById(R.id.LoginBtn);
        haveAccountTvLogin = findViewById(R.id.haveAccountTvLogin);
        loginBtn.setOnClickListener(this);
        haveAccountTvLogin.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        forgetPasswordTv.setOnClickListener(this);
        forgetPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialogue();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.haveAccountTvLogin:
                startActivity(new Intent(LoginActivity.this, ResisterActivity.class));
                finish();
            case R.id.LoginBtn:
                if (emailLogin.getText().toString().trim().isEmpty()) {
                    emailLogin.setError("Email Address is Empty");
                }
                if (isValidEmailId(emailLogin.getText().toString().trim()) == false) {
                    emailLogin.setError("InValid Email Address.");
                    emailLogin.requestFocus();
                    return;
                }
                if (passwordLogin.getText().toString().trim().isEmpty()) {
                    passwordLogin.setError("Password is Empty");
                    passwordLogin.requestFocus();
                    return;
                }
                if (passwordLogin.getText().toString().trim().length() <= 7) {
                    passwordLogin.setError("Password Should be 2 or More");
                    passwordLogin.requestFocus();
                    return;
                }
                progressDialog.show();
                loginUser(emailLogin.getText().toString().trim(), passwordLogin.getText().toString().trim());

        }
    }

    private void showRecoverPasswordDialogue() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View view1 = getLayoutInflater().inflate(R.layout.dialog_box, null);
        final Button recoverBtn = view1.findViewById(R.id.recoverBtn);
        final Button cancel = view1.findViewById(R.id.cancel);
        final EditText recoverEmail = view1.findViewById(R.id.recoverEmail);

        builder.setView(view1);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        final String email = recoverEmail.getText().toString();
        recoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginRecovery(email);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        // AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Recover Password");
//        //set layout
//        LinearLayout linearLayout = new LinearLayout(this);
//        //make view
//        final EditText emailEt = new EditText(this);
//        emailEt.setHint("Enter Your Email");
//        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
//        emailEt.setMaxEms(16);
//
//
//        linearLayout.addView(emailEt);//add view to linearLayout
//        linearLayout.setPadding(10, 10, 10, 10);
        // View view = getLayoutInflater().inflate(R.layout.dialog_box, null, false);


        //  builder.setView(view);//add view for dialog
//
//        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {//custom alert dialog
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String email = emailEt.getText().toString().trim();
//                beginRecovery(email);
//            }
//        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
        // builder.create().show();
    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Login User........");
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.show();
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get a proper error sms
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("LLL", e.getMessage());
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.setMessage("Login User........");
                            progressDialog.show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "" + user.getEmail(), Toast.LENGTH_LONG).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Log.d("LLL", task.getException().getMessage());
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void updateUI(FirebaseUser user) {
        if (!user.getEmail().equals(null)) {
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
            finish();
        } else {
            Toast.makeText(this, "no login", Toast.LENGTH_SHORT).show();
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


}
