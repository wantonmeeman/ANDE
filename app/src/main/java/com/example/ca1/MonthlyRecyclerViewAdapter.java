package com.example.ca1;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class MonthlyRecyclerViewAdapter extends RecyclerView.Adapter<MonthlyRecyclerViewAdapter.MyViewHolder> {
    private SimpleDateFormat tfhrTimeFormat = new SimpleDateFormat("HHmm", Locale.ENGLISH);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Context mContext;
    private List<Alarm> mData;

    public MonthlyRecyclerViewAdapter(Context mContext, List<Alarm> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.monthly_task,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.taskTitle.setText(mData.get(position).getTitle());
        holder.taskTime.setText(tfhrTimeFormat.format(mData.get(position).getUnixTime()));
        holder.taskDate.setText(dateFormat.format(mData.get(position).getUnixTime()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView taskTitle;
        TextView taskTime;
        TextView taskDate;

        public MyViewHolder(View itemView){
            super(itemView);

            taskTitle = (TextView) itemView.findViewById(R.id.taskTitle);
            taskTime = (TextView) itemView.findViewById(R.id.taskTime);
            taskDate = (TextView) itemView.findViewById(R.id.taskDate);
        }
    }
}
