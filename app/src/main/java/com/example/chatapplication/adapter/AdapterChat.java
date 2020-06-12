package com.example.chatapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.modelAll.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

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
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
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
        //set seen/delivered status of message
        if (position==chatList.size()-1){
            if (chatList.get(position).isSeen()){
                holder.isSeenTv.setText("Seen");
            }else {
                holder.isSeenTv.setText("Delivered");
            }
        }else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
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
        TextView messageTv, isSeenTv,timeTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIvv);
            messageTv = itemView.findViewById(R.id.messageTvv);
           timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
        }
    }
}
