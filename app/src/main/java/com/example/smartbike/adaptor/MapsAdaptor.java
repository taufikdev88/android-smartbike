package com.example.smartbike.adaptor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbike.R;
import com.example.smartbike.model.MapsModel;

import java.util.ArrayList;

public class MapsAdaptor extends RecyclerView.Adapter<MapsAdaptor.ViewHolder> {
    public ArrayList<MapsModel> dataSet;

    public MapsAdaptor(ArrayList<MapsModel> mapsModels){
        dataSet = mapsModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View mapsView = layoutInflater.inflate(R.layout.fragment_maps_history, parent, false);
        return new ViewHolder(context, mapsView);
    }

    @Override
    public void onBindViewHolder(@NonNull MapsAdaptor.ViewHolder holder, int position) {
        MapsModel mapsModel =  dataSet.get(position);

        TextView txtNo = holder.txtNo;
        TextView txtDate = holder.txtDate;
        TextView txtTime = holder.txtTime;
        TextView txtStep = holder.txtStep;

        txtNo.setText(String.valueOf(position+1));
        txtDate.setText(mapsModel.getDate());
        txtTime.setText(mapsModel.getTime());
        txtStep.setText(String.valueOf(mapsModel.getStep()));
        Log.d("BIND DEBUG", holder.txtTime.getText().toString());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNo;
        TextView txtDate;
        TextView txtTime;
        TextView txtStep;
        Context mContext;

        public ViewHolder(Context context,@NonNull View itemView) {
            super(itemView);

            txtNo = itemView.findViewById(R.id.fragmentMapsNo);
            txtDate = itemView.findViewById(R.id.fragmentMapsDate);
            txtTime = itemView.findViewById(R.id.fragmentMapsTime);
            txtStep = itemView.findViewById(R.id.fragmentMapsStep);
            mContext = context;
        }
    }
}
