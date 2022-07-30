package com.example.lamiacucina.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lamiacucina.R;
import com.example.lamiacucina.model.Recipe;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class SelectRecipesAdaptor extends RecyclerView.Adapter<SelectRecipesAdaptor.MyHolder> {
    Context ct;
    ArrayList<Recipe> al;
    Boolean Editable;
    List<Boolean> Selection = new ArrayList<>();

    public SelectRecipesAdaptor(Context cont, ArrayList<Recipe> al, Boolean editable) {
        this.ct = cont;
        this.al = al;
        Editable = editable;

        for (int i = 0 ; i < al.size() ; i++)
        {
            Selection.add(i,false);
        }

    }

    public ArrayList<Recipe> GetRecipesSelected()
    {
        ArrayList<Recipe> recipes = new ArrayList<>();
        for (int i = 0 ; i < Selection.size() ; i++)
        {
            if (Selection.get(i))
            {
                recipes.add(al.get(i));
            }
        }
        if (recipes.size() == 0)
            return null;
        else
            return recipes;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ct);
        View v = li.inflate(R.layout.select_recipes_list_layout, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectRecipesAdaptor.MyHolder holder, final int position) {
        holder.bind(al.get(position),position);
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView Image;
        TextView Title;
        CheckBox CheckBox;
        View cld;

        public MyHolder(View itemView) {
            super(itemView);
            cld = itemView.findViewById(R.id.RecipeViewCard);
            Image = itemView.findViewById(R.id.image);
            Title = itemView.findViewById(R.id.title);
            CheckBox = itemView.findViewById(R.id.RecipeSelected);
        }

        public void bind(final Recipe p1,int position) {
            Title.setText("Title : " + p1.getTitle());

            if (p1.getImage() != null)
                Picasso.get().load(p1.getImage()).into(Image);
            else
                Image.setImageResource(R.drawable.profile);

            CheckBox.setChecked(Selection.get(position));

            cld.setOnClickListener(view -> {
                Selection.set(position,!Selection.get(position));

                CheckBox.setChecked(Selection.get(position));
            });
        }
    }
}