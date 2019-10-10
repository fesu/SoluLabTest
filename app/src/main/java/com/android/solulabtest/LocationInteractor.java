package com.android.solulabtest;

import com.android.solulabtest.model.Locations;

import java.util.List;

import rx.Observable;

public interface LocationInteractor {
    Observable<List<Locations>> getLocationRecords();
}