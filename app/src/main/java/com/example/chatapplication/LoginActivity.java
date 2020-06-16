package com.example.chatapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    EditText emailLogin, passwordLogin;
    TextView haveAccountTvLogin, forgetPasswordTv;
    Button loginBtn;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    SignInButton googleLoginBtn;//for google login

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
        googleLoginBtn = findViewById(R.id.googleLoginBtn);//for google login btn
        googleLoginBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
        haveAccountTvLogin.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        forgetPasswordTv.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.googleLoginBtn) {
            //begin google login process
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        if (v.getId() == R.id.forgetPasswordTv) {
            showRecoverPasswordDialogue();
        }
        if (v.getId() == R.id.haveAccountTvLogin) {

            startActivity(new Intent(LoginActivity.this, ResisterActivity.class));
            finish();
        }
        if (v.getId() == R.id.LoginBtn) {

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


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        //set layout
        LinearLayout linearLayout = new LinearLayout(this);
        //make view
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Enter Your Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMaxEms(16);


        linearLayout.addView(emailEt);//add view to linearLayout
        linearLayout.setPadding(10, 10, 10, 10);


        builder.setView(linearLayout);//add view for dialog

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {//custom alert dialog
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();
                progressDialog.show();
                beginRecovery(email);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Sent Email........");
        if (email.isEmpty()) {
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show();
        }
        if (!email.equals(null)) {

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
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


                            // get user id and email from firebase auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            // when user resisteried store user info to realtime database
                            //using hasmap
                            HashMap<Object,String> hashMap = new HashMap<>();
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");// will add later
                            hashMap.put("phone","");// will add later
                            hashMap.put("image","");// will add later
                            hashMap.put("cover","");// will add later
                            //Firebase database instance
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            //path to store user data named "Users";
                            DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
                            //put data within hasmap in database
                            databaseReference.child(uid).setValue(hashMap);


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
            Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
            /**
             * after add the two FLAGS after change activity can't back previous activity
             */
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);//go to profile activith with google signin
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("google", "firebaseAuthWithGoogle:" + account.getId());
                Toast.makeText(this, "firebaseAuthWithGoogle = " + account.getId(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d("googlef", "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed = " + e.getMessage(), Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("sss", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                String email = user.getEmail();
                                String uid = user.getUid();
                                HashMap<Object,String> hashMap = new HashMap<>();
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name","");
                                hashMap.put("onlineStatus","online");// will add later
                                hashMap.put("password","");
                                hashMap.put("image","");
                                hashMap.put("cover","");
                                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                DatabaseReference databaseReference = firebaseDatabase.getReference("Users");
                                databaseReference.child(uid).setValue(hashMap);
                            }
                            Toast.makeText(LoginActivity.this, "success = " + user.getEmail(), Toast.LENGTH_SHORT).show();//show curent user email
                            Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
                            /**
                             * after add the two FLAGS after change activity can't back previous activity
                             */
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);//go to profile activith with google signin
                            finish();

//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("eee", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "failure = " + task.getException(), Toast.LENGTH_SHORT).show();
//                            Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Exception =  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
