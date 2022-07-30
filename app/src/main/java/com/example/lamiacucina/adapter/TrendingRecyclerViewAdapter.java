package com.example.lamiacucina.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.lamiacucina.R;
import com.example.lamiacucina.RecipeDetailActivity;
import com.example.lamiacucina.model.Recipe;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrendingRecyclerViewAdapter extends SliderViewAdapter<TrendingRecyclerViewAdapter.ViewHolder> {
    private final Context context;
    ItemClickListener mClickListener;
    List<Recipe> RecipesList;

    public TrendingRecyclerViewAdapter(Context c, ItemClickListener mClickListener, List<Recipe> ads) {
        this.mClickListener = mClickListener;
        RecipesList = ads;
        context = c;
    }

    public void renewItems(List<Recipe> sliderItems) {
        this.RecipesList = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.RecipesList.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(Recipe sliderItem) {
        this.RecipesList.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams")
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_slider_rview_item,parent,false);
        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Recipe p1 = RecipesList.get(position);

        holder.Title.setText("Title : " + p1.getTitle());

        if (p1.getImage() != null)
            Picasso.get().load(p1.getImage()).into(holder.Image);
        else
            holder.Image.setImageResource(R.drawable.profile);

        holder.cld.setOnClickListener(view -> context.startActivity(new Intent(context, RecipeDetailActivity.class).putExtra("Recipe", p1)));
    }

    @Override
    public int getCount() {
        if (RecipesList == null)
            return 0;
        return RecipesList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends SliderViewAdapter.ViewHolder implements View.OnClickListener {
        ImageView Image;
        TextView Title;
        View cld;

        ViewHolder(View itemView) {
            super(itemView);
            cld = itemView.findViewById(R.id.RecipeViewCard);
            Image = itemView.findViewById(R.id.image);
            Title = itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getItemPosition(view));
        }
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

