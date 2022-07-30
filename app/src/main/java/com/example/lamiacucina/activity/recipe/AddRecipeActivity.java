package com.example.lamiacucina.activity.recipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lamiacucina.R;
import com.example.lamiacucina.adapter.AddIngredientListAdaptor;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.util.BaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class AddRecipeActivity extends AppCompatActivity {
    private ProgressDialog pd ;
    EditText recipeName,ingredientName,ingredientQuantity,ingredientUnit,cookingSteps;
    Button selectImage,addIngredient,SaveRecipe;
    RecyclerView ingredientsListRecyclerView;
    ProgressBar progressBar;
    ImageView RecipeImage;
    private Uri filepath;
    private DatabaseReference firebaseDatabase;
    private StorageReference storageReference;
    AddIngredientListAdaptor md;
    ArrayList<Ingredient> al;
    private final int SELECT_PICTURE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        recipeName = findViewById(R.id.recipeName);
        ingredientName = findViewById(R.id.ingredientName);
        ingredientQuantity = findViewById(R.id.ingredientQuantity);
        ingredientUnit = findViewById(R.id.ingredientUnit);
        cookingSteps = findViewById(R.id.cookingSteps);
        selectImage = findViewById(R.id.selectImage);
        addIngredient = findViewById(R.id.addIngredient);
        SaveRecipe = findViewById(R.id.SaveRecipe);
        ingredientsListRecyclerView = findViewById(R.id.ingredientsListRecyclerView);
        ingredientsListRecyclerView.setVisibility(View.GONE);
        RecipeImage = findViewById(R.id.RecipeImage);

        pd = new ProgressDialog(this);

        RecyclerView.LayoutManager rlm = new LinearLayoutManager(this);
        ingredientsListRecyclerView.setLayoutManager(rlm);

        progressBar = findViewById(R.id.progressBar);

        al = new ArrayList<>();

        md = new AddIngredientListAdaptor(this, al,false);
        ingredientsListRecyclerView.setAdapter(md);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

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

            Ingredient p = new Ingredient();
            p.setIngredientName(ingredientName.getText().toString().trim());
            p.setIngredientQuantity(ingredientQuantity.getText().toString().trim());
            p.setIngredientUnit(ingredientUnit.getText().toString().trim());

            al.add(p);

            ingredientsListRecyclerView.setVisibility(View.VISIBLE);
            md = new AddIngredientListAdaptor(this, al,false);
            ingredientsListRecyclerView.setAdapter(md);

            ingredientName.setText("");
            ingredientQuantity.setText("");
            ingredientUnit.setText("");
        });

        selectImage.setOnClickListener(view -> ChooseImage());
        RecipeImage.setOnClickListener(view -> ChooseImage());

        SaveRecipe.setOnClickListener(view -> {
            if (filepath==null)
            {
                Toast.makeText(AddRecipeActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                return;
            }
            if (recipeName.getText() ==null || recipeName.getText().toString().equals(""))
            {
                Toast.makeText(AddRecipeActivity.this, "Please type Recipe Name/Title", Toast.LENGTH_SHORT).show();
                recipeName.requestFocus();
                return;
            }
            if (al.size() == 0)
            {
                Toast.makeText(AddRecipeActivity.this, "Please add some Ingredients", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cookingSteps.getText()==null || cookingSteps.getText().toString().equals(""))
            {
                cookingSteps.setError("Enter Cooking Steps");
                cookingSteps.requestFocus();
                return;
            }
            UploadImage();
        });
    }

    public void UploadImage() {
        StorageReference ref;

        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssZ", Locale.getDefault());
        String path = "IMG-" + df.format(Calendar.getInstance().getTime()) + "AP" + filepath.getLastPathSegment();

        ref = storageReference.child("Recipes").child("Pictures").child(path);
        if (filepath != null) {
            pd.setTitle("Uploading Recipe Image...");
            pd.show();

            ref.putFile(filepath).addOnSuccessListener(taskSnapshot -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(pd::dismiss, 500);
                Task<Uri> result = Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl();
                result.addOnSuccessListener(uri -> {
                    String urlToImage = uri.toString();
                    saveDatabase(urlToImage);
                });
                Toast.makeText(getApplicationContext(), "Recipe Image Uploaded", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(getApplication(), "Recipe Image Uploading failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                pd.setMessage("Uploaded" + (int) progress + "%");
            });
        } else {
            Toast.makeText(getApplication(), "No Recipe Image file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveRecipe(String ID,String title,String instructions,String ImgURL,ArrayList<Ingredient> arrayList) {
        HashMap<String, Object> recipe = new HashMap<>();

        recipe.put("title", title);
        recipe.put("Instructions", instructions);

        for (int i = 0 ; i < arrayList.size() ; i++)
        {
            arrayList.get(i).setID(i+1 + "");
        }

        HashMap<String,Ingredient> ListOfIngredients = new HashMap<>();
        for (int i = 0 ; i < arrayList.size() ; i++)
        {
            ListOfIngredients.put(i+1 + "",arrayList.get(i));
        }

        recipe.put("ingredients", ListOfIngredients);
        recipe.put("Image", ImgURL);
        recipe.put("FamilyID",new BaseUtil(this).getFamilyID());


        firebaseDatabase.child("Recipes").child(ID).setValue(recipe);

        progressBar.setVisibility(View.GONE);

        finish();
    }

    private void saveDatabase(String imgUrl) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseDatabase.child("Recipes").orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ID = "1";
                if (snapshot.exists())
                {
                    String LastID = "";
                    for (DataSnapshot s:snapshot.getChildren() ) {
                        LastID = s.getKey();
                    }
                    assert LastID != null;
                    int LastIntID = Integer.parseInt(LastID);
                    LastIntID++;
                    ID = String.valueOf(LastIntID);
                }

                SaveRecipe(ID,recipeName.getText().toString(),cookingSteps.getText().toString(),imgUrl,al);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ChooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        if (resultcode == RESULT_OK) {
            if (requestcode == SELECT_PICTURE) {
                filepath = data.getData();

                if (filepath != null) {
                    Picasso.get().load(filepath).into(RecipeImage);
                }
            }
        }
    }
}