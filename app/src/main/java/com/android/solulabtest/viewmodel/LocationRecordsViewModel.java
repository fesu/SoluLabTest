package com.android.solulabtest.viewmodel;

import com.android.solulabtest.LocationInteractor;
import com.android.solulabtest.model.Locations;

import java.util.List;

import rx.Observable;
import rx.Scheduler;

public class LocationRecordsViewModel {

    private LocationInteractor interactor;
    private Scheduler scheduler;

    public LocationRecordsViewModel(LocationInteractor interactor, Scheduler scheduler) {
        this.interactor = interactor;
        this.scheduler = scheduler;
    }

    public Observable<List<Locations>> getLocationRecords() {
        return interactor.getLocationRecords().observeOn(scheduler);
    }
}