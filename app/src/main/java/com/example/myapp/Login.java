package com.example.myapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private EditText emaillogin;
    private EditText passwordlogin;
    private Button button;

    private TextView Sign_upButton;

    database db;
    User account_login;
    ArrayList<User> User = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        db = new database(this);
        display();

        // Initialize views
        emaillogin = findViewById(R.id.email);
        passwordlogin = findViewById(R.id.password);
        button = findViewById(R.id.button2);
        Sign_upButton = findViewById(R.id.sign_btn);

        Sign_upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Login.this, "Go to Sign Up screen", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Login.this, MainActivity.class));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emaillogin.getText().toString().trim();
                String password = passwordlogin.getText().toString().trim();

                LogManager.getInstance(getApplicationContext()).log("Login attempt: " + email);

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    LogManager.getInstance(getApplicationContext()).log("Login failed: empty fields");
                } else {
                    boolean notfound = false;
                    for (User user : User) {
                        if (email.equals(user.getEmailAddress().trim()) &&
                                password.equals(user.getPassword().trim())) {
                            User currentUser = user;
                            Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            LogManager.getInstance(getApplicationContext()).log("Login success: " + email);
                            notfound = true;
                            
                            // Redirect to DashboardActivity instead of homeactivity
                            Intent intent = new Intent(Login.this, homeactivity.class);
                            intent.putExtra("CURRENT_USER", currentUser);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Close the login activity
                        }
                    }
                    if (!notfound) {
                        Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        LogManager.getInstance(getApplicationContext()).log("Login failed: invalid credentials for " + email);
                    }
                }
            }
        });
    }

    public void display() {
        Cursor cursor = db.getUsers();
        if (cursor.getCount() == 0) {
            Toast.makeText(Login.this, "No Data!", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                User account = new User(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(4),
                        cursor.getString(3),
                        cursor.getString(5),
                        cursor.getString(6)
                );
                User.add(account);
            }
        }
    }
}