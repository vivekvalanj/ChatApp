package com.example.yap_chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class splash extends AppCompatActivity {

    ImageView logo;
    TextView name, from, owner;
    Animation topanim, bottomanim;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        logo = findViewById(R.id.logoimg);
        name = findViewById(R.id.appname);
        from = findViewById(R.id.from);
        owner = findViewById(R.id.owner);

        topanim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        bottomanim = AnimationUtils.loadAnimation(this, R.anim.bottomaim);

        logo.setAnimation(topanim);
        name.setAnimation(bottomanim);
        from.setAnimation(bottomanim);
        owner.setAnimation(bottomanim);

        // Delay for splash screen
        new Handler().postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                // If user is logged in, go to MainActivity
                Intent intent = new Intent(splash.this, MainActivity.class);
                startActivity(intent);
            } else {
                // If user is not logged in, go to Registration
                Intent intent = new Intent(splash.this, registration.class);
                startActivity(intent);
            }
            finish();
        }, 4000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
