package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
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

import com.example.chatapplication.adapter.AdapterChat;
import com.example.chatapplication.convertImage.StringImageCodeToBitmap;
import com.example.chatapplication.modelAll.ModelChat;
import com.example.chatapplication.notificatons.ApiService;
import com.example.chatapplication.notificatons.Client;
import com.example.chatapplication.notificatons.Data;
import com.example.chatapplication.notificatons.Response;
import com.example.chatapplication.notificatons.Sender;
import com.example.chatapplication.notificatons.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    //for checking if user has seen message or not.
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;
    ApiService apiService;
    boolean notify = false;

    String hisUid;
    String myUid;
    String hisImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(ApiService.class);
  /*
        On clicking user from users list we have passed that's user UID using intent
        *so get that UID here to get the profile picture, name and start chat with that
        *user*/
        Intent intent = getIntent();//in oncreate view
        hisUid = intent.getStringExtra("hisUid");//in oncreate view

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");
        //search user to get that user's info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check untill required info is rechived
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();
                    //check typing status
                    if (typingStatus.equals(myUid)) {
                        userStatusTv.setText(name + " Typing now....");
                    } else {
                        //get value of oline status
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")) {
                            userStatusTv.setText(onlineStatus);
                        } else {//jodi online na hoi tahole last seen er time ta firebase theke ekhane dekhabe.
                            userStatusTv.setText(onlineStatus);
                        }
                    }


                    //set data
                    nameTv.setText(name);
                    try {
                        //image received set it to imageview in toolbar
                        //Picasso.get().load(hisImage).placeholder(R.drawable.ic_default).into(profileIv);
                        Bitmap bitmap = StringImageCodeToBitmap.jsonimageConvertTOBitmap(hisImage);
                        profileIv.setImageBitmap(bitmap);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_face_img).into(profileIv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //check editText change listener
        messageEt.addTextChangedListener(new TextWatcher() {//for check user currently typing or not
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {//if don't have any data in edit text
                    checkTypingStatus("noOneTyping");
                } else {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        readMessage();//in oncreate view
        seenMessage();//in oncreate view


        //click send button to send message

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    messageEt.setError("message is empty");
                    messageEt.requestFocus();
                    return;
                } else {
                    sendMessage(message);
                }
                //reset editText after sending message
                messageEt.setText("");
            }
        });


    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                        HashMap<String, Object> hasSeenHasMap = new HashMap<>();
                        hasSeenHasMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHasMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Chats");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message) {

        /*
        "Chats" node will be created that will contains all chats
        * whenever user send message  it will create new child in firebase database in "Chats" node and that child will contain
        * that follwing  key values
        *sender : UID of sender
        * rechiver : UID of receiver
        * message : The actual message
         */
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //String timeStamp = String.valueOf(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String timeStamp = formatter.format(date);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);


        DatabaseReference drefer = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        drefer.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("MyTag", ds.getValue(String.class));
                    //  Log.d("sms",ds.child("name").getValue(String.class));
                    //ModelUser user = ds.getValue(ModelUser.class);
                    if (notify) {
                        sendNotification(hisUid, ds.child("name").getValue(String.class), message);//problem here can't show user name get null
                    }
                    notify = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(final String hisUid, final String name, final String message) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        final String timeStamp = formatter.format(date);

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(myUid, name + ";" + message, timeStamp, hisUid, R.drawable.ic_face_img);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, "" + response.message(), Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {//check user sign in or not and accessibility
        FirebaseUser user = firebaseAuth.getCurrentUser();//get current user
        if (user != null) {
            //user signed in stay here
//            mProfileTv.setText(user.getEmail());
            myUid = user.getUid();//currently signed in user's UID
        } else {
            //user not signed in
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    private void checkOnlineStatus(String status) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> map = new HashMap<>();
        map.put("onlineStatus", status);
        //update value of online status of current user.
        dRef.updateChildren(map);
    }

    private void checkTypingStatus(String typing) {//check typing status user currently typing or not.
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> map = new HashMap<>();
        map.put("typingTo", typing);
        //update value of online status of current user.
        dRef.updateChildren(map);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online status
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String timeStamp = formatter.format(date);
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOneTyping");
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set Online status
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        //hide search view and add post as we don't need it here
        menu.findItem(R.id.action_search).setVisible(false);//remove search icon from here......
        menu.findItem(R.id.actionAddPost).setVisible(false);//remove search icon from here......
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