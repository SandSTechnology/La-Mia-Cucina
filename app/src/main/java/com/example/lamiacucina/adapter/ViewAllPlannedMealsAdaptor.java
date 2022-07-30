package com.example.lamiacucina.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lamiacucina.PlannedMealDetailActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.model.Meal;
import com.example.lamiacucina.util.BaseUtil;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ViewAllPlannedMealsAdaptor extends RecyclerView.Adapter<ViewAllPlannedMealsAdaptor.MyHolder> {
    Context ct;
    ArrayList<Meal> al;
    Boolean Editable;
    String Role;

    public ViewAllPlannedMealsAdaptor(Context cont, ArrayList<Meal> al, Boolean editable) {
        this.ct = cont;
        this.al = al;
        Editable = editable;

        Role = new BaseUtil(cont).getLoginRole();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ct);
        View v = li.inflate(R.layout.recycler_view_planned_meals_item, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAllPlannedMealsAdaptor.MyHolder holder, final int position) {
        holder.bind(al.get(position),position);
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    public void removeAt(int position) {
        al.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, al.size());
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        TextView MenuName,DurationOfMeal,MealTime,Serving,Recipes,DeleteMeal;
        ImageView Recipe1Image;
        View cld;

        public MyHolder(View itemView) {
            super(itemView);
            cld = itemView.findViewById(R.id.PlannedMealsCard);

            MenuName = itemView.findViewById(R.id.MenuName);
            DurationOfMeal = itemView.findViewById(R.id.DurationOfMeal);
            MealTime = itemView.findViewById(R.id.MealTime);
            Recipe1Image = itemView.findViewById(R.id.Recipe1Image);
            Serving = itemView.findViewById(R.id.Serving);
            Recipes = itemView.findViewById(R.id.Recipes);
            DeleteMeal = itemView.findViewById(R.id.DeleteMeal);
        }

        public void bind(final Meal p1,int position) {
            MenuName.setText("Menu Name : " + p1.getMenuName());
            DurationOfMeal.setText("DurationOfMeal : " + p1.getDurationOfMeal());
            MealTime.setText("Meal Time : " + p1.getMealTime());
            Serving.setText("Serving : " + p1.getServing());
            Recipes.setText("Recipes include : " + p1.getRecipesID().size());

            if (p1.getRecipe1ImageUrl()!=null && !p1.getRecipe1ImageUrl().equals(""))
                Picasso.get().load(p1.getRecipe1ImageUrl()).into(Recipe1Image);
            else
                Recipe1Image.setImageResource(R.mipmap.ic_launcher_round);

            if (Objects.requireNonNull(Role).equals("Chef")) {
                DeleteMeal.setVisibility(View.VISIBLE);
                DeleteMeal.setOnClickListener(view -> {
                    new AlertDialog.Builder(cld.getContext()).setTitle("DELETE")
                            .setMessage("Are you sure you want to delete this Planned Meal")
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                FirebaseDatabase.getInstance().getReference().child("Meals").child(p1.getID()).removeValue();
                                removeAt(position);
                            }).show();
                });
            }
            else
                DeleteMeal.setVisibility(View.GONE);

            cld.setOnClickListener(view -> ct.startActivity(new Intent(ct, PlannedMealDetailActivity.class).putExtra("meal", p1)));
        }
    }
}