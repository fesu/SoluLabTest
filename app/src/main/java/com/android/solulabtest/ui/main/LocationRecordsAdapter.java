package com.android.solulabtest.ui.main;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.solulabtest.R;
import com.android.solulabtest.model.Locations;
import com.android.solulabtest.utils.Utils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationRecordsAdapter extends RecyclerView.Adapter<LocationRecordsAdapter.MyViewHolder> {
    private List<Locations> locationList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tv_lat, tv_lon, tv_time, tv_id;
        public MyViewHolder(View v) {
            super(v);
            tv_lat = v.findViewById(R.id.tv_lat);
            tv_lon = v.findViewById(R.id.tv_lon);
            tv_time = v.findViewById(R.id.tv_time);
            tv_id = v.findViewById(R.id.tv_id);
        }
    }

    public LocationRecordsAdapter(List<Locations> locationList) {
        this.locationList = locationList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LocationRecordsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_record_view, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Locations locations = locationList.get(position);

        String msg = "Record ID : " + locations.getId();
        holder.tv_id.setText(msg);
        holder.tv_lat.setText(locations.getLatitude());
        holder.tv_lon.setText(locations.getLongitude());

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(locations.getTime()));
        String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

        holder.tv_time.setText(date);

    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }
}