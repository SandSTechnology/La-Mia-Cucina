package com.example.lamiacucina.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lamiacucina.activity.kitchen_log.AddIngredientKitchenLogActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.activity.kitchen_log.ViewIngredientKitchenLogActivity;

public class KitchenLogFragment extends Fragment {
    CardView addIngredient,viewIngredient,modifyIngredient;
    Context context;

    public KitchenLogFragment(Context c) {
        // Required empty public constructor
        context = c ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kitchen_log, container, false);

        addIngredient = view.findViewById(R.id.addIngredient);
        viewIngredient = view.findViewById(R.id.viewIngredient);
        modifyIngredient = view.findViewById(R.id.modifyIngredient);
        addIngredient.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), AddIngredientKitchenLogActivity.class)));
        viewIngredient.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), ViewIngredientKitchenLogActivity.class)));
        modifyIngredient.setOnClickListener(view1 -> startActivity(new Intent(getActivity(),
                        ViewIngredientKitchenLogActivity.class).putExtra("Editable", "true")));

        return view;
    }
}