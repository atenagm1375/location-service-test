package com.atena.test.locationservicetest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by atena on 8/30/2017.
 */

public class LocationDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "location.db";
    private static final String TABLE_LOCATION = "location";
    private static final String FIELD_LATITUDE = "latitude";
    private static final String FIELD_LONGITUDE = "longitude";
    private static final String FIELD_PROVIDER = "provider";
    private static final int DATABASE_VERSION = 1;

    public LocationDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_LOCATION + " (_id integer PRIMARY KEY," + FIELD_LATITUDE + " REAL, " +
            FIELD_LONGITUDE + " REAL, " + FIELD_PROVIDER + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void saveRecord(double latitude, double longitude, String provider) {
        long id = findLocId(latitude, longitude, provider);
        if (id > 0)
            updateRecord(id, latitude, longitude, provider);
        else
            addRecord(latitude, longitude, provider);
    }

    protected long addRecord(double latitude, double longitude, String provider) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_LATITUDE, latitude);
        values.put(FIELD_LONGITUDE, longitude);
        values.put(FIELD_PROVIDER, provider);
        return db.insert(TABLE_LOCATION, null, values);
    }

    protected int updateRecord(long id, double lat, double lng, String prv) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put(FIELD_LATITUDE, lat);
        values.put(FIELD_LONGITUDE, lng);
        values.put(FIELD_PROVIDER, prv);
        return db.update(TABLE_LOCATION, values, "_id = ?", new String[]{String.valueOf(id)});
    }

    public long findLocId(double lat, double lng, String prv) {
        long returnVal = -1;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM " + TABLE_LOCATION + " WHERE " + FIELD_LATITUDE + " = ? AND "
            + FIELD_LONGITUDE + " = ? AND " + FIELD_PROVIDER + " = ? ", new String[]{String.valueOf(lat),
                String.valueOf(lng), prv});
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            returnVal = cursor.getInt(0);
        }
        return returnVal;
    }

    public int deleteRecord(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_LOCATION, "_id = ?", new String[]{String.valueOf(id)});
    }

    public void test() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_LOCATION;
        try {
            Cursor cursor = db.rawQuery(query, null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        double lat = cursor.getDouble(0);
                        double lng = cursor.getDouble(1);
                        String pvd = cursor.getString(2);
                        Log.i("database", String.valueOf(lat) + ", " + String.valueOf(lng) + ", " + pvd);
                    }
                    while(cursor.moveToNext());
                }
            }finally {
                try{cursor.close();}catch (Exception e){}
            }
        }finally {
            try{db.close();}catch (Exception e){}
        }
    }
}
