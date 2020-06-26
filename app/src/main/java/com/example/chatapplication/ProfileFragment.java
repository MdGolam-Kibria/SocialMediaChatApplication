package com.example.chatapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.adapter.AdapterPost;
import com.example.chatapplication.convertImage.BitmapImageToString;
import com.example.chatapplication.convertImage.StringImageCodeToBitmap;
import com.example.chatapplication.modelAll.ModelPost;
import com.example.chatapplication.notificatons.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    Bitmap bitmap;
    ImageView avaterIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;
    RecyclerView postsRecyclerview;
    ProgressDialog pd;
    //
    public static final int IMAGE_PIC_CAMERA_CODE_for_profile_pic = 1;
    public static final int IMAGE_PIC_CAMERA_CODE_for_coverPhoto = 2;
    //permission contains
    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int STORAGE_REQUEST_CODE = 200;
    public static final int IMAGE_PIC_GALLERY_CODE = 300;
    public static final int IMAGE_PIC_CAMERA_CODE = 400;
    //ARRAYS OF PERMISSION TO BE REQUESTED
    String cameraPermission[];
    String storagePermission[];
    //uri for picked image
    Uri image_uri;
    //..for chacking profilr or cover photo
    String profileOrCoverPhoto;
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;
    //path where storage of user profile and cover photo will be stored.
    String storagePath = "Users_Profile_Cover_Imgs/";
    private String myUid;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        avaterIv = view.findViewById(R.id.avaterIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerview = view.findViewById(R.id.recyclerview_posts);
        pd = new ProgressDialog(getActivity());
        fab.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();//firebase storage refarence
        //int arrays of permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};






         /*we have to get info of currently signed user,we can get it using user email or user id here i want to use email
         ///by using order by child query we will show the detail from node which key name email has value equal currently signed email.
         it will search all node where the key matches it will get its detail
          */
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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
        checkUserStatus();
        loadMyPosts();
        return view;
    }

    private void loadMyPosts() {
        //linearLayout for recyclerview
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
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
                    adapterPost = new AdapterPost(getActivity(), postList);
                    //add this adapter in recyclerview
                    postsRecyclerview.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void searchMyPosts(final String searchQuery) {
        //linearLayout for recyclerview
        final LinearLayoutManager manager = new LinearLayoutManager(getActivity());
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

                    if (myPosts.getPTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getPDescr().toLowerCase().contains(searchQuery.toLowerCase())) {//This condition for search .
                        postList.add(myPosts);

                    }
                    //now all value add to list
                    postList.add(myPosts);
                    //adapter
                    adapterPost = new AdapterPost(getActivity(), postList);
                    //add this adapter in recyclerview
                    postsRecyclerview.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            showEditProfileDialog();
        }
    }


    private boolean checkStoragePermission() {//for Gallery
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {//for Gallery
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {//for camera
        //check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {//for camera
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //edit profile clicked
                    pd.setMessage("Updating Profile Pic...");
                    profileOrCoverPhoto = "image";//changing profile picture make sure to assign same value.
                    showImagePicDialog(which);
                } else if (which == 1) {
                    // Edit Cover Photo click
                    pd.setMessage("Updating Cover photo....");
                    profileOrCoverPhoto = "cover";//changing profile picture make sure to assign same value.
                    showImagePicDialog(which);
                } else if (which == 2) {
                    // Edit name click
                    pd.setMessage("Updating name....");
                    //calling method and pass key "name" as a parameter  to update its value in database so...
                    showNamePhoneUpdateDialog("name");
                } else if (which == 3) {
                    // Edit phone click
                    pd.setMessage("Updating phone....");
                    showNamePhoneUpdateDialog("phone");

                }
            }
        });
        builder.create().show();
    }


    private void showNamePhoneUpdateDialog(final String key) {
        /*parameter name will contains value :
        - either "name" which is key in user's database which is used to update user's name,
        or
        - either "phone" which is key in user's database which is used to update user's phone,
         */
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("update " + key);//update name or update phone depends on key
        //set Layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter your " + key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)) {//if user input is empty
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);
                    databaseReference.child(user.getUid()).updateChildren(result)//update name/phone
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //if update name/phone here will be response
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //something error here
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "error update name/phone" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    //if user edit his name also change it from hist posts.
                    //mane jodi user name update kora hoi tobe seta firebase er  "users" path er sathe "posts" path e o update hobe
                    if (key.equals("name")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //update name in current users comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")){
                                        String child1 = ""+dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts")
                                                .child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), "please enter your " + key, Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog(int which) {///problem here
        //show dialog containing option camera and gallery to pick the image.
        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCameraSnd(which);
                    }
                    pd.setMessage("Updating Profile Pic...");
//                    showImagePicDialog();
                } else if (which == 1) {
                    //gallery clicked
                    pd.setMessage("Editing Cover photo....");
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method call when user allow or deny from permission request dialog
        //here we will handle permission cases (allow & deny )

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                //picking from camera first check if  camera and storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enabled camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                //picking from storage first check if storage permission allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enabled storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        ////try

        if (requestCode == IMAGE_PIC_CAMERA_CODE_for_profile_pic) {//by camera
            bitmap = (Bitmap) data.getExtras().get("data");
            uploadProfilePhoto(bitmap, "profilePic");
        }
        if (requestCode == IMAGE_PIC_CAMERA_CODE_for_coverPhoto) {
            ///later
            bitmap = (Bitmap) data.getExtras().get("data");
            uploadProfilePhoto(bitmap, "coverPhoto");
        }


        if (requestCode == RESULT_OK) {
            if (requestCode == IMAGE_PIC_GALLERY_CODE) {
                //image is picked from gallery , get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }

        }

    }

    @Override
    public void onStart() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myUid = user.getUid();
        }
        super.onStart();
    }


    private void uploadProfilePhoto(Bitmap bitmap, String profileOrCoverPhoto) {

        //path to store user data named "Users";
        HashMap<String, Object> map = new HashMap<>();
        if (profileOrCoverPhoto.equals("profilePic")) {//for update profile pic
            map.put("image", BitmapImageToString.convert(bitmap));
            databaseReference.child(user.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "profile pic updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "profile pic Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (profileOrCoverPhoto.equals("coverPhoto")) {//for update cover photo
            map.put("cover", BitmapImageToString.convert(bitmap));
            databaseReference.child(user.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "cover photo updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "image upload Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        //show progress
        pd.show();
        //**instead creating saparate function for profile picture and cover photo
        //i am doing work for both in same function
        //
        //to add check ill add a string variable and assign it value "image" when user clicked
        //"Edit Profile Pic" and assign it value "cover" when user clicked edit cover photo.
        //here image is the key in each user containing url of user profile picture.
        //here cover is the key in each user containing url of user cover photo.

        /**the paramiter "image_uri" contains the image uri picked either from camera or gallery
         * we will use UID of the currently signed in user as name of the image so there will be only one image profile and one image for
         * profile each other
         */
        //path and name of image to be stored in firebase storage
        //foe ex: Users_Profile_Cover_Imgs/image_endfjdaknk.jpg
        //foe ex: Users_Profile_Cover_Imgs/cover_endfjdaknk.jpg
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is upload to storage .now gets its url and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        final Uri dawonloadUri = uriTask.getResult();
                        //check if image is uploaded or not and url is received
                        if (uriTask.isSuccessful()) {
                            //image uploaded
                            //add/update url in users database
                            HashMap<String, Object> results = new HashMap<>();
                    /*here first paramiter is  profileOrCoverPhoto that has value "image" or "cover"
                    which are keys in users database where url of image will be saved in one of them
                    secound paramiter contains the url of image stored in firebase storage this url will be saved as value
                     against key "image" or  "cover"*/
                            results.put(profileOrCoverPhoto, dawonloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)//updated here
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added sucessfully
                                            //change progress dialog
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "image updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of user
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "error from update image = " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                            //if user edit his image also change it from hist posts.
                            //mane jodi user image update kora hoi tobe seta firebase er  "users" path er sathe "posts" path e o update hobe
                            if (profileOrCoverPhoto.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(dawonloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                //when you change this part after chage ,,,here must change from video
                                        //*Firebase Social Media App - 23 Add Comments 58:43 sec theke start
                                //update name in current users comments on posts
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments")){
                                                String child1 = ""+dataSnapshot.child(child).getValue();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts")
                                                        .child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(dawonloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        } else {
                            //error here
                            pd.dismiss();
                            Toast.makeText(getActivity(), "some error occurd", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ////there are some error(s) progressDialog will dismiss here
                pd.dismiss();
                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void pickFromCameraSnd(int which) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        if (which == 0) {
            startActivityForResult(takePictureIntent, IMAGE_PIC_CAMERA_CODE_for_profile_pic);
        }
        if (which == 1) {
            startActivityForResult(takePictureIntent, IMAGE_PIC_CAMERA_CODE_for_coverPhoto);
        }
    }

    private void pickFromCamera() {
        //intent of picking image from device camera
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "Temp");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "TempDescription");
//        //put image uri
//        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            startActivityForResult(cameraIntent, IMAGE_PIC_CAMERA_CODE);
        }
    }

    private void pickFromGallery() {
//pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PIC_GALLERY_CODE);
    }

    private void checkUserStatus() {//check user sign in or not and accessibility
        FirebaseUser user = firebaseAuth.getCurrentUser();//get current user
        if (user != null) {
            //user signed in stay here
//            mProfileTv.setText(user.getEmail());
            uid = user.getUid();

        } else {
            //user not signed in
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {//for inflate option menu
        inflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called whrn user press search button
                if (!TextUtils.isEmpty(query)) {
                    searchMyPosts(query);
                } else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user type any latter
                if (!TextUtils.isEmpty(newText)) {
                    searchMyPosts(newText);
                } else {
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//for option item selection handle "logout"
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if (id == R.id.actionAddPost) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
