package com.example.lamiacucina.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lamiacucina.PantryManagerActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.StartActivity;
import com.example.lamiacucina.activity.recipe.AddRecipeActivity;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomePantryManagerFragment extends Fragment {
    CircleImageView ProfileImage;
    TextView UserNameTxt;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    FragmentManager fragmentManager;
    CardView SeePlannedMeals;
    Context context;

    public HomePantryManagerFragment(FragmentManager supportFragmentManager,Context c) {
        // Required empty public constructor
        fragmentManager = supportFragmentManager;
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_pantry_manager, container, false);

        UserNameTxt = view.findViewById(R.id.UserNameTxt);
        SeePlannedMeals = view.findViewById(R.id.PlannedMealsCard);

        SeePlannedMeals.setOnClickListener(view12 -> {
            fragmentManager.beginTransaction().replace(R.id.flFragment, new ViewScheduleMealPlansFragment(context)).commit();
           // PantryManagerActivity.change();
        });



        databaseReference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String Name = snapshot.child("PersonName").getValue(String.class);
                    UserNameTxt.setText(context.getResources().getString(R.string.hello_with_name,Name));
                } else
                    UserNameTxt.setText(getResources().getString(R.string.hello));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}