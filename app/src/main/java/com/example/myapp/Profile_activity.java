package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Profile_activity extends Fragment {
    private static final String ARG_USER = "currentUser";
    private static final String UpdateIdecator = "UpdatedIdecator";
    private static final int PICK_IMAGE_REQUEST = 1;
    private User currentUser;
    private OnIndecatorUpdate updateIndecator;
    private ImageView profileImageView;
    private database dbHelper;

    // Factory method using Parcelable
    public static Profile_activity newInstance(User currentUser, OnIndecatorUpdate updateIndecator) {
        Profile_activity fragment = new Profile_activity();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, currentUser); // Using putParcelable
        args.putParcelable(UpdateIdecator, updateIndecator);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = getArguments().getParcelable(ARG_USER);
            updateIndecator = getArguments().getParcelable(UpdateIdecator);
        }
        dbHelper = new database(getContext());
        updateIndecator.Update_indecator();
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
        ImageView editname = view.findViewById(R.id.editname);
        ImageView editEmail = view.findViewById(R.id.editEmail);

        // Set up click listener for profile image
        profileImageView.setOnClickListener(v -> openImageChooser());

        Log.d("ProfileActivity", "onCreateView: currentUser is " + (currentUser == null ? "null" : currentUser.toString()));
        if (currentUser != null) {
            Log.d("ProfileActivity", "onCreateView: currentUser.getName() is " + currentUser.getName());
            Username.setText("Name: " + currentUser.getName());
            Useremail.setText("Email: " + currentUser.getEmailAddress());
            Userstarted.setText("Started: " + currentUser.getStarted());

            // Load existing avatar if any
            loadUserAvatar();
        } else {
            Log.d("ProfileActivity", "onCreateView: currentUser is null, cannot set text.");
            Username.setText("User Name: Not available"); // Or some other default
            Useremail.setText("Email: Not available");
        }

        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create custom dialog layout
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_email, null);

                // Initialize views
                TextInputLayout emailInputLayout = dialogView.findViewById(R.id.emailInputLayout);
                TextInputEditText emailEditText = dialogView.findViewById(R.id.emailEditText);
                emailEditText.setText(currentUser.getEmailAddress());

                // Create the dialog
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                        getContext(),
                        R.style.ThemeOverlay_MaterialComponents_Dialog
                )
                        .setTitle("Update Email Address")
                        .setView(dialogView)
                        .setPositiveButton("Save", null) // Set to null to override default behavior
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(view -> {
                        String newEmail = emailEditText.getText().toString().trim();

                        // Validation
                        if (newEmail.isEmpty()) {
                            emailInputLayout.setError("Email cannot be empty");
                            return;
                        }

                        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                            emailInputLayout.setError("Please enter a valid email");
                            return;
                        }

                        if (newEmail.equals(currentUser.getEmailAddress())) {
                            dialog.dismiss();
                            return;
                        }

                        // Update email
                        boolean updated = dbHelper.updatedUserEmail(currentUser.getId(), newEmail);
                        if (updated) {
                            currentUser.setEmailAddress(newEmail);
                            Useremail.setText("Email: " + newEmail);
                            Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            emailInputLayout.setError("Failed to update email");
                        }
                    });
                });

                // Clear error when typing
                emailEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        emailInputLayout.setError(null);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                dialog.show();
            }
        });





        editname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a custom dialog layout
                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_name, null);

                // Initialize views
                TextInputLayout nameInputLayout = dialogView.findViewById(R.id.nameInputLayout);
                TextInputEditText nameEditText = dialogView.findViewById(R.id.nameEditText);
                nameEditText.setText(currentUser.getName());

                // Create the dialog
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.ThemeOverlay_MaterialComponents_Dialog)
                        .setTitle("Edit Your Name")
                        .setView(dialogView)
                        .setPositiveButton("Save", null) // Set to null to override default behavior
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(view -> {
                        String newName = nameEditText.getText().toString().trim();

                        // Validation
                        if (newName.isEmpty()) {
                            nameInputLayout.setError("Name cannot be empty");
                            return;
                        }

                        if (newName.equals(currentUser.getName())) {
                            dialog.dismiss();
                            return;
                        }

                        if (!isValidName(newName)) {
                            nameInputLayout.setError("Please enter a valid name (letters and spaces only)");
                            return;
                        }

                        // Update name
                        boolean updated = dbHelper.updatedusername(currentUser.getId(), newName);
                        if (updated) {
                            currentUser.setName(newName);
                            Username.setText("Name: " + newName);
                            Toast.makeText(getContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            nameInputLayout.setError("Failed to update name");
                        }
                    });
                });

                // Clear error when typing
                nameEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        nameInputLayout.setError(null);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                dialog.show();
            }
        });
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

    // Email validation helper method
    private boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // Name validation helper method
    private boolean isValidName(String name) {
        // Allows letters, spaces, and common name characters
        String nameRegex = "^[\\p{L} .'-]+$";
        return name.matches(nameRegex);
    }
}