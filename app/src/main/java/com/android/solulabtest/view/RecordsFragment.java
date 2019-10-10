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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.solulabtest.LocationInteractorImpl;
import com.android.solulabtest.R;
import com.android.solulabtest.model.Locations;
import com.android.solulabtest.utils.Config;
import com.android.solulabtest.view.main.LocationRecordsAdapter;
import com.android.solulabtest.viewmodel.LocationRecordsViewModel;

import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * A placeholder fragment containing a simple view.
 */
public class RecordsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = RecordsFragment.class.getSimpleName();

    private Context context;
    private RecyclerView rcv_records;

    private Subscription subscription = new CompositeSubscription();
    private LocationRecordsViewModel locationRecordsViewModel;

    private BroadcastReceiver milestoneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            Log.d(TAG, Config.MILESTONE_THRESHOLD_IN_METER + " meter Milestone reached");

            // Load new added records
            getLocationData();
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

        getLocationData();

        return root;
    }

    private void init(View root) {
        locationRecordsViewModel = new LocationRecordsViewModel(new LocationInteractorImpl(context),
                AndroidSchedulers.mainThread());

        LocalBroadcastManager.getInstance(context).registerReceiver(milestoneBroadcastReceiver,
                new IntentFilter(Config.MILESTONE_BROADCAST));

        rcv_records = root.findViewById(R.id.rcv_records);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rcv_records.setLayoutManager(linearLayoutManager);

    }

    private void getLocationData() {
        subscription = locationRecordsViewModel.getLocationRecords()
                .subscribe(new Observer<List<Locations>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Locations> locationList) {
                        updateUi(locationList);
                    }
                });
    }

    private void updateUi(List<Locations> locationList) {
        if (locationList.size() > 0){
            LocationRecordsAdapter locationRecordsAdapter = new LocationRecordsAdapter(locationList);
            rcv_records.setAdapter(locationRecordsAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(milestoneBroadcastReceiver);
    }
}