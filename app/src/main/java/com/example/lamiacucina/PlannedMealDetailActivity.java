package com.example.lamiacucina;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lamiacucina.activity.kitchen_log.ViewIngredientKitchenLogActivity;
import com.example.lamiacucina.adapter.IngredientListAdaptor;
import com.example.lamiacucina.adapter.SelectRecipesAdaptor;
import com.example.lamiacucina.adapter.ViewAllRecipesAdaptor;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.model.Meal;
import com.example.lamiacucina.model.Recipe;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlannedMealDetailActivity extends AppCompatActivity {

    TextView MenuName,DurationOfMeal,MealTime,Serving;
    Meal p1;
    RecyclerView SelectedRecipesRecyclerView;
    ArrayList<Recipe> currentList = new ArrayList<>();
    ProgressBar progressBar;
    ViewAllRecipesAdaptor md;
    TextView startCookingTextView;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planned_meal_detail);

        startCookingTextView = findViewById(R.id.startCookingTextView);
        boolean Role = new BaseUtil(this).getLoginRole().equals("Chef");
        if (Role) // Chef
        {
            //Show Start Cooking
            startCookingTextView.setVisibility(View.VISIBLE);
            startCookingTextView.setOnClickListener(view -> new AlertDialog.Builder(PlannedMealDetailActivity.this).setTitle("Start Cooking").setMessage("Are you sure ?")
                    .setPositiveButton("OK", (dialogInterface, i) -> CalculateIngredients())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show());
        }
        else
        {
            startCookingTextView.setVisibility(View.GONE);
        }
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

    private void CalculateIngredients() {
        database.child("Recipes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<Ingredient> IngredientList = new ArrayList<>();
                    ArrayList<Ingredient> KitchenLogIngredientList = new ArrayList<>();

                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        if (p1.getRecipesID().contains(eachAdRecord.getKey()))
                        {
                            for (DataSnapshot ingredients : eachAdRecord.child("ingredients").getChildren()) {
                                IngredientList.add(ingredients.getValue(Ingredient.class));
                            }
                        }
                    }
                    String MyFamilyID = new BaseUtil(PlannedMealDetailActivity.this).getFamilyID();
                    if (!IngredientList.isEmpty()) {
                        //Getting Kitchen Log to Compare with
                        database.child("ingredients").addListenerForSingleValueEvent(new ValueEventListener() {
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

                                            KitchenLogIngredientList.add(p);
                                        }
                                    }
                                    if (!KitchenLogIngredientList.isEmpty()) {
                                        ArrayList<Ingredient> MissingIngredients = new ArrayList<>();
                                        ArrayList<Ingredient> ShouldUpdateDemandListIngredients = new ArrayList<>();

                                        boolean UnableToCook = false;

                                        //compare
                                        for (int i= 0 ; i < IngredientList.size() ; i++)
                                        {
                                            boolean matched = false;
                                            for(int ii = 0 ; ii < KitchenLogIngredientList.size() ; ii++)
                                            {
                                                if (IngredientList.get(i).getIngredientName()
                                                        .equalsIgnoreCase(KitchenLogIngredientList.get(ii).getIngredientName()))
                                                {
                                                    Log.e("Ingredients","Matched");

                                                    double myRecipeIngredientQuantity = Double.parseDouble(IngredientList.get(i).getIngredientQuantity());
                                                    double myKitchenLogIngredientQuantity = Double.parseDouble(KitchenLogIngredientList.get(i).getIngredientQuantity());

                                                    double difference = myKitchenLogIngredientQuantity - myRecipeIngredientQuantity;

                                                    KitchenLogIngredientList.get(i).setIngredientQuantity(String.valueOf(difference));

                                                    ShouldUpdateDemandListIngredients.add(KitchenLogIngredientList.get(i));

                                                    if (difference < 0)
                                                    {
                                                        UnableToCook = true;
                                                        //Should Add into Demand List and Show Error of Unable to cook
                                                    }

                                                    matched = true;
                                                    break;
                                                }
                                            }

                                            if (!matched)
                                            {
                                                MissingIngredients.add(IngredientList.get(i));
                                            }
                                        }

                                        if (MissingIngredients.size()>0)
                                        {
                                            for (int i = 0 ; i< MissingIngredients.size() ; i++)
                                            {
                                                String key = database.child("ingredients").push().getKey();

                                                Map<String, Object> ingredient = new HashMap<>();
                                                ingredient.put("IngredientName", MissingIngredients.get(i).getIngredientName());
                                                double quantity = Double.parseDouble(MissingIngredients.get(i).getIngredientQuantity()); // Convert it to Minus
                                                ingredient.put("IngredientQuantity", String.valueOf(-quantity));
                                                ingredient.put("IngredientUnit", MissingIngredients.get(i).getIngredientUnit());
                                                ingredient.put("IngredientThresholdValue", MissingIngredients.get(i).getIngredientThresholdValue());
                                                ingredient.put("FamilyID", new BaseUtil(PlannedMealDetailActivity.this).getFamilyID());

                                                assert key != null;
                                                database.child("ingredients").child(key).setValue(ingredient);
                                            }
                                            new AlertDialog.Builder(PlannedMealDetailActivity.this)
                                                    .setTitle("Unable to Cook")
                                                    .setMessage("Missing Ingredients, Please add required Ingredients to Cook")
                                                    .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                                                    .show();
                                        }
                                        else if (UnableToCook) // low on Ingredients
                                        {
                                            for (int i = 0 ; i < ShouldUpdateDemandListIngredients.size() ; i++)
                                            {
                                                String key = ShouldUpdateDemandListIngredients.get(i).getID();

                                                Map<String, Object> ingredient = new HashMap<>();
                                                ingredient.put("IngredientName", ShouldUpdateDemandListIngredients.get(i).getIngredientName());
                                                ingredient.put("IngredientQuantity", String.valueOf(ShouldUpdateDemandListIngredients.get(i).getIngredientQuantity()));
                                                ingredient.put("IngredientUnit", ShouldUpdateDemandListIngredients.get(i).getIngredientUnit());
                                                ingredient.put("IngredientThresholdValue", ShouldUpdateDemandListIngredients.get(i).getIngredientThresholdValue());
                                                ingredient.put("FamilyID", new BaseUtil(PlannedMealDetailActivity.this).getFamilyID());

                                                assert key != null;
                                                database.child("ingredients").child(key).updateChildren(ingredient);
                                            }

                                            new AlertDialog.Builder(PlannedMealDetailActivity.this)
                                                    .setTitle("Unable to Cook")
                                                    .setMessage("Low on Ingredients, Please add required Ingredients to Cook")
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                        }
                                                    })
                                                    .show();
                                        }
                                        else // Start Cooking
                                        {
                                            for (int i = 0 ; i < ShouldUpdateDemandListIngredients.size() ; i++)
                                            {
                                                String key = ShouldUpdateDemandListIngredients.get(i).getID();

                                                Map<String, Object> ingredient = new HashMap<>();
                                                ingredient.put("IngredientName", ShouldUpdateDemandListIngredients.get(i).getIngredientName());
                                                ingredient.put("IngredientQuantity", String.valueOf(ShouldUpdateDemandListIngredients.get(i).getIngredientQuantity()));
                                                ingredient.put("IngredientUnit", ShouldUpdateDemandListIngredients.get(i).getIngredientUnit());
                                                ingredient.put("IngredientThresholdValue", ShouldUpdateDemandListIngredients.get(i).getIngredientThresholdValue());
                                                ingredient.put("FamilyID", new BaseUtil(PlannedMealDetailActivity.this).getFamilyID());

                                                assert key != null;
                                                database.child("ingredients").child(key).updateChildren(ingredient);
                                            }

                                            new AlertDialog.Builder(PlannedMealDetailActivity.this)
                                                    .setTitle("Start Cooking")
                                                    //.setMessage("Low on Ingredients, Please add required Ingredients to Cook")
                                                    .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                                                    .show();
                                        }
                                    } else {
                                        //Show Unable to Cook and Set Ingredients in minus
                                        for (int i = 0 ; i< IngredientList.size() ; i++)
                                        {
                                            String key = database.child("ingredients").push().getKey();

                                            Map<String, Object> ingredient = new HashMap<>();
                                            ingredient.put("IngredientName", IngredientList.get(i).getIngredientName());
                                            double quantity = Double.parseDouble(IngredientList.get(i).getIngredientQuantity()); // Convert it to Minus
                                            ingredient.put("IngredientQuantity", String.valueOf(-quantity));
                                            ingredient.put("IngredientUnit", IngredientList.get(i).getIngredientUnit());
                                            ingredient.put("IngredientThresholdValue", IngredientList.get(i).getIngredientThresholdValue());
                                            ingredient.put("FamilyID", new BaseUtil(PlannedMealDetailActivity.this).getFamilyID());

                                            assert key != null;
                                            database.child("ingredients").child(key).setValue(ingredient);
                                        }
                                        new AlertDialog.Builder(PlannedMealDetailActivity.this)
                                                .setTitle("Unable to Cook")
                                                .setMessage("Missing Ingredients, Please add required Ingredients to Cook")
                                                .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                                                .show();
                                    }
                                } else {
                                    //Show Unable to Cook and Set Ingredients in minus
                                    for (int i = 0 ; i< IngredientList.size() ; i++)
                                    {
                                        String key = database.child("ingredients").push().getKey();

                                        Map<String, Object> ingredient = new HashMap<>();
                                        ingredient.put("IngredientName", IngredientList.get(i).getIngredientName());
                                        double quantity = Double.parseDouble(IngredientList.get(i).getIngredientQuantity()); // Convert it to Minus
                                        ingredient.put("IngredientQuantity", String.valueOf(-quantity));
                                        ingredient.put("IngredientUnit", IngredientList.get(i).getIngredientUnit());
                                        ingredient.put("IngredientThresholdValue", IngredientList.get(i).getIngredientThresholdValue());
                                        ingredient.put("FamilyID", new BaseUtil(PlannedMealDetailActivity.this).getFamilyID());

                                        assert key != null;
                                        database.child("ingredients").child(key).setValue(ingredient);
                                    }
                                    new AlertDialog.Builder(PlannedMealDetailActivity.this)
                                            .setTitle("Unable to Cook")
                                            .setMessage("Missing Ingredients, Please add required Ingredients to Cook")
                                            .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(PlannedMealDetailActivity.this, "Ingredients not exists", Toast.LENGTH_SHORT).show();
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