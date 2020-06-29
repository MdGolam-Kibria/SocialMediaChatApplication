package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapplication.adapter.AdapterComments;
import com.example.chatapplication.modelAll.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    //to get detail of  user and post
    String hisUid, myUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage;
    boolean mProgressComment = false;
    boolean mProgressLike = false;
    //Progress bar
    ProgressDialog pd;
    //views
    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;
    //add comments views
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        //action bar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");//click post ID

        //init views
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTitleTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);
        //for load post info using post id from firebase database "Posts" node
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        setLikes();
        //set sub title of action bar
        actionBar.setSubtitle("SignedIn as: " + myEmail);
        loadComments();
        //send comment btn clicked
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
        //like button click handle
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });
        //more button click handle
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        //share button  click handle
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = pTitleTv.getText().toString().trim();
                String pDescription = pDescriptionTv.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable) pImageIv.getDrawable();
                if (bitmapDrawable==null){
                    //post without image
                    shareTextOnly(pTitle,pDescription);
                }else {
                    //post with image
                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);
                }
            }
        });
    }


    private void shareTextOnly(String pTitle, String pDescription) {//for share post without any image
        //concatenate tile and description to share
        String shareBody = pTitle+"\n"+pDescription;
        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");//in case you share via an email app
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);//text to share
        startActivity(Intent.createChooser(intent,"Share via"));//message to show share dialog

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {//for share post with image
        //first we will save the image in cache,    get the saved image Uri
        String shareBody = pTitle+"\n"+pDescription;
        //first we will save the image in cache,    get the saved image Uri
        Uri uri = saveImageToShare(bitmap);
        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");//in case you share via an email app
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent,"Share via"));

        //copy same code in post detail activity
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(this.getCacheDir(),"images");//images eta obossoi amader res er paths er cache-path er images name er sathe match korte hobe
        Uri uri = null;
        try {
            imageFolder.mkdir();//create if not exists
            File file = new File(imageFolder,"shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this,"com.example.chatapplication.fileprovider",file);//this authorities name must match with provider in manifests
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }


    private void loadComments() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        //now path of the post to get its comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);
                    commentList.add(modelComment);
                    //pass myUid and postId as parameter of comment adapter

                    //setup adapter
                    adapterComments = new AdapterComments(getApplicationContext(),commentList,myUid,postId);
                    //now set adapter to recyclerview
                    recyclerView.setAdapter(adapterComments);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showMoreOptions() {
        //creating popup menu currently having options Delete ,,we will add more option later
        final PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);
        //show delete option in only post(s) of currently singed in user so..........
        if (hisUid.equals(myUid)) {//mane post er moddhe je gular id current user er sathe match korbe segula sei current user delete korte parbe
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        //add onClick listener in menu item
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    //delete is clicked
                    beginDelete();
                } else if (id == 1) {
                    //delete is clicked
                    //start add post activity with key "editText" and the id of the post clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image so.....
        if (pImage.equals("noImage")) {//post without image
            deleteWithoutImage();
        } else {//post with image
            deleteWithImage();
        }

    }

    private void deleteWithImage() {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting......");
        /*steps:
       1. delete image using url
       2.delete from database using post id
         */
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //image deleted now delete database
                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts")//for deleting post from firebase "Posts" path
                        .orderByChild("pId").equalTo(postId);//this "pId" must match with AdapterPost Model class.
                fQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();//remove value from firebase where "pId" matches
                        }
                        //delete success
                        Toast.makeText(PostDetailActivity.this, "Deleting Successfully", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting.....");
        //image deleted now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts")//for deleting post from firebase "Posts" path
                .orderByChild("pId").equalTo(postId);//this "pId" must match with AdapterPost Model class.
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();//remove value from firebase where "pId" matches
                }
                //delete success
                Toast.makeText(PostDetailActivity.this, "Deleting Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        //when the details of post is loading alsos check is current user has liked or not
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)) {//ei ref node e jodi current user er id thake
                    //user has liked thi posts
                    /*to indicate that the post is liked by this (SignedIn) user i mean current user
                     *change drawable left button from like button
                     * change text of "Like" to "Liked"*/
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likeBtn.setText("Liked");

                } else {
                    //user not liked this posts
                    /*to indicate that the post is not  liked by this (SignedIn) user i mean current user
                     *change drawable left button from like button
                     * change text of "Liked" to "Like"*/
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        /*get total number of likes for the posts
         *if currently signed in user has not liked it before
         * increase value by one , otherwise decrease value by one.
         */
        mProgressLike = true;
        //get id of the post clicked.
        final DatabaseReference likeReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProgressLike) {//if mProcessLike have true
                    if (dataSnapshot.child(postId).hasChild(myUid)) {//jodi current user er id("myUid") jodi Likes node e thake. tar mane
                        //already liked , so remove like
                        postsReference.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) - 1));//"pLikes" node a like koto hobe  seta set korlam
                        likeReference.child(postId).child(myUid).removeValue();
                        mProgressLike = false;

                    } else {//not liked, so like it
                        postsReference.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes) + 1));
                        likeReference.child(postId).child(myUid).setValue("Liked");//you can set any value
                        mProgressLike = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment.....");
        //get data from comment editText
        String comment = commentEt.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "comment is empty", Toast.LENGTH_SHORT).show();
            commentEt.requestFocus();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());
        // each post will have a child  "Comments" that will contain comments of that's post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");//here make coments node
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);
        // now put data in database
        reference.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //comment added
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();//now set comment number in database
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to added comment
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "failed to added comment" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void updateCommentCount() {
        //whenever user add comment incress the comment count as we did for like count
        mProgressComment = true;//jodi comment kora hoi tahole process ta true hobe
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);//make database node for store comment number
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProgressComment) {//process ta true hole ...
                    String comments = "" + dataSnapshot.child("pComments").getValue();
                    int newCommentsVal = Integer.parseInt(comments) + 1;
                    reference.child("pComments").setValue("" + newCommentsVal);
                    mProgressComment = false;//jehoto comment er poriman add hoiya gese database e so process ta false
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadUserInfo() {
        //for get current user info
        Query query = FirebaseDatabase.getInstance().getReference("Users");
        query.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();
                    //now set data
                    try {
                        //if image is received then saved
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_face_img).into(cAvatarIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face_img).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadPostInfo() {
        //for load post info using post id from firebase database "Posts" node
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep cheaking the post util get required post
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescription = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    hisDp = "" + ds.child("uDp").getValue();
                    hisUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    hisName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();
                    //convert timeStamp to proper format
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calendar).toString();
                    //now set the getting data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescription);
                    pLikesTv.setText(pLikes + " Likes");
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText(commentCount + " Comments");

                    uNameTv.setText(hisName);
                    //set image to user who posted
                    //set post image
                    //if there is no image i mean when we get post without any image then hide image view
                    if (pImage.equals("noImage")) {//if post without any image

                        pImageIv.setVisibility(View.GONE);
                    } else {//if post have image
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }
                    //set user image in comment part
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_face_img).into(uPictureIv);
                    } catch (Exception e) {
                        Picasso.get().load(hisDp).into(uPictureIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();

        } else {
            //user not Signed in go t MainActivity
            startActivity(new Intent(PostDetailActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        //hide some menu items
        menu.findItem(R.id.actionAddPost).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}