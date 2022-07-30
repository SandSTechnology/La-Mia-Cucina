package com.example.lamiacucina.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lamiacucina.R;
import com.example.lamiacucina.model.Family;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class FamilyListAdaptor extends RecyclerView.Adapter<FamilyListAdaptor.MyHolder> {
    Context ct;
    ArrayList<Family> al;

    public FamilyListAdaptor(Context cont, ArrayList<Family> al) {
        this.ct = cont;
        this.al = al;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(ct);
        View v = li.inflate(R.layout.family_recyclerview_item, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyListAdaptor.MyHolder holder, final int position) {
        final Family p1 = al.get(position);

        if (p1.getName()!=null && !p1.getName().equals(""))
        {
            if (p1.getID().equals(FirebaseAuth.getInstance().getUid()))
                holder.PersonName.setText(ct.getResources().getString(R.string.my_name_in_family_list,p1.getName().trim(),p1.getRole().trim()));
            else
                holder.PersonName.setText(ct.getResources().getString(R.string.person_name_in_family_list,p1.getName().trim() , p1.getRole().trim()));
        }
        else
            holder.PersonName.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return al.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView PersonName;
        CardView cld;

        public MyHolder(View itemView) {
            super(itemView);
            cld = itemView.findViewById(R.id.FamilyDetailCard);
            image = itemView.findViewById(R.id.image);
            PersonName = itemView.findViewById(R.id.PersonName);
        }
    }
}