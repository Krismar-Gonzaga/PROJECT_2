package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Profile_activity extends Fragment {
    private static final String ARG_USER = "currentUser";
    private static final int PICK_IMAGE_REQUEST = 1;
    private User currentUser;
    private ImageView profileImageView;
    private database dbHelper;

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
        dbHelper = new database(getContext());
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_activity, container, false);

        // Initialize views
        profileImageView = view.findViewById(R.id.profile_image);
        TextView Username = view.findViewById(R.id.Username);
        TextView Useremail = view.findViewById(R.id.User_email);
        TextView Userstarted = view.findViewById(R.id.User_started);

        // Set up click listener for profile image
        profileImageView.setOnClickListener(v -> openImageChooser());

        Log.d("ProfileActivity", "onCreateView: currentUser is " + (currentUser == null ? "null" : currentUser.toString()));
        if (currentUser != null) {
            Log.d("ProfileActivity", "onCreateView: currentUser.getName() is " + currentUser.getName());
            Username.setText("User Name: " + currentUser.getName());
            Useremail.setText("Email: " + currentUser.getEmailAddress());
            Userstarted.setText("Started: " + currentUser.getStarted());
            
            // Load existing avatar if any
            loadUserAvatar();
        } else {
            Log.d("ProfileActivity", "onCreateView: currentUser is null, cannot set text.");
            Username.setText("User Name: Not available"); // Or some other default
            Useremail.setText("Email: Not available");
        }
        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK 
            && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                // Compress and resize the bitmap if needed
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] byteArray = stream.toByteArray();
                
                // Update database
                if (currentUser != null && dbHelper.updateUserAvatar(String.valueOf(currentUser.getId()), byteArray)) {
                    // Update ImageView
                    profileImageView.setImageBitmap(bitmap);
                    Toast.makeText(getContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserAvatar() {
        if (currentUser != null) {
            byte[] avatarBytes = dbHelper.getUserAvatar(String.valueOf(currentUser.getId()));
            if (avatarBytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
                profileImageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}