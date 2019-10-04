package com.android.solulabtest.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.solulabtest.R;
import com.android.solulabtest.data.DBManager;
import com.android.solulabtest.model.Locations;
import com.android.solulabtest.ui.main.LocationRecordsAdapter;
import com.android.solulabtest.utils.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecordsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = RecordsFragment.class.getSimpleName();

    private Context context;
    private RecyclerView rcv_records;
    private ProgressBar progress_bar;
    private LocationRecordsAdapter locationRecordsAdapter;
    private List<Locations> locationList;
    private DBManager dbManager;

    private BroadcastReceiver milestoneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            Log.d(TAG, Config.MILESTONE_THRESHOLD_IN_METER + " meter Milestone reached");

            // Load new added records
            new LoadData().execute();
        }
    };

    public static RecordsFragment newInstance(int index) {
        RecordsFragment fragment = new RecordsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_records, container, false);
        context = getActivity();

        init(root);

        new LoadData().execute();

        return root;
    }

    private void init(View root) {
        LocalBroadcastManager.getInstance(context).registerReceiver(milestoneBroadcastReceiver,
                new IntentFilter(Config.MILESTONE_BROADCAST));

        rcv_records = root.findViewById(R.id.rcv_records);
        progress_bar = root.findViewById(R.id.progress_bar);
        locationList = new ArrayList<>();
        dbManager = new DBManager(context);
        dbManager.open();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rcv_records.setLayoutManager(linearLayoutManager);

    }

    private class LoadData extends AsyncTask<String, Void, List<Locations>> {

        @Override
        protected List<Locations> doInBackground(String... params) {

            return dbManager.getLocationRecords();
        }

        @Override
        protected void onPostExecute(List<Locations> result) {
            locationList = result;

            locationRecordsAdapter = new LocationRecordsAdapter(locationList);
            rcv_records.setAdapter(locationRecordsAdapter);

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(milestoneBroadcastReceiver);
    }
}