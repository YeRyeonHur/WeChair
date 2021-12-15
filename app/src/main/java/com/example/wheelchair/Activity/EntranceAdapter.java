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

public class EntranceAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<EntranceInfo> info;
    boolean[] info_flag = new boolean[5];

    public EntranceAdapter(Context context, ArrayList<EntranceInfo> entranceInfos) {
        mContext = context;
        info = entranceInfos;
        mLayoutInflater = LayoutInflater.from(mContext);
    }


    public void addItem(EntranceInfo i) {
        info.add(i);
    }

    @Override
    public int getCount() {
        return info.size();
    }

    @Override
    public Object getItem(int position) {
        return info.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.entrance_list_view_item, null);
        ImageView list_item_image = (ImageView) view.findViewById(R.id.list_item_image);
        TextView list_item_text = (TextView) view.findViewById(R.id.list_item_text);
        int tmp = info.get(position).getType();
        if (tmp == 0) {
            list_item_image.setImageResource(R.drawable.ramp);
            list_item_text.setText("경사로");
        } else if (tmp == 1) {
            list_item_image.setImageResource(R.drawable.toilet1);
            list_item_text.setText("대변기");
        } else if (tmp == 2) {
            list_item_image.setImageResource(R.drawable.elevator);
            list_item_text.setText("엘리베이터");
        } else if (tmp == 3) {
            list_item_image.setImageResource(R.drawable.parking);
            list_item_text.setText("장애인 전용 주차장");
        } else if (tmp == 4) {
            list_item_image.setImageResource(R.drawable.wheelchair);
            list_item_text.setText("접근로 및 높이차이 제거");
        }
        return view;
    }

}
