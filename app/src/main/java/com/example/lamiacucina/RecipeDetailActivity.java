package com.example.lamiacucina;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lamiacucina.activity.recipe.CustomizeRecipeActivity;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.model.Recipe;
import com.squareup.picasso.Picasso;

public class RecipeDetailActivity extends AppCompatActivity {
    ImageView RecipeImage;
    TextView ItemCount;
    TextView IngredientsTextView;
    TextView InstructionsTextView;
    TextView RecipeTitle;
    TextView EditRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Recipe recipe = (Recipe) getIntent().getSerializableExtra("Recipe");

        if (recipe==null)
        {
            Toast.makeText(this, "No Recipe", Toast.LENGTH_SHORT).show();
            return;
        }

        RecipeImage = findViewById(R.id.RecipeImage);
        ItemCount = findViewById(R.id.ItemCount);
        RecipeTitle = findViewById(R.id.RecipeTitle);
        IngredientsTextView = findViewById(R.id.IngredientsTextView);
        InstructionsTextView = findViewById(R.id.InstructionsTextView);
        EditRecipe =findViewById(R.id.EditRecipe);

        EditRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecipeDetailActivity.this, CustomizeRecipeActivity.class).putExtra("recipe",recipe));
            }
        });

        ItemCount.setText(recipe.getIngredients().size() + " Items");
        RecipeTitle.setText(recipe.getTitle());
        InstructionsTextView.setText(recipe.getInstruction());

        if (recipe.getImage() != null)
            Picasso.get().load(recipe.getImage()).into(RecipeImage);
        else
            RecipeImage.setImageResource(R.drawable.oil);

        StringBuilder Ingredient = new StringBuilder();

        for (Ingredient ingredient : recipe.getIngredients()) {
            Ingredient.append(ingredient.toString()).append("\n\n");
        }

        IngredientsTextView.setText(Ingredient.toString());
    }
}