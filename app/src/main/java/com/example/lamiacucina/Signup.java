package com.example.lamiacucina;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Signup extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName, mEmail, mPassword;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    String userID;
    Spinner RoleSpinner;
    ArrayAdapter<String> adapterRoles;
    ArrayList<String> RolesList = new ArrayList<>();
    TextView FamilyID;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mFullName = findViewById(R.id.user_name);
        mEmail = findViewById(R.id.user_email);
        mPassword = findViewById(R.id.user_password);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginBtn = findViewById(R.id.btnLogin);
        RoleSpinner = findViewById(R.id.RoleSpinner);
        FamilyID = findViewById(R.id.familyID);

        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //if (editable != null && editable.length() != 0) {
                    //String email = editable.toString();
                    //CheckFamilyID(email); TODO get Data of required Email if exists and fill into Field, Make Fields UnEditable.
                //}
            }
        });

        RolesList.add("Chef");
        RolesList.add("Pantry Manager");

        adapterRoles = new ArrayAdapter<>(Signup.this, android.R.layout.simple_spinner_item, RolesList);
        adapterRoles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RoleSpinner.setAdapter(adapterRoles);

        fAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);

        mRegisterBtn.setOnClickListener(v -> {
            final String email = mEmail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();
            final String fullName = mFullName.getText().toString();

            if (TextUtils.isEmpty(fullName)) {
                mFullName.setError("Name is Required.");
                mFullName.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email is Required.");
                mEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                mPassword.setError("Password is Required.");
                mPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                mPassword.setError("Password Must be >= 6 Characters");
                mPassword.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            GetFamilyID(email, fullName);
        });

        mLoginBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Login.class)));
    }

    private void GetFamilyID(String email, String fullName) {
        databaseReference.child("FamilyIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean exists = false;
                        for (DataSnapshot s: snapshot.getChildren()) {
                            if (s.child("Email").exists() && !s.child("Email").getValue().equals("") && s.child("Email").getValue().equals(email))
                            {
                                Toast.makeText(Signup.this, "You are Member of some Family, Redirecting to your Family Data", Toast.LENGTH_LONG).show();
                                exists = true;

                                String PersonName = fullName;
                                String Role = s.child("Role").getValue().toString();
                                String FamilyID = s.child("FamilyID").getValue().toString();

                                SaveDB(PersonName,email,Role,FamilyID);
                                break;
                            }
                        }
                        if (!exists){
                            String FamilyID = databaseReference.child("Families").push().getKey();
                            SaveDB(fullName,email,RoleSpinner.getSelectedItem().toString(),FamilyID);
                        }
                } else {
                    String key = databaseReference.child("FamilyIds").push().getKey();
                    String FamilyID = databaseReference.child("Families").push().getKey();

                    Map<String, Object> user = new HashMap<>();
                    user.put("PersonName", fullName);
                    user.put("Role", RolesList.get(RoleSpinner.getSelectedItemPosition()));
                    user.put("Email", email);
                    user.put("FamilyID", FamilyID);
                    user.put("AccountCreated", "true");

                    databaseReference.child("FamilyIds").child(key).setValue(user);

                    SaveDB(fullName,email,RoleSpinner.getSelectedItem().toString(),FamilyID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SaveDB(String fullName, String email, String Role, String familyID) {
        fAuth.createUserWithEmailAndPassword(email, mPassword.getText().toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Signup.this, "User Created", Toast.LENGTH_SHORT).show();
                userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();

                Map<String, Object> user = new HashMap<>();
                user.put("PersonName", fullName);
                user.put("Role", RolesList.get(RoleSpinner.getSelectedItemPosition()));
                user.put("Email", email);
                user.put("FamilyID", familyID);
                user.put("AccountCreated", "true");

                databaseReference.child("Users").child(userID).setValue(user).addOnSuccessListener(unused -> {
                    new BaseUtil(Signup.this).SetLoginRole(Role);
                    new BaseUtil(Signup.this).SetFamilyID(familyID);
                    new BaseUtil(Signup.this).SetLoggedIn(true);

                    if (RolesList.get(RoleSpinner.getSelectedItemPosition()).equals("Chef"))
                        startActivity(new Intent(getApplicationContext(), ChefActivity.class));
                    else
                        startActivity(new Intent(getApplicationContext(), PantryManagerActivity.class));
                    finish();
                }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e));

            } else {
                Toast.makeText(Signup.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,StartActivity.class));
        this.finish();
        super.onBackPressed();
    }

}
