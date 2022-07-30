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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lamiacucina.LocationActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.adapter.IngredientListAdaptor;
import com.example.lamiacucina.model.Ingredient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditDemandListFragment extends Fragment {
    public FirebaseAuth mAuth;
    ArrayList<Ingredient> al;
    IngredientListAdaptor md;
    RecyclerView rv;
    View NoRecordFoundView;
    DatabaseReference databaseReference;
    ProgressBar progressBar;
    Context context;
    TextView findStores;

    public EditDemandListFragment(Context c) {
        // Required empty public constructor
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_demand_list, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        findStores = view.findViewById(R.id.findStores);

        findStores.setOnClickListener(view1 -> startActivity(new Intent(context, LocationActivity.class)));

        progressBar = view.findViewById(R.id.progressBar);
        NoRecordFoundView = view.findViewById(R.id.noRcdFnd);
        NoRecordFoundView.setVisibility(View.GONE);

        rv = view.findViewById(R.id.recyclerViewDemandIngredients);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(context);
        rv.setLayoutManager(rlm);

        mAuth = FirebaseAuth.getInstance();

        al = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("ingredients").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        String Quantity = eachAdRecord.child("IngredientQuantity").getValue(String.class);
                        String ThresholdValue = eachAdRecord.child("IngredientThresholdValue").getValue(String.class);

                        double quantity = Double.parseDouble(Quantity);
                        double thresholdValue = Double.parseDouble(ThresholdValue);

                        if (thresholdValue >= quantity)
                        {
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
                        md = new IngredientListAdaptor(context, al,true);
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
        return view;
    }
}