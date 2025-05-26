package com.example.myapp;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText username ,emailInput, passwordInput, confirmPasswordInput;
    Button signUpButton;
    TextView signInText;

    database db;
    String usertype;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout); // Make sure your XML file is named activity_sign_up.xml
        db = new database(MainActivity.this);

        // Initialize views
        username = findViewById(R.id.username);
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.signup_btn);
        signInText = findViewById(R.id.sign_btn);
        adduser();


        // "Sign In" text click listener
        signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace with actual LoginActivity if you have one
                Toast.makeText(MainActivity.this, "Go to Sign In screen", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, Login.class));
            }
        });


    }

    public void adduser() {
        // Sign Up button logic
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = username.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                LogManager.getInstance().log("Signup attempt: " + email);

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(name) ) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    LogManager.getInstance().log("Signup failed: empty fields");
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    LogManager.getInstance().log("Signup failed: passwords do not match for " + email);
                } else {
                    Cursor cursor = db.getUsers();
                    if (cursor.getCount() == 0){
                        usertype = "admin";
                    }else{
                        usertype = "user";
                    }

                    User account_login = new User("", email, password, usertype, name, "", "");
                    boolean inserted = db.insertdata(account_login);
                    if (inserted) {
                        Toast.makeText(MainActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                        LogManager.getInstance().log("Signup success: " + email);
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Email already exists!", Toast.LENGTH_SHORT).show();
                        LogManager.getInstance().log("Signup failed: email exists for " + email);
                    }
                }
            }
        });
    }





}
