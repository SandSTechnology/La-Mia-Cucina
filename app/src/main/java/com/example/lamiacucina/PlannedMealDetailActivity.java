package com.example.lamiacucina;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lamiacucina.adapter.SelectRecipesAdaptor;
import com.example.lamiacucina.adapter.ViewAllRecipesAdaptor;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.model.Meal;
import com.example.lamiacucina.model.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlannedMealDetailActivity extends AppCompatActivity {

    TextView MenuName,DurationOfMeal,MealTime,Serving;
    Meal p1;
    RecyclerView SelectedRecipesRecyclerView;
    ArrayList<Recipe> currentList = new ArrayList<>();
    ProgressBar progressBar;
    ViewAllRecipesAdaptor md;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_meal_detail);

        p1 = (Meal) getIntent().getSerializableExtra("meal");

        progressBar = findViewById(R.id.progressBar);
        MenuName = findViewById(R.id.MenuName);
        DurationOfMeal = findViewById(R.id.DurationOfMeal);
        MealTime = findViewById(R.id.MealTime);
        Serving = findViewById(R.id.Serving);

        SelectedRecipesRecyclerView = findViewById(R.id.recyclerViewSelectedRecipes);
        RecyclerView.LayoutManager rlm = new LinearLayoutManager(this);
        SelectedRecipesRecyclerView.setLayoutManager(rlm);

        MenuName.setText("Menu Name : " + p1.getMenuName());
        DurationOfMeal.setText("DurationOfMeal : " + p1.getDurationOfMeal());
        MealTime.setText("Meal Time : " + p1.getMealTime());
        //MealType.setText("Meal Type : " + p1.getMenuType());
        Serving.setText("Serving : " + p1.getServing());

        GetAllSelectedRecipes();
        //Recipes.setText("Recipes include : " + p1.getRecipesID().size());
    }

    void GetAllSelectedRecipes()
    {
        currentList.clear();
        FirebaseDatabase.getInstance().getReference().child("Recipes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        if (p1.getRecipesID().contains(eachAdRecord.getKey()))
                        {
                            Recipe recipe = new Recipe();
                            recipe.setID(eachAdRecord.getKey());
                            recipe.setTitle(eachAdRecord.child("title").getValue(String.class));
                            recipe.setImage(eachAdRecord.child("Image").getValue(String.class));
                            recipe.setInstruction(eachAdRecord.child("Instructions").getValue(String.class));
                            recipe.setSelected(false);

                            for (DataSnapshot ingredients : eachAdRecord.child("ingredients").getChildren()) {
                                recipe.addIngredient(ingredients.getValue(Ingredient.class));
                            }

                            currentList.add(recipe);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    if (!currentList.isEmpty()) {
                        md = new ViewAllRecipesAdaptor(PlannedMealDetailActivity.this, currentList, false);
                        SelectedRecipesRecyclerView.setAdapter(md);
                    } else {
                        SelectedRecipesRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}