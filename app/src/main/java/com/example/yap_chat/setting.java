package com.example.yap_chat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.Map;

public class setting extends AppCompatActivity {
    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Uri setImageUri;
    String email, password;
    ProgressDialog progressDialog;

    Cloudinary cloudinary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "kami01",
                "api_key", "469246935425585",
                "api_secret", "dokxMz3mHWzx-ubyZTULGSCfodQ"));

        // Initialize UI elements
        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebutt);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving...");
        progressDialog.setCancelable(false);

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());

        // Load current user data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email = snapshot.child("mail").getValue(String.class);
                password = snapshot.child("password").getValue(String.class);
                String name = snapshot.child("userName").getValue(String.class);
                String profile = snapshot.child("profilepic").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                setname.setText(name);
                setstatus.setText(status);
                Picasso.get().load(profile).into(setprofile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // Select new profile picture
        setprofile.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        // Save updated profile information
        donebut.setOnClickListener(view -> {
            progressDialog.show();

            String name = setname.getText().toString();
            String status = setstatus.getText().toString();

            if (setImageUri != null) {
                // Upload image to Cloudinary
                new Thread(() -> {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(setImageUri);
                        Map uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                        String finalImageUri = (String) uploadResult.get("secure_url");

                        // Save updated user data in Firebase
                        saveUserData(reference, name, status, finalImageUri);
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(setting.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            } else {
                // Use existing profile picture URL
                reference.child("profilepic").get().addOnSuccessListener(snapshot -> {
                    String existingImageUri = snapshot.getValue(String.class);
                    saveUserData(reference, name, status, existingImageUri);
                });
            }
        });
    }

    private void saveUserData(DatabaseReference reference, String name, String status, String imageUri) {
        Users users = new Users(auth.getUid(), name, email, password, imageUri, status);
        reference.setValue(users).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(setting.this, "Data is saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(setting.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            } else {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(setting.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);
        }
    }
}
