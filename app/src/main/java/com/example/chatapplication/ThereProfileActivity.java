package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.adapter.AdapterPost;
import com.example.chatapplication.convertImage.StringImageCodeToBitmap;
import com.example.chatapplication.modelAll.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageView avaterIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    RecyclerView postsRecyclerview;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        avaterIv = findViewById(R.id.avaterIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        postsRecyclerview = findViewById(R.id.recyclerview_posts);

        firebaseAuth = FirebaseAuth.getInstance();
        //get uid of click user to retrieve user posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
//                    Toast.makeText(getActivity(), "" + dataSnapshot.child("email").getValue(), Toast.LENGTH_SHORT).show();
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        Bitmap bitmap = StringImageCodeToBitmap.jsonimageConvertTOBitmap(cover);
                        coverIv.setImageBitmap(bitmap);

                    } catch (Exception e) {
                        //Picasso.get().load(cover).into(coverIv);

                    }
                    try {

//          Bitmap convertImage =  StringImageCodeToBitmap.jsonimageConvertTOBitmap(userImage);
                        Bitmap bitmap = StringImageCodeToBitmap.jsonimageConvertTOBitmap(image);
//                        byte[] images = image.getBytes();
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(images, 0, images.length);
                        avaterIv.setImageBitmap(bitmap);
                        /*Picasso.get().load(images).into(avaterIv);*/

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile_icon).into(avaterIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postList = new ArrayList<>();
        checkUserStatus();//at first check this method logic
        loadHistPosts();

    }

    private void loadHistPosts() {
        //linearLayout for recyclerview
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //show newest post first.for this load from last
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerview.setLayoutManager(manager);
        //initt.posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //Query to load posts
        /*whenever user publishes a post the uid of this user is also saved as a info of post
         *so we are retrieving posts having uid equalsTo current user uid "uid"
         */
        Query query = ref.orderByChild("uid").equalTo(uid);//jodi post firebase er theke asa data gula theke uid current user er sathe match kore
        //now get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    //now all value add to list
                    postList.add(modelPost);
                    //adapter
                    adapterPost = new AdapterPost(ThereProfileActivity.this, postList);
                    //add this adapter in recyclerview
                    postsRecyclerview.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchHistPosts(final String searchQuert) {
        //linearLayout for recyclerview
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        //show newest post first.for this load from last
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerview.setLayoutManager(manager);
        //initt.posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //Query to load posts
        /*whenever user publishes a post the uid of this user is also saved as a info of post
         *so we are retrieving posts having uid equalsTo current user uid "uid"
         */
        Query query = ref.orderByChild("uid").equalTo(uid);//jodi post firebase er theke asa data gula theke uid current user er sathe match kore
        //now get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getPTitle().toLowerCase().contains(searchQuert.toLowerCase()) ||
                            myPosts.getPDescr().toLowerCase().contains(searchQuert.toLowerCase())) {//This condition for search .
                        postList.add(myPosts);

                    }
                    //now all value add to list
                    postList.add(myPosts);
                    //adapter
                    adapterPost = new AdapterPost(ThereProfileActivity.this, postList);
                    //add this adapter in recyclerview
                    postsRecyclerview.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkUserStatus() {//check user sign in or not and accessibility
        FirebaseUser user = firebaseAuth.getCurrentUser();//get current user
        if (user != null) {
            //user signed in stay here
//            mProfileTv.setText(user.getEmail());

        } else {
            //user not signed in
            startActivity(new Intent(ThereProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.actionAddPost).setVisible(false);//hide add post from this activity
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called whrn user press search button
                if (!TextUtils.isEmpty(query)) {
                    searchHistPosts(query);
                } else {
                    loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user type any latter
                if (!TextUtils.isEmpty(newText)) {
                    searchHistPosts(newText);
                } else {
                    loadHistPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}