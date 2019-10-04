package com.android.solulabtest.ui;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.solulabtest.MainActivity;
import com.android.solulabtest.R;
import com.android.solulabtest.data.DBManager;
import com.android.solulabtest.utils.Config;
import com.android.solulabtest.utils.LocationAddress;
import com.android.solulabtest.utils.Utils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import static com.android.solulabtest.utils.Config.FASTEST_INTERVAL_IN_MILLI;
import static com.android.solulabtest.utils.Config.IS_TRACKING_STARTED;
import static com.android.solulabtest.utils.Config.LIVE_TRACKING_BROADCAST;
import static com.android.solulabtest.utils.Config.NOTIFICATION_CHANNEL_ID;
import static com.android.solulabtest.utils.Config.NOTIFICATION_ID;
import static com.android.solulabtest.utils.Config.MILESTONE_THRESHOLD_IN_METER;
import static com.android.solulabtest.utils.Config.UPDATE_INTERVAL_IN_MILLI;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private int ACCESS_FINE_LOCATION_CODE = 1001;
    private int REQUEST_CHECK_SETTINGS = 1002;
    private boolean mLocationPermissionGranted = false;

    private Context context;

    private boolean isTrackingStarted = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    private TextView txtLocation;

    private DBManager dbManager;

    private LocalBroadcastManager localBroadcastManager;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            isTrackingStarted = intent.getBooleanExtra(IS_TRACKING_STARTED, false);
            Log.d("Received data", String.valueOf(isTrackingStarted));

            if (isTrackingStarted)
                startLocationUpdates();
            else
                stopLocationUpdates();
        }
    };

    public static MapFragment newInstance(int index) {
        MapFragment fragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        context = getActivity();

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        txtLocation = root.findViewById(R.id.txtLocation);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,
                new IntentFilter(LIVE_TRACKING_BROADCAST));

        createNotificationChannel();

        dbManager = new DBManager(context);
        dbManager.open();

        return root;
    }

    // Trigger new location updates at interval
    private void startLocationUpdates() {
        txtLocation.setVisibility(View.VISIBLE);
        createLocationRequest();

        onMyLocationButtonClick();

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                locationCallback, Looper.myLooper());
    }

    private Location lastKnownLocation;
    public static double distanceTravelledInMeter = 0;
    private double distance = 0;
    public static double startPoint = 0;
    private void onLocationChanged(Location location) {
        if (lastKnownLocation != null){
            Log.d("Old Lat : ", String.valueOf(lastKnownLocation.getLatitude()));
            Log.d("Old Lon : ", String.valueOf(lastKnownLocation.getLongitude()));

            Log.d("New Lat : ", String.valueOf(location.getLatitude()));
            Log.d("New Lon : ", String.valueOf(location.getLongitude()));

            // Count meter & add it to total
            distance = getDistanceInMeter(lastKnownLocation, location);
            distanceTravelledInMeter += distance;
        }

        if (distanceTravelledInMeter > 0){
            double fiftyCheck = distanceTravelledInMeter - startPoint;
            if (fiftyCheck >= MILESTONE_THRESHOLD_IN_METER){
                sendMileStoneBroadcast();
                showNotification();
                dbManager.insert(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), String.valueOf(fiftyCheck),
                        String.valueOf(System.currentTimeMillis()));
                startPoint = distanceTravelledInMeter;
            }
        }

        String prefValue = Utils.getTotalTravelledMeters(context);
        double prefDis = 0;
        if(!prefValue.isEmpty())
            prefDis = Double.valueOf(prefValue);
        double totalMeter = prefDis + distance;
        Utils.saveTotalTravelledMeters(context, totalMeter);

        // After calculating distance, set lastKnowLocation as current location
        lastKnownLocation = location;

        //txtLocation.setVisibility(View.VISIBLE);
        // New location has now been determined
        String msg = location.getLatitude() + " : " +
                        location.getLongitude() + "\n" + "Meter travelled : " + distanceTravelledInMeter;
        txtLocation.setText(msg);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    private void sendMileStoneBroadcast() {
        // send local broadcast to notify live tracking is started.
        Intent localIntent = new Intent(Config.MILESTONE_BROADCAST);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification() {
        Log.d(TAG, "Notification shown");
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("isFromNotification", true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Milestone reached")
                .setContentText("You have travelled 50 more meters.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        builder.setContentIntent(resultPendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (notificationManager != null) {
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void stopLocationUpdates() {
        Log.d("Total Meters : ", String.valueOf(distanceTravelledInMeter));
        txtLocation.setVisibility(View.GONE);
        distanceTravelledInMeter = 0;
        lastKnownLocation = null;
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(context);

        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // GPS location can be null if GPS is switched off
                    if (location != null) {
                        onLocationChanged(location);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("MapDemoActivity", "Error trying to get last GPS location");
                    e.printStackTrace();
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        // Checks for permission using the Support library before enabling the My Location layer
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionGranted = true;

            // We have access. Life is good.
            createLocationRequest();
        } /*else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), READ_CONTACTS)) {

            // We've been denied once before. Explain why we need the permission, then ask again.
            getActivity().showDialog(DIALOG_PERMISSION_REASON);
        }*/ else {

            // We've never asked. Just do it.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLI);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_IN_MILLI);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((Activity) context, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            updateLocationUI();
        });

        task.addOnFailureListener((Activity) context, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult((Activity) context,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.setOnMyLocationClickListener(this);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                createLocationRequest();

            } else {
                // Permission was denied. Display an error message.
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        LocationAddress.getAddressFromLocation(location.getLatitude(), location.getLongitude(),
                context, new GeoCoderHandler());
    }

    private class GeoCoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            showAddress(locationAddress);
        }
    }

    private void showAddress(String locationAddress) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                context);
        alertDialog.setTitle("Address");
        alertDialog.setMessage(locationAddress);
        alertDialog.setPositiveButton("OK",
                (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    public double getDistanceInMeter(Location source, Location destination) {
        double distance = 0d;
        if (source != null && destination != null) {
            double lat1 = source.getLatitude();
            double lon1 = source.getLongitude();
            double lat2 = destination.getLatitude();
            double lon2 = destination.getLongitude();

            final int R = 6371;
            // Radius of the earth in km
            double dLat = deg2rad(lat2 - lat1);
            // deg2rad below
            double dLon = deg2rad(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            // Distance in m
            distance = (R * c) * 1000;
        }
        return distance;
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    /**
     *
     * @param deg
     * @return
     */
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    /**
     *
     * @param rad
     * @return
     */
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }
}