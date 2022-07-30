package com.example.lamiacucina.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lamiacucina.R;
import com.example.lamiacucina.RecipeDetailActivity;
import com.example.lamiacucina.model.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ViewAllRecipesAdaptor extends RecyclerView.Adapter<ViewAllRecipesAdaptor.MyHolder> {
    Context ct;
    ArrayList<Recipe> al;
    Boolean Editable;

    public ViewAllRecipesAdaptor(Context cont, ArrayList<Recipe> al, Boolean editable) {
        this.ct = cont;
        this.al = al;
        Editable = editable;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ct);
        View v = li.inflate(R.layout.recycler_view_recipes_list_item, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAllRecipesAdaptor.MyHolder holder, final int position) {
        holder.bind(al.get(position),position);
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView Image;
        TextView Title;
        View cld;

        public MyHolder(View itemView) {
            super(itemView);
            cld = itemView.findViewById(R.id.RecipeViewCard);
            Image = itemView.findViewById(R.id.image);
            Title = itemView.findViewById(R.id.title);
        }

        public void bind(final Recipe p1,int position) {
            Title.setText("Title : " + p1.getTitle());

            if (p1.getImage() != null)
                Picasso.get().load(p1.getImage()).into(Image);
            else
                Image.setImageResource(R.drawable.profile);

            cld.setOnClickListener(view -> ct.startActivity(new Intent(ct, RecipeDetailActivity.class).putExtra("Recipe", p1)));
        }
    }
}