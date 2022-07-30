package com.example.lamiacucina.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lamiacucina.ChefActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.SelectRecipesActivity;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class MealPlannerFragment extends Fragment {
    Button SelectRecipeButton;
    EditText MenuName,DurationOfMeal,MealTime,Serving;
    TextView RecipesCount;
    DatabaseReference databaseReference;
    TextView plannedMealsTV;
    FragmentManager fragmentManager;
    Context context;

    public MealPlannerFragment(FragmentManager supportFragmentManager,Context c) {
        // Required empty public constructor
        fragmentManager = supportFragmentManager;
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_planner, container, false);
        SelectRecipeButton = view.findViewById(R.id.SelectRecipeButton);
        MenuName = view.findViewById(R.id.MenuName);
        DurationOfMeal = view.findViewById(R.id.DurationOfMeal);
        MealTime = view.findViewById(R.id.MealTime);
        Serving = view.findViewById(R.id.Serving);
        RecipesCount = view.findViewById(R.id.RecipesCount);

        plannedMealsTV = view.findViewById(R.id.plannedMealsTV);

        plannedMealsTV.setOnClickListener(view12 -> {
            fragmentManager.beginTransaction().replace(R.id.flFragment, new ViewScheduleMealPlansFragment(context)).commit();
        });

        SelectRecipeButton.setOnClickListener(view1 -> {
            HashMap<String,String> data = GetData();
            if (data!=null) {
                startActivity(new Intent(context, SelectRecipesActivity.class).putExtra("data", data));
                MenuName.setText("");
                DurationOfMeal.setText("");
                MealTime.setText("");
                Serving.setText("");
            }else
                Toast.makeText(context, "Please fill all required Data", Toast.LENGTH_SHORT).show();
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("Meals").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int count = 0;
                    for (DataSnapshot eachAdRecord : dataSnapshot.getChildren()) {
                        if (eachAdRecord.child("FamilyID").exists() && !Objects.equals(eachAdRecord.child("FamilyID").getValue(String.class), "")) {
                            String mFamilyID = eachAdRecord.child("FamilyID").getValue(String.class);
                            String FamilyID = new BaseUtil(context).getFamilyID();

                            if (Objects.equals(mFamilyID, FamilyID)) {
                                count++;
                            }
                        }
                    }
                        if (count != 0)
                        {
                            RecipesCount.setText(getResources().getString(R.string.meals_count,count));
                        }
                        else
                            RecipesCount.setText(getResources().getString(R.string.meals_no_count));
                } else {
                    RecipesCount.setText(getResources().getString(R.string.meals_no_count));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private HashMap<String, String> GetData() {
        HashMap<String, String> data = new HashMap<>();

        String menuName;
        String durationOfMeal;
        String mealTime;
        String serving;

        if (MenuName.getText() !=null && !MenuName.getText().toString().equals(""))
            menuName = MenuName.getText().toString();
        else
        {
            MenuName.setError("Please Add Menu Name");
            MenuName.requestFocus();
            return null;
        }

        if (DurationOfMeal.getText() !=null && !DurationOfMeal.getText().toString().equals(""))
            durationOfMeal = DurationOfMeal.getText().toString();
        else
        {
            DurationOfMeal.setError("Please Add Meal Duration");
            DurationOfMeal.requestFocus();
            return null;
        }

        if (MealTime.getText() !=null && !MealTime.getText().toString().equals(""))
            mealTime = MealTime.getText().toString();
        else
        {
            MealTime.setError("Please Add Meal Time");
            MealTime.requestFocus();
            return null;
        }

        if (Serving.getText() !=null && !Serving.getText().toString().equals(""))
            serving = Serving.getText().toString();
        else
        {
            Serving.setError("Please Add Number of Serving");
            Serving.requestFocus();
            return null;
        }

        data.put("MenuName",menuName);
        data.put("DurationOfMeal",durationOfMeal);
        data.put("MealTime",mealTime);
        data.put("Serving",serving);

        return data;
    }
}