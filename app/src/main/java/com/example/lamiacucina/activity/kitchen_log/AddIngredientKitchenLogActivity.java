package com.example.lamiacucina.activity.kitchen_log;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class AddIngredientKitchenLogActivity extends AppCompatActivity {
    EditText ingredientName,ingredientQuantity,ingredientUnit,ingredientThresholdValue;
    Button addIngredient;
    ProgressBar progressBar;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient_kitchen_log);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        ingredientName = findViewById(R.id.ingredientName);
        ingredientQuantity = findViewById(R.id.ingredientQuantity);
        ingredientUnit = findViewById(R.id.ingredientUnit);
        ingredientThresholdValue = findViewById(R.id.ingredientThresholdValue);
        addIngredient = findViewById(R.id.addIngredient);
        progressBar = findViewById(R.id.progressBar);

        progressBar = findViewById(R.id.progressBar);

        addIngredient.setOnClickListener(view -> {
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

            assert key != null;
            firebaseDatabase.child("ingredients").child(key).setValue(ingredient).addOnSuccessListener(unused -> {
                Toast.makeText(AddIngredientKitchenLogActivity.this, "Ingredient added !", Toast.LENGTH_SHORT).show();

                ingredientName.setText("");
                ingredientQuantity.setText("");
                ingredientUnit.setText("");
                ingredientThresholdValue.setText("");

                progressBar.setVisibility(View.GONE);
            }).addOnFailureListener(e ->
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Ingredient not added !", Toast.LENGTH_SHORT).show();
            });
        });
    }
}