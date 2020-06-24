package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.chatapplication.notificatons.Token;
import com.example.chatapplication.util.UseUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashBoardActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth firebaseAuth;
    //    TextView mProfileTv;
    ActionBar actionBar;
    BottomNavigationView navigationView;
    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
//        mProfileTv = findViewById(R.id.mProfileTv);
        firebaseAuth = FirebaseAuth.getInstance();

        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.setSelectedItemId(R.id.nav_home);//for on create mode select fragment in BottomNavigationView.

        actionBar = getSupportActionBar();
        actionBar.setTitle("News Feed");
        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    private void updateToken(String token) {
        DatabaseReference df = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        df.child(mUID).setValue(mToken);
      // updateToken(FirebaseInstanceId.getInstance().getToken());

    }


    public void repleaseFragment(Fragment fragment) {
        FragmentTransaction fm;
        fm = getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(UseUtil.changeFragmentAnimation1st, UseUtil.changeFragmentAnimation2nd, UseUtil.changeFragmentAnimation3rd, UseUtil.changeFragmentAnimation4th)
                .replace(R.id.content, fragment);
        fm.commit();
    }

    private void checkUserStatus() {//check user sign in or not and accessibility
        FirebaseUser user = firebaseAuth.getCurrentUser();//get current user
        if (user != null) {
            //user signed in stay here
//            mProfileTv.setText(user.getEmail());
            mUID = user.getUid();//for get current userId
            //save user id of currently signed in user in  SharedPreferences.
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
            //updateToken
            updateToken(FirebaseInstanceId.getInstance().getToken());
        } else{
            //user not signed in
            startActivity(new Intent(DashBoardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_home:
                repleaseFragment(new HomeFragment());
                return true;
            case R.id.nav_profile:
                repleaseFragment(new ProfileFragment());
                return true;
            case R.id.nav_users:
                repleaseFragment(new UsersFragment());
                return true;
            case R.id.nav_chat:
                repleaseFragment(new ChatlistFragment());
        }
        return false;
    }
}
