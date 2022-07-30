package com.example.lamiacucina.activity.kitchen_log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lamiacucina.R;
import com.example.lamiacucina.adapter.IngredientListAdaptor;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewIngredientKitchenLogActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    ArrayList<Ingredient> al;
    IngredientListAdaptor md;
    RecyclerView rv;
    View NoRecordFoundView;
    DatabaseReference databaseReference;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ingredient_kitchen_log);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        String Editable = intent.getStringExtra("Editable");

        progressBar = findViewById(R.id.progressBar);
        NoRecordFoundView = findViewById(R.id.noRcdFnd);
        NoRecordFoundView.setVisibility(View.GONE);

        rv = findViewById(R.id.recyclerViewIngredients);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(this);
        rv.setLayoutManager(rlm);

        mAuth = FirebaseAuth.getInstance();

        al = new ArrayList<>();

        String MyFamilyID = new BaseUtil(this).getFamilyID();
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("ingredients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        String thisFamilyID = eachAdRecord.child("FamilyID").getValue().toString();
                        if (MyFamilyID.equals(thisFamilyID)) {
                            Ingredient p = new Ingredient();
                            p.setID(eachAdRecord.getKey());

                            p.setIngredientName(eachAdRecord.child("IngredientName").getValue(String.class));
                            p.setIngredientQuantity(eachAdRecord.child("IngredientQuantity").getValue(String.class));
                            p.setIngredientUnit(eachAdRecord.child("IngredientUnit").getValue(String.class));
                            p.setIngredientThresholdValue(eachAdRecord.child("IngredientThresholdValue").getValue(String.class));

                            al.add(p);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    if (!al.isEmpty()) {
                        NoRecordFoundView.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        md = new IngredientListAdaptor(ViewIngredientKitchenLogActivity.this, al, Boolean.parseBoolean(Editable));
                        rv.setAdapter(md);
                    } else {
                        NoRecordFoundView.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    NoRecordFoundView.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}