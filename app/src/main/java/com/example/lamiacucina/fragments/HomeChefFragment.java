package com.example.lamiacucina.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lamiacucina.R;
import com.example.lamiacucina.RecipeDetailActivity;
import com.example.lamiacucina.adapter.TrendingRecyclerViewAdapter;
import com.example.lamiacucina.model.Ingredient;
import com.example.lamiacucina.model.Recipe;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeChefFragment extends Fragment implements TrendingRecyclerViewAdapter.ItemClickListener {
    CircleImageView ProfileImage;
    TextView UserNameTxt;
    TextView RecipesCount;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    TrendingRecyclerViewAdapter trendingRecyclerViewAdapter;
    private SliderView sliderView;
    private final ArrayList<Recipe> currentList = new ArrayList<>();
    private View no_Trending_ads_layout;
    CardView SeeRecipes;
    FragmentManager fragmentManager;
    Context context;

    public HomeChefFragment(FragmentManager supportFragmentManager, Context c) {
        // Required empty public constructor
        fragmentManager = supportFragmentManager;
        context = c;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        UserNameTxt = view.findViewById(R.id.UserNameTxt);
        no_Trending_ads_layout = view.findViewById(R.id.no_Trending_ads_layout);
        sliderView = view.findViewById(R.id.imageSlider);
        RecipesCount = view.findViewById(R.id.RecipesCount);
        SeeRecipes = view.findViewById(R.id.CardView);

        SeeRecipes.setOnClickListener(view12 -> {
            fragmentManager.beginTransaction().replace(R.id.flFragment, new ViewAllRecipesFragment()).commit();
            //  ChefActivity.change();
        });

        databaseReference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String Name = snapshot.child("PersonName").getValue(String.class);
                    UserNameTxt.setText(context.getResources().getString(R.string.hello_with_name, Name));
                } else
                    UserNameTxt.setText(getResources().getString(R.string.hello));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        startSlider();

        databaseReference.child("Recipes").addListenerForSingleValueEvent(new ValueEventListener() {
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
                    if (count != 0) {
                        RecipesCount.setText(getResources().getString(R.string.recipes_count, count));
                    } else
                        RecipesCount.setText(getResources().getString(R.string.recipes_no_count));
            } else
            {
                RecipesCount.setText(getResources().getString(R.string.recipes_no_count));
            }
        }

        @Override
        public void onCancelled (@NonNull DatabaseError databaseError){

        }
    });

        return view;
}

    private void startSlider() {
        trendingRecyclerViewAdapter = new TrendingRecyclerViewAdapter(context, this, currentList);
        sliderView.setSliderAdapter(trendingRecyclerViewAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINDEPTHTRANSFORMATION); //set animation for slider
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT); //set rotation type for slider
        sliderView.setIndicatorSelectedColor(Color.WHITE); //set indicator selected color
        sliderView.setIndicatorUnselectedColor(Color.GRAY); //set indicator Unselected color
        sliderView.setScrollTimeInSec(3); //set slide time
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        getTrending();
    }

    private void getTrending() {
        databaseReference.child("Recipes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot eachAdRecord : snapshot.getChildren()) {
                        Recipe recipe = new Recipe();
                        recipe.setID(eachAdRecord.getKey());
                        recipe.setTitle(eachAdRecord.child("title").getValue(String.class));
                        recipe.setImage(eachAdRecord.child("Image").getValue(String.class));
                        recipe.setInstruction(eachAdRecord.child("Instructions").getValue(String.class));

                        for (DataSnapshot ingredients : eachAdRecord.child("ingredients").getChildren()) {
                            recipe.addIngredient(ingredients.getValue(Ingredient.class));
                        }

                        currentList.add(recipe);
                    }

                    if (!currentList.isEmpty()) {
                        ShowTrending();
                        trendingRecyclerViewAdapter.renewItems(currentList);
                    } else {
                        HideTrending();
                    }
                } else {
                    HideTrending();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ShowTrending() {
        no_Trending_ads_layout.setVisibility(View.GONE);
        sliderView.setVisibility(View.VISIBLE);
    }

    private void HideTrending() {
        no_Trending_ads_layout.setVisibility(View.VISIBLE);
        sliderView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position >= 0) {
            Intent o = new Intent(context, RecipeDetailActivity.class);
            o.putExtra("Recipe", currentList.get(position));
            startActivity(o);
        }
    }
}