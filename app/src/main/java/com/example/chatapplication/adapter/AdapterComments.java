package com.example.chatapplication.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.modelAll.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {
    Context context;
    List<ModelComment> commentsList;
    String myUid, postId;

    public AdapterComments(Context context, List<ModelComment> commentsList, String myUid, String postId) {
        this.context = context;
        this.commentsList = commentsList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get the data
        final String uid = commentsList.get(position).getUid();
        String name = commentsList.get(position).getUName();
        String email = commentsList.get(position).getUEmail();
        String image = commentsList.get(position).getUDp();//for image
        final String cid = commentsList.get(position).getCId();
        String comment = commentsList.get(position).getComment();
        String timestamp = commentsList.get(position).getTimestamp();
        //convert timestamp to actual format time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String cTime = DateFormat.format("dd/MM/yyy hh:mm aa", calendar).toString();
        //set the data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(cTime);
        //set user dp
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_face_img).into(holder.avatarIv);
        } catch (Exception e) {

        }
//        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return false;
//            }
//        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {//for comment delete
            @Override
            public boolean onLongClick(View v) {
                //check if this comment is by currently signedIn user or not
                if (myUid.equals(uid)) {
                    //this is current user comment ,,,so current user can delete this comment
                    //show delete dialog
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment ?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete comment
                            deleteComment(cid);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });
                    //show dialog
                    builder.create().show();
                } else {
                    //this comment not current user comment
                    Toast.makeText(context, "Can't delete others comment..", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

        ref.child("Comments").child(cid).removeValue();//it will delete the comment

        //now update the database comment count after delete a comment
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = "" + dataSnapshot.child("pComments").getValue();
                int newCommentsVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue("" + String.valueOf(newCommentsVal));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        //declare views form row_comments
        ImageView avatarIv;
        TextView nameTv, commentTv, timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
