package com.example.chatapplication.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.modelAll.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;
    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        //setData
        holder.messageTv.setText(message);
        holder.timeTv.setText(timeStamp);
        try {
            Picasso.get().load(imageUrl).into(holder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.common_google_signin_btn_text_dark_normal_background).into(holder.profileIv);
        }

        holder.messageLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are You Sure To Delete This Message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
        //set seen/delivered status of message
        if (position == chatList.size() - 1) {
            if (chatList.get(position).isSeen()) {
                holder.isSeenTv.setText("Seen");
            } else {
                holder.isSeenTv.setText("Delivered");
            }
        } else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*Logic:
         *get timestamp of click message
         * compare the timestamp of click message with all message in chats.
         * where both value matches delete that message*/
        String getTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Chats");
        //chat list e chat er jei sms e click korbe setar time stamp er sathe firebase er timestamp ta compare korte hobe
        Query query = dr.orderByChild("timestamp").equalTo(getTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.send);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    /*if you want to allow sender to delete only his message the compare sender value with current user uId
                    if its matches means... its the message of sender that is trying to delete */
                    if (ds.child("sender").getValue().equals(myUID)) {
                                /*we can do one of two things here
                            1.remove the message from chats.
                            2.set the value of message like "This message is deleted" so lets go
                     */
                        //  1.remove the message from chats and here don't have any sms.
                                 //ds.getRef().removeValue();////if you want try this line aganist below three line code happy coding
                        // 2.delete message and set the value of message like "This message is deleted".
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("message", "This message is deleted");
                        ds.getRef().updateChildren(map);
                        mediaPlayer.start();
                        FancyToast.makeText(context, "Message Deleted", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true);
                    } else {
                        FancyToast.makeText(context, "You Can Delete Only Your Message", FancyToast.LENGTH_LONG, FancyToast.WARNING, true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {//after Overrideing method
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView profileIv;
        TextView messageTv, isSeenTv, timeTv;
        LinearLayout messageLayout;//This is for click to show delete message options


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIvv);
            messageTv = itemView.findViewById(R.id.messageTvv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);//This is for click to show delete message options
        }
    }
}
