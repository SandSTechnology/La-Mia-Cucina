package com.example.lamiacucina;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    Button btnLogin,btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In Activity's onCreate() for instance
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_start);

        btnSignup =findViewById(R.id.registerBtn);
        btnLogin =findViewById(R.id.btnLogin);

        btnSignup.setOnClickListener(view -> {
            Intent i= new Intent(StartActivity.this, Signup.class);
            startActivity(i);
            finish();
        });
        btnLogin.setOnClickListener(view -> {
            Intent i= new Intent(StartActivity.this, Login.class);
            startActivity(i);
            finish();
        });
    }
}