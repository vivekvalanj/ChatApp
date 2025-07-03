package com.example.yap_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
public class registration extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 10;

    TextView login;
    EditText rg_username, rg_email, rg_password, rg_re_password;
    Button signup;
    CircleImageView rg_profileImg;

    FirebaseAuth auth;
    FirebaseDatabase database;
    Uri imageURI;
    String imageUrl;
    String emailPattern = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}+";

    // Cloudinary configuration
    Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "kami01",
                "api_key", "469246935425585",
                "api_secret", "dokxMz3mHWzx-ubyZTULGSCfodQ"));

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Set up UI elements
        signup = findViewById(R.id.signup);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_re_password = findViewById(R.id.rg_re_Password);
        rg_profileImg = findViewById(R.id.profilerg0);
        login = findViewById(R.id.login);

        // Login button click listener
        login.setOnClickListener(v -> {
            Intent intent = new Intent(registration.this, login.class);
            startActivity(intent);
            finish();
        });

        // Sign-up button click listener
        signup.setOnClickListener(v -> {
            String name = rg_username.getText().toString();
            String email = rg_email.getText().toString();
            String password = rg_password.getText().toString();
            String confirmPassword = rg_re_password.getText().toString();
            String status = "Using this application";

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(registration.this, "Please enter all information", Toast.LENGTH_SHORT).show();
            } else if (!email.matches(emailPattern)) {
                rg_email.setError("Type a valid email");
            } else if (password.length() < 8) {
                rg_password.setError("Password must be 8 characters or more");
            } else if (!password.equals(confirmPassword)) {
                rg_password.setError("Passwords don't match");
            } else {
                // Create Firebase user
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = task.getResult().getUser().getUid();
                        DatabaseReference reference = database.getReference().child("user").child(userId);

                        if (imageURI != null) {
                            // Upload image to Cloudinary
                            new Thread(() -> {
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(imageURI);
                                    Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                                    imageUrl = (String) uploadResult.get("secure_url");

                                    // Save user data in Firebase Database
                                    Users user = new Users(userId, name, email, password, imageUrl, status);
                                    reference.setValue(user).addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            runOnUiThread(() -> {
                                                Intent intent = new Intent(registration.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            });
                                        } else {
                                            runOnUiThread(() -> Toast.makeText(registration.this, "Error in creating user", Toast.LENGTH_SHORT).show());
                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(() -> Toast.makeText(registration.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            }).start();
                        } else {
                            // If no image selected, use a default image
                            imageUrl = "https://res.cloudinary.com/kami01/image/upload/v1732976725/r6jwumsunzrivxiy88bv.png";
                            Users user = new Users(userId, name, email, password, imageUrl, status);
                            reference.setValue(user).addOnCompleteListener(saveTask -> {
                                if (saveTask.isSuccessful()) {
                                    Intent intent = new Intent(registration.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(registration.this, "Error in creating user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(registration.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Profile image click listener
        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });
    }

    // Handle the result of selecting an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
