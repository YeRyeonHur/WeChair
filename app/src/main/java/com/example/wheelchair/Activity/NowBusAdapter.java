package com.example.wheelchair.Activity;

import android.content.Context;
import com.example.wheelchair.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.wheelchair.DTO.NowBus;

import java.util.ArrayList;

public class NowBusAdapter extends BaseAdapter {

    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<NowBus> buses;

    public NowBusAdapter(Context context, ArrayList<NowBus> data){
        mContext = context;
        buses = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }
    @Override
    public int getCount() {
        return buses.size();
    }

    @Override
    public Object getItem(int position) {
        return buses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String arriveTime = buses.get(position).getTime()/60+"분 " + buses.get(position).getTime()%60+"초";
        if(buses.get(position).getTime()<60) arriveTime = "곧 도착";
        View view = mLayoutInflater.inflate(R.layout.bus_listview_item, null);
        TextView busName = (TextView)view.findViewById(R.id.list_item_bus_num);
        TextView busTime = (TextView)view.findViewById(R.id.list_item_time);
        TextView busType = (TextView)view.findViewById(R.id.list_item_bus_type);
        busName.setText(buses.get(position).getBusNum());
        busTime.setText(arriveTime);
        busType.setText(buses.get(position).getBusType());

        return view;
    }
}
