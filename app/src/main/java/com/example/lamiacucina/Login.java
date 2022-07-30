package com.example.lamiacucina;

import android.content.Intent;
import android.content.LocusId;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;

import java.util.Objects;

public class Login extends AppCompatActivity {
    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView forgotPasswordTextView;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.user_email);
        mPassword = findViewById(R.id.user_password);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        mLoginBtn = findViewById(R.id.btnLogin);
        forgotPasswordTextView=findViewById(R.id.forgotPassword);

        forgotPasswordTextView.setOnClickListener(v -> StartForgotPasswordActivity());

        mLoginBtn.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required.");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required.");
                return;
            }

            if (password.length() < 6) {
                mPassword.setError("Password Must be >= 6 Characters");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // authenticate the user

            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null)
                    {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    if (snapshot.child("Role").exists())
                                    {
                                        String Role = snapshot.child("Role").getValue(String.class);
                                        String FamilyID = snapshot.child("FamilyID").getValue(String.class);

                                        new BaseUtil(Login.this).SetLoginRole(Role);
                                        new BaseUtil(Login.this).SetFamilyID(FamilyID);
                                        new BaseUtil(Login.this).SetLoggedIn(true);

                                        if (Objects.requireNonNull(Role).equals("Chef"))
                                            startActivity(new Intent(Login.this, ChefActivity.class));
                                        else if (Role.equals("Pantry Manager"))
                                            startActivity(new Intent(Login.this, PantryManagerActivity.class));
                                        else
                                            startActivity(new Intent(Login.this, StartActivity.class));

                                        Toast.makeText(getApplicationContext(), "Logged in Successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        startActivity(new Intent(Login.this, StartActivity.class));
                                    }
                                }
                                else{
                                    startActivity(new Intent(Login.this, StartActivity.class));
                                }
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                } else {
                    Toast.makeText(Login.this, "User not found. Invalid credentials", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,StartActivity.class));
        this.finish();
        super.onBackPressed();
    }

    private void StartForgotPasswordActivity()
    {
        Intent intent=new Intent(Login.this,ResetEmailActivity.class);
        startActivity(intent);
        finish();
    }
}
