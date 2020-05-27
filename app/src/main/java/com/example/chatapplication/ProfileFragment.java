package com.example.chatapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    ProgressDialog progressDialog;
    ImageView avaterIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton floatingActionButton;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_REQUEST_CODE = 400;
    //ARRAYS OF PERMISSION TO BE REQUESTED
    String cameraPermission[];
    String storagePermission[];
    //uri for picked image
    Uri image_uri;
        //..for chacking profilr or cover photo
    String profileOrCoverPhoto;////////////////////////////////////////////////////////////////////stop from here 35:12 video

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        avaterIv = view.findViewById(R.id.avaterIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        coverIv = view.findViewById(R.id.coverIv);
        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.keepSynced(true);
        progressDialog = new ProgressDialog(getActivity());

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
                    Toast.makeText(getContext(), "" + dataSnapshot.child("email").getValue(), Toast.LENGTH_SHORT).show();
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {

                        Picasso.get().load(image).into(avaterIv);
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile_icon).into(avaterIv);
                        Picasso.get().load(R.drawable.user_icon).into(avaterIv);
                        Log.d("image", e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            showEditProfileDialog();
        }
    }

    private boolean checkStoragePermission() {//for Gallery
        //check if storage permission is enabled or not
        //if enabled return true else false.
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_DENIED);
        return result;
    }

    private void requestStoragePermission() {//for Gallery
        ActivityCompat.requestPermissions(getActivity(), storagePermission, STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission() {//for camera
        //check if storage permission is enabled or not
        //if enabled return true else false.
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {//for camera
        ActivityCompat.requestPermissions(getActivity(), cameraPermission, CAMERA_REQUEST_CODE);
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
                    progressDialog.setMessage("Updating Profile Pic...");
                    showImagePicDialog();
                } else if (which == 1) {
                    // Edit Cover Photo click
                    progressDialog.setMessage("Editing Cover photo....");
                } else if (which == 2) {
                    // Edit name click
                    progressDialog.setMessage("Editing name....");
                } else if (which == 3) {
                    // Edit phone click
                    progressDialog.setMessage("Editing phone....");

                }
            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog containing option camera and gallery to pick the image.
        String options[] = {"Gallery", "Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From ");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //Gallery click
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                    progressDialog.setMessage("Updating Profile Pic...");
                    showImagePicDialog();
                } else if (which == 1) {
                    //Camera clicked
                    progressDialog.setMessage("Editing Cover photo....");
                    if (checkStoragePermission()) {
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
                //picking from camera first check camera permission allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enable camera and storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            case STORAGE_REQUEST_CODE: {
                //picking from gallery first check camera permission allowed or not
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        //permission enabled
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking image from camera or gallery
        if (requestCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_REQUEST_CODE){
                //image picked from gallery , get image uri
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }if (requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                //image picked from camera , get image uri
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {
        //**instead creating saparate function for profile picture and cover photo
        //i am doing work for both in same function
        //
        //to add check ill add a string variable and assign it value "image" when user clicked
        //"Edit Profile Pic" and assign it value "cover" when user clicked edit cover photo.
        //here image is the key in each user containing url of user profile picture.
        //here cover is the key in each user containing url of user cover photo.
    }

    private void pickFromCamera() {
    //intent of picking image of device camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"temp pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"temp description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        //intent to start camera
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(camera_intent,IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery() {
//intent of picking image from gallery
        Intent gallery_intent = new Intent(Intent.ACTION_PICK);
        gallery_intent.setType("image/*");
        startActivityForResult(gallery_intent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }
}


