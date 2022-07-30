package com.example.lamiacucina.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lamiacucina.R;
import com.example.lamiacucina.adapter.ViewAllPlannedMealsAdaptor;
import com.example.lamiacucina.model.Meal;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ViewScheduleMealPlansFragment extends Fragment {
    public FirebaseAuth mAuth;
    ArrayList<Meal> currentList;
    ViewAllPlannedMealsAdaptor md;
    RecyclerView rv;
    View NoRecordFoundView;
    DatabaseReference databaseReference;
    ProgressBar progressBar;
    Context context;

    public ViewScheduleMealPlansFragment(Context c) {
        // Required empty public constructor
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_schedule_meal_plans, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressBar = view.findViewById(R.id.progressBar);
        NoRecordFoundView = view.findViewById(R.id.noRcdFnd);
        NoRecordFoundView.setVisibility(View.GONE);

        rv = view.findViewById(R.id.recyclerViewViewMealsPlan);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(context);
        rv.setLayoutManager(rlm);

        mAuth = FirebaseAuth.getInstance();

        currentList = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("Meals").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String FamilyID = new BaseUtil(context).getFamilyID();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        if (eachAdRecord.child("FamilyID").exists() && !Objects.equals(eachAdRecord.child("FamilyID").getValue(String.class), "")) {
                            String mFamilyID = eachAdRecord.child("FamilyID").getValue(String.class);

                            if (Objects.equals(mFamilyID, FamilyID)) {
                                Meal meal = new Meal();
                                meal.setID(eachAdRecord.getKey());
                                meal.setMealTime(eachAdRecord.child("MealTime").getValue(String.class));
                                meal.setDurationOfMeal(eachAdRecord.child("DurationOfMeal").getValue(String.class));
                                meal.setMenuName(eachAdRecord.child("MenuName").getValue(String.class));
                                meal.setServing(eachAdRecord.child("Serving").getValue(String.class));
                                meal.setRecipe1ImageUrl(eachAdRecord.child("Recipe1Image").getValue(String.class));

                                ArrayList<String> RecipesList = new ArrayList<>();
                                for (DataSnapshot recipes : eachAdRecord.child("Recipes").getChildren()) {
                                    RecipesList.add(recipes.getValue(String.class));
                                }
                                meal.setRecipesID(RecipesList);

                                currentList.add(meal);
                            }
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    if (!currentList.isEmpty()) {
                        NoRecordFoundView.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                        md = new ViewAllPlannedMealsAdaptor(context, currentList, false);
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