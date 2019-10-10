package com.android.solulabtest.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.solulabtest.R;
import com.android.solulabtest.utils.Config;
import com.android.solulabtest.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class DistanceFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = DistanceFragment.class.getSimpleName();
    private TextView tv_total_distance;

    private Context context;
    private boolean isTrackingStarted = false;

    public static DistanceFragment newInstance(int index) {
        DistanceFragment fragment = new DistanceFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    private BroadcastReceiver trackingBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            try {
                isTrackingStarted = intent.getBooleanExtra(Config.IS_TRACKING_STARTED, false);
                showDistanceData();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private BroadcastReceiver milestoneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            try {
                Log.d(TAG, Config.MILESTONE_THRESHOLD_IN_METER + " meter Milestone reached");
                showDistanceData();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_distance, container, false);
        LocalBroadcastManager.getInstance(context).registerReceiver(trackingBroadcastReceiver,
                new IntentFilter(Config.LIVE_TRACKING_BROADCAST));
        LocalBroadcastManager.getInstance(context).registerReceiver(milestoneBroadcastReceiver,
                new IntentFilter(Config.MILESTONE_BROADCAST));
        context = getActivity();

        tv_total_distance = root.findViewById(R.id.tv_total_distance);

        showDistanceData();

        return root;
    }

    private void showDistanceData() {
        try {
            String msg = "Total Travelled distance\n" + Utils.getTotalTravelledMeters(context) + " Meters";
            tv_total_distance.setText(msg);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(trackingBroadcastReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(milestoneBroadcastReceiver);
    }
}