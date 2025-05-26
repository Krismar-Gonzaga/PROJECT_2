package com.example.myapp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;

public class Profile_activity extends Fragment {
    private static final String ARG_USER = "currentUser";
    private User currentUser;

    // Factory method using Parcelable
    public static Profile_activity newInstance(User currentUser) {
        Profile_activity fragment = new Profile_activity();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, currentUser); // Using putParcelable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = getArguments().getParcelable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_activity, container, false);

        // Initialize your TextViews
        TextView Username = view.findViewById(R.id.Username);
        TextView Useremail = view.findViewById(R.id.User_email);
        TextView Userstore = view.findViewById(R.id.User_store_name);
        TextView Userstarted = view.findViewById(R.id.User_started);

        Log.d("ProfileActivity", "onCreateView: currentUser is " + (currentUser == null ? "null" : currentUser.toString()));
        if (currentUser != null) {
            Log.d("ProfileActivity", "onCreateView: currentUser.getName() is " + currentUser.getName());
            Username.setText("User Name: " + currentUser.getName());
            Useremail.setText("Email: " + currentUser.getEmailAddress());
            Userstore.setText("Store Name: " + currentUser.getStore_name());
            Userstarted.setText("Started: " + currentUser.getStarted());
        } else {
            Log.d("ProfileActivity", "onCreateView: currentUser is null, cannot set text.");
            Username.setText("User Name: Not available"); // Or some other default
            Useremail.setText("Email: Not available");
        }
        return view;
    }




}