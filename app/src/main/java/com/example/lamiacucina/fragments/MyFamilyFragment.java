package com.example.lamiacucina.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.lamiacucina.R;
import com.example.lamiacucina.activity.family.AddNewFamilyMemberActivity;
import com.example.lamiacucina.activity.kitchen_log.ViewIngredientKitchenLogActivity;
import com.example.lamiacucina.adapter.FamilyListAdaptor;
import com.example.lamiacucina.adapter.IngredientListAdaptor;
import com.example.lamiacucina.model.Family;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyFamilyFragment extends Fragment {
    public FirebaseAuth mAuth;
    ArrayList<Family> al;
    FamilyListAdaptor md;
    RecyclerView rv;
    View NoRecordFoundView;
    DatabaseReference databaseReference;
    ProgressBar progressBar;
    Button AddFamilyMemberBtn;
    Context context;

    public MyFamilyFragment(Context c) {
        // Required empty public constructor
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_family, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        AddFamilyMemberBtn = view.findViewById(R.id.addFamilyMemberBtn);

        progressBar = view.findViewById(R.id.progressBar);
        NoRecordFoundView = view.findViewById(R.id.noRcdFnd);
        NoRecordFoundView.setVisibility(View.GONE);

        rv = view.findViewById(R.id.recyclerViewFamily);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(context);
        rv.setLayoutManager(rlm);

        mAuth = FirebaseAuth.getInstance();

        al = new ArrayList<>();

        AddFamilyMemberBtn.setOnClickListener(view1 -> startActivity(new Intent(context, AddNewFamilyMemberActivity.class)));

        progressBar.setVisibility(View.VISIBLE);

        GetMyData();

        return view;
    }

    private void GetMyData() {
        String MyFamilyID = new BaseUtil(context).getFamilyID();
        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        String thisFamilyID = eachAdRecord.child("FamilyID").getValue().toString();
                        if (MyFamilyID.equals(thisFamilyID))
                        {
                            Family p = new Family();
                            p.setID(eachAdRecord.getKey());

                            p.setName(eachAdRecord.child("PersonName").getValue(String.class));
                            p.setRole(eachAdRecord.child("Role").getValue(String.class));
                            p.setFamilyId(eachAdRecord.child("FamilyID").getValue(String.class));
                            p.setEmail(eachAdRecord.child("Email").getValue(String.class));
                            p.setIsAccountCreated(eachAdRecord.child("AccountCreated").getValue(String.class));

                            al.add(p);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    if (!al.isEmpty()) {
                        NoRecordFoundView.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        md = new FamilyListAdaptor(context, al);
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