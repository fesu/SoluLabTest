package com.android.solulabtest;

import android.content.Context;

import com.android.solulabtest.data.DBManager;
import com.android.solulabtest.data.DatabaseHelper;
import com.android.solulabtest.model.Locations;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

public class LocationInteractorImpl implements LocationInteractor {
    private DatabaseHelper databaseHelper;

    public LocationInteractorImpl(Context context) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public Observable<List<Locations>> getLocationRecords() {
        return databaseHelper.makeObservable(DBManager::getLocationRecords)
                .subscribeOn(Schedulers.io());
    }
}