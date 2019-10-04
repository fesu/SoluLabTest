package com.android.solulabtest;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.android.solulabtest.ui.main.SectionsPagerAdapter;
import com.android.solulabtest.utils.Config;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private LocalBroadcastManager localBroadcastManager;
    private boolean isTrackingStarted = false;
    private boolean isFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab = findViewById(R.id.fab);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        fab.setOnClickListener(view -> {
            if (!isTrackingStarted){
                Snackbar.make(view, "Starting Live tracking...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                startTracking();
            }
            else {
                Snackbar.make(view, "Stopping Live tracking...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                stopTracking();
            }

        });

        if (isFromNotification){
            selectRecordsTab();
        }
    }

    private void selectRecordsTab() {
        TabLayout.Tab tab = tabLayout.getTabAt(2);
        if (tab != null) {
            tab.select();
        }
    }

    private void stopTracking() {
        isTrackingStarted = false;
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_navigation));

        // send local broadcast to notify live tracking is started.
        Intent localIntent = new Intent(Config.LIVE_TRACKING_BROADCAST);
        localIntent.putExtra(Config.IS_TRACKING_STARTED, false);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    private void startTracking() {
        selectMapTab();
        isTrackingStarted = true;
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop));

        // send local broadcast to notify live tracking has stopped.
        Intent localIntent = new Intent(Config.LIVE_TRACKING_BROADCAST);
        localIntent.putExtra(Config.IS_TRACKING_STARTED, true);
        localBroadcastManager.sendBroadcast(localIntent);
    }

    private void selectMapTab() {
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
        }
    }

}