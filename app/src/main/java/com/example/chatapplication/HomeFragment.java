package com.example.chatapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapplication.adapter.AdapterPost;
import com.example.chatapplication.modelAll.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterPost adapterPost;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post last ,for this load from last
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(layoutManager);

        //inti...post list
        postList = new ArrayList<>();
        loadPosts();
        return view;
    }

    private void loadPosts() {
        //path of all posts
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        //now get all data from this databaseReference
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);
                    //adapter
                    adapterPost = new AdapterPost(getActivity(),postList);
                    //now set adapter to recycler view
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case any error from loading data time
                Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchPost(final String searchQuery){
        //path of all posts
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        //now get all data from this databaseReference
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    /*
                    if in search view user enter query match with postTitle and descriptions
                     */
                    if (modelPost.getPTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            modelPost.getPDescr().toLowerCase().contains(searchQuery.toLowerCase())){

                        postList.add(modelPost);
                    }
                    //adapter
                    adapterPost = new AdapterPost(getActivity(),postList);
                    //now set adapter to recycler view
                    recyclerView.setAdapter(adapterPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case any error from loading data time
                Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {//check user sign in or not and accessibility
        FirebaseUser user = firebaseAuth.getCurrentUser();//get current user
        if (user != null) {
            //user signed in stay here
//            mProfileTv.setText(user.getEmail());
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
        //add search view for search post by title/description
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView  = (SearchView) MenuItemCompat.getActionView(menuItem);
        //now search listen...
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {//called when user press search button
                if (!TextUtils.isEmpty(query)){//if user set any search query in search view
                    searchPost(query);
                }else {//if user dont search anything
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {//called as when user press any latter
                if (!TextUtils.isEmpty(query)){//if user set any search query in search view
                    searchPost(query);
                }else {//if user dont search anything
                    loadPosts();
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
