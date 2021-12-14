package com.example.wheelchair.Activity;

import android.content.Context;

import com.example.wheelchair.DTO.EntranceInfo;
import com.example.wheelchair.R;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wheelchair.DTO.NowBus;

import java.util.ArrayList;

public class EntranceAdapter extends RecyclerView.Adapter<EntranceAdapter.MyViewHolder> {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<EntranceInfo> info;

    public EntranceAdapter(Context context, ArrayList<EntranceInfo> data){
        mContext = context;
        info = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void addItem(EntranceInfo i){
        info.add(i);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.entrance_list_view_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        EntranceInfo i = info.get(position);
        holder.setItem(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return info.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
             imageView = itemView.findViewById(R.id.list_item_image);
        }
        public void setItem(EntranceInfo info){
            int type = info.getType();
            if(type==0){
                imageView.setImageResource(R.drawable.elevator);
            }
            else if (type==1) imageView.setImageResource(R.drawable.ramp);
            else if (type==2) imageView.setImageResource(R.drawable.stairs);
        }
    }
}
