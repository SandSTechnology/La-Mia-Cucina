package com.example.lamiacucina.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lamiacucina.EditDemandListActivity;
import com.example.lamiacucina.R;
import com.example.lamiacucina.model.Ingredient;

import java.util.ArrayList;

public class AddIngredientListAdaptor extends RecyclerView.Adapter<AddIngredientListAdaptor.MyHolder> {
    Context ct;
    ArrayList<Ingredient> al;
    Boolean Editable;

    public AddIngredientListAdaptor(Context cont, ArrayList<Ingredient> al, Boolean editable) {
        this.ct = cont;
        this.al = al;
        Editable = editable;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ct);
        View v = li.inflate(R.layout.delete_ingredient_recyclerview_item, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AddIngredientListAdaptor.MyHolder holder, final int position) {
        final Ingredient p1 = al.get(position);

        if (p1.getIngredientName()!=null && !p1.getIngredientName().equals(""))
            holder.IngredientName.setText(ct.getResources().getString(R.string.ingredient_name_in_list,p1.getIngredientName().trim(),p1.getIngredientQuantity().trim(),p1.getIngredientUnit().trim()));
        else
            holder.IngredientName.setVisibility(View.GONE);

        holder.image.setOnClickListener(view -> removeAt(holder.getAdapterPosition()));

        if (Editable)
            holder.cld.setOnClickListener(view -> ct.startActivity(new Intent(ct, EditDemandListActivity.class).putExtra("Ingredient", p1)));
    }

    public void removeAt(int position) {
        al.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, al.size());
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView IngredientName;
        CardView cld;

        public MyHolder(View itemView) {
            super(itemView);
            cld = itemView.findViewById(R.id.IngredientDetailCard);
            image = itemView.findViewById(R.id.delete);
            IngredientName = itemView.findViewById(R.id.IngredientName);
        }
    }
}