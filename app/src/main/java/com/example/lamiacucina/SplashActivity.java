package com.example.lamiacucina;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String Role = new BaseUtil(this).getLoginRole();
            if (Objects.requireNonNull(Role).equals("Chef"))
                startActivity(new Intent(SplashActivity.this, ChefActivity.class));
            else if (Role.equals("Pantry Manager"))
                startActivity(new Intent(SplashActivity.this, PantryManagerActivity.class));
            else
                startActivity(new Intent(SplashActivity.this, StartActivity.class));
        }
        else
            startActivity(new Intent(SplashActivity.this, StartActivity.class));
        finish();
    }
}