package com.example.lamiacucina.activity.family;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lamiacucina.R;
import com.example.lamiacucina.Signup;
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

public class AddNewFamilyMemberActivity extends AppCompatActivity {
    EditText mEmail;
    Button addToFamily;
    ProgressBar progressBar;
    Spinner RoleSpinner;
    ArrayAdapter<String> adapterRoles;
    ArrayList<String> RolesList = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_family_member);

        mEmail      = findViewById(R.id.user_email);
        addToFamily= findViewById(R.id.addToFamily);
        RoleSpinner = findViewById(R.id.RoleSpinner);

        RolesList.add("Chef");
        RolesList.add("Pantry Manager");

        adapterRoles = new ArrayAdapter<>(AddNewFamilyMemberActivity.this, android.R.layout.simple_spinner_item, RolesList);
        adapterRoles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        RoleSpinner.setAdapter(adapterRoles);

        progressBar = findViewById(R.id.progressBar);

        addToFamily.setOnClickListener(v -> {
            final String email = mEmail.getText().toString().trim();

            if(TextUtils.isEmpty(email)){
                mEmail.setError("Email is Required.");
                mEmail.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            databaseReference.child("FamilyIds").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists())
                    {
                        boolean exists = false;
                        for (DataSnapshot s: snapshot.getChildren()) {
                            if (s.child("Email").exists() && !s.child("Email").getValue().equals("") && s.child("Email").getValue().equals(email))
                            {
                                Toast.makeText(AddNewFamilyMemberActivity.this, "Email already Exists", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                exists = true;
                                break;
                            }
                        }
                        if (!exists){
                            SaveDB(email);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    private void SaveDB(String email) {
        String MyFamilyID = new BaseUtil(this).getFamilyID();

        Map<String,Object> user = new HashMap<>();
        user.put("Role",RolesList.get(RoleSpinner.getSelectedItemPosition()));
        user.put("Email",email);
        user.put("FamilyID",MyFamilyID);

        String Id = databaseReference.child("FamilyIds").push().getKey();
        databaseReference.child("FamilyIds").child(Id).setValue(user);
        Toast.makeText(AddNewFamilyMemberActivity.this, "Member Added", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        mEmail.setText("");
    }
}