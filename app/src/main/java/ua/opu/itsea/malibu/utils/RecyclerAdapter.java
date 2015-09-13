package ua.opu.itsea.malibu.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.opu.itsea.malibu.R;
import ua.opu.itsea.malibu.ScanActivity;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<ScanResult> mScans;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTime;
        public TextView mDate;
        public TextView mText;
        public ImageView mBrokerResult;

        public ViewHolder(View v) {
            super(v);
            mTime = (TextView) v.findViewById(R.id.time);
            mDate = (TextView) v.findViewById(R.id.date);
            mText = (TextView) v.findViewById(R.id.qrcode_text);
            mBrokerResult = (ImageView) v.findViewById(R.id.broker_result_image);
        }
    }

    public RecyclerAdapter(ArrayList<ScanResult> scans) {
        mScans = scans;
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTime.setText(String.valueOf(mScans.get(position).getTime()));
        holder.mDate.setText(String.valueOf(mScans.get(position).getDate()));
        holder.mText.setText(mScans.get(position).getContents());
        holder.mBrokerResult.setImageResource(mScans.get(position).getmImageResource());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mScans.size();
    }
}