package com.android.solulabtest.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.solulabtest.model.Locations;

import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private static final String TAG = DBManager.class.getSimpleName();
    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String lat, String lon, String dis, String time) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.LATITUDE, lat);
            contentValues.put(DatabaseHelper.LONGITUDE, lon);
            contentValues.put(DatabaseHelper.DISTANCE, dis);
            contentValues.put(DatabaseHelper.TIME, time);
            long rowInserted = database.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
            if(rowInserted != -1)
                Log.d(TAG, "Record inserted successfully...");
            else
                Log.d(TAG, "Record not inserted...");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.LATITUDE, DatabaseHelper.LONGITUDE,
                DatabaseHelper.DISTANCE, DatabaseHelper.TIME };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, DatabaseHelper._ID + " DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public List<Locations> getLocationRecords() {
        Cursor c = fetch();
        List<Locations> locationList = new ArrayList<>();
        Locations locations;

        if (c != null ) {
            if(c.moveToFirst()){
                do{
                    locations = new Locations();
                    locations.setId(c.getInt(c.getColumnIndex(DatabaseHelper._ID)));
                    locations.setLatitude(c.getString(c.getColumnIndex(DatabaseHelper.LATITUDE)));
                    locations.setLongitude(c.getString(c.getColumnIndex(DatabaseHelper.LONGITUDE)));
                    locations.setDistance(c.getString(c.getColumnIndex(DatabaseHelper.DISTANCE)));
                    locations.setTime(c.getString(c.getColumnIndex(DatabaseHelper.TIME)));

                    locationList.add(locations);
                }while(c.moveToNext());
            }
            c.close();
        }

        return locationList;
    }

    public int update(long _id, String lat, String lon, String dis, String time) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.LATITUDE, lat);
        contentValues.put(DatabaseHelper.LONGITUDE, lon);
        contentValues.put(DatabaseHelper.DISTANCE, dis);
        contentValues.put(DatabaseHelper.TIME, time);
        int i = database.update(DatabaseHelper.TABLE_NAME, contentValues, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }

}
