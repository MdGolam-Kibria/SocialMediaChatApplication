package com.example.chatapplication.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.AddPostActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.ThereProfileActivity;
import com.example.chatapplication.modelAll.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;
    String myUid;
    private DatabaseReference likesRef;//for likes database node.
    private DatabaseReference postsRef;//Reference of posts.
    boolean mProcessLike = false;

    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//for get current  current user
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final String uId = postList.get(position).getUid();
        String uEmail = postList.get(position).getUEmail();
        String uName = postList.get(position).getUName();
        String uDp = postList.get(position).getUDp();
        final String pId = postList.get(position).getPId();
        String pTitle = postList.get(position).getPTitle();
        String pDescription = postList.get(position).getPDescr();
        final String pImage = postList.get(position).getPImage();
        String pTimeStap = postList.get(position).getPTime();
        String pLikes = postList.get(position).getPLikes();//contains total number of likes for a post.


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStap));
        String pTime = DateFormat.format("dd/MM/yyy hh:mm aa", calendar).toString();

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikesTv.setText(pLikes + " Likes");//set total number of likes for a post
        setLikes(holder,pId);
        //set user dp
        try {

        } catch (Exception e) {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_img).into(holder.uPictureIv);
        }
        //set post image

        //if there is no image i mean when we get post without any image then hide image view
        if (pImage.equals("noImage")) {//if post without any image

            holder.pImageIv.setVisibility(View.GONE);
        } else {//if post have image
            holder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }

        //now handle button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implement later
                myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                // Toast.makeText(context, "more", Toast.LENGTH_SHORT).show();
                showMoreOptions(holder.moreBtn, uId, myUid, pId, pImage);
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /*get total number of likes for the posts
                *if currently signed in user has not liked it before
                * increase value by one , otherwise decrease value by one.
                */
               final int pLikes = Integer.parseInt(postList.get(position).getPLikes());//total like of a post
               mProcessLike=true;
               //get id of the post clicked.
                final String postId  = postList.get(position).getPId();//get id of the post clicked.
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (mProcessLike){//if mProcessLike have true
                                if (dataSnapshot.child(postId).hasChild(myUid)){//jodi current user er id("myUid") jodi Likes node e thake. tar mane
                                    //already liked , so remove like
                                    postsRef.child(postId).child("pLikes").setValue(""+(pLikes-1));//"pLikes" node a like koto hobe  seta set korlam
                                    likesRef.child(postId).child(myUid).removeValue();
                                    mProcessLike = false;
                                  }else {//not liked, so like it
                                    postsRef.child(postId).child("pLikes").setValue(""+(pLikes+1));
                                    likesRef.child(postId).child(myUid).setValue("Liked");//you can set any value
                                    mProcessLike = false;
                                }
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implement later
                Toast.makeText(context, "comment", Toast.LENGTH_SHORT).show();
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implement later
                Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {//for show specific user profile.
            @Override
            public void onClick(View v) {
                /*
                click to go there profile activity with uid , This uid is of clicked user
                        *which will be used to show user specific data/posts
                 */
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uId);
                context.startActivity(intent);
            }
        });

    }

    private void setLikes(final MyHolder holder, final String pId) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(pId).hasChild(myUid)){//ei ref node e jodi current user er id thake
                    //user has liked thi posts
                    /*to indicate that the post is liked by this (SignedIn) user i mean current user
                    *change drawable left button from like button
                    * change text of "Like" to "Liked"*/
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("Liked");

                }else {
                    //user not liked this posts
                    /*to indicate that the post is not  liked by this (SignedIn) user i mean current user
                     *change drawable left button from like button
                     * change text of "Liked" to "Like"*/
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uId, String myUid, final String pId, final String pImage) {//for control more option in posts
        //creating popup menu currently having options Delete ,,we will add more option later
        final PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
        //show delete option in only post(s) of currently singed in user so..........
        if (uId.equals(myUid)) {//mane post er moddhe je gular id current user er sathe match korbe segula sei current user delete korte parbe
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
                    beginDelete(pId, pImage);
                } else if (id == 1) {
                    //delete is clicked
                    //start add post activity with key "editText" and the id of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }


    private void beginDelete(String pId, String pImage) {
        //post can be with or without image so.....
        if (pImage.equals("noImage")) {//post without image
            deleteWithoutImage(pId, pImage);
        } else {//post with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {//delete with image
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
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
                        .orderByChild("pId").equalTo(pId);//this "pId" must match with AdapterPost Model class.
                fQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();//remove value from firebase where "pId" matches
                        }
                        //delete success
                        Toast.makeText(context, "Deleting Successfully", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId, String pImage) {//delete without image
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting.....");
        //image deleted now delete database
        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts")//for deleting post from firebase "Posts" path
                .orderByChild("pId").equalTo(pId);//this "pId" must match with AdapterPost Model class.
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();//remove value from firebase where "pId" matches
                }
                //delete success
                Toast.makeText(context, "Deleting Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        //views from row_post.xml
        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //int views

            //imageview
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            //textview
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.uTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            //image button
            moreBtn = itemView.findViewById(R.id.moreBtn);
            //button
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);

        }
    }
}
