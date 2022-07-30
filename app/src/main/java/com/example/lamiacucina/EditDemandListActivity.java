package com.example.lamiacucina;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lamiacucina.activity.kitchen_log.AddIngredientKitchenLogActivity;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.util.BaseUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditDemandListActivity extends AppCompatActivity {
    private EditText ingredientName,ingredientQuantity,ingredientUnit,ingredientThresholdValue;
    private ProgressBar progressBar;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_demand_list);

        Ingredient preIngredient = (Ingredient) getIntent().getSerializableExtra("Ingredient");

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        ingredientName = findViewById(R.id.ingredientName);
        ingredientQuantity = findViewById(R.id.ingredientQuantity);
        ingredientUnit = findViewById(R.id.ingredientUnit);
        ingredientThresholdValue = findViewById(R.id.ingredientThresholdValue);
        Button updateIngredient = findViewById(R.id.updateIngredient);
        progressBar = findViewById(R.id.progressBar);

        progressBar = findViewById(R.id.progressBar);

        if (preIngredient != null)
        {
            String ID = preIngredient.getID();
            String Name = preIngredient.getIngredientName();
            String Quantity = preIngredient.getIngredientQuantity();
            String Unit = preIngredient.getIngredientUnit();
            String ThresholdValue = preIngredient.getIngredientThresholdValue();

            if (Name !=null && !Name.equals(""))
                ingredientName.setText(Name);
            if (Quantity !=null && !Quantity.equals(""))
                ingredientQuantity.setText(Quantity);
            if (ThresholdValue !=null && !ThresholdValue.equals(""))
                ingredientThresholdValue.setText(ThresholdValue);
            if (Unit !=null && !Unit.equals(""))
                ingredientUnit.setText(Unit);
        }

        updateIngredient.setOnClickListener(view -> {
            if (ingredientName.getText()==null || ingredientName.getText().toString().equals(""))
            {
                ingredientName.setError("Enter Ingredient Name");
                ingredientName.requestFocus();
                return;
            }
            if (ingredientQuantity.getText()==null || ingredientQuantity.getText().toString().equals(""))
            {
                ingredientQuantity.setError("Enter Ingredient Quantity");
                ingredientQuantity.requestFocus();
                return;
            }
            if (ingredientUnit.getText()==null || ingredientUnit.getText().toString().equals(""))
            {
                ingredientUnit.setError("Enter Ingredient Unit");
                ingredientUnit.requestFocus();
                return;
            }
            if (ingredientThresholdValue.getText()==null || ingredientThresholdValue.getText().toString().equals(""))
            {
                ingredientThresholdValue.setError("Enter Ingredient Threshold Value");
                ingredientThresholdValue.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String key = database.getReference("ingredients").push().getKey();

            Map<String,Object> ingredient = new HashMap<>();
            ingredient.put("IngredientName",ingredientName.getText().toString());
            ingredient.put("IngredientQuantity",ingredientQuantity.getText().toString());
            ingredient.put("IngredientUnit",ingredientUnit.getText().toString());
            ingredient.put("IngredientThresholdValue",ingredientThresholdValue.getText().toString());
            ingredient.put("FamilyID",new BaseUtil(this).getFamilyID());

            firebaseDatabase.child("ingredients").child(preIngredient.getID()).updateChildren(ingredient).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(EditDemandListActivity.this, "Ingredient Updated !", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(e ->
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Ingredient not updated !", Toast.LENGTH_SHORT).show();
            });
        });
    }
}