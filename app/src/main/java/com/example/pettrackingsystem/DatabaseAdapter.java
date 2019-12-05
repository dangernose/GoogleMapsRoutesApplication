package com.example.pettrackingsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseAdapter extends SQLiteOpenHelper {

    private static final String LOG = "DatabaseAdapter";

    private static final String DATABASE_NAME = "pets.db";
    private static final int DATABASE_VERSION = 1;


    private static final String TABLE_PET_LOCATION_DATA = "table_pet_location";
    private static final String COL_0 = "id";
    private static final String COL_1 = "lat";
    private static final String COL_2 = "lng";
    private static final String COL_3 = "date";
    private static final String COL_4 = "time";
    private static final String COL_5 = "petid";

    //Query to create new table to store Pet Location Data
    private  static final String CREATE_TABLE_PET_LOCATION = "CREATE TABLE "+
            TABLE_PET_LOCATION_DATA + " (" +
            COL_0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_1 + " REAL NOT NULL, " +
            COL_2 + " REAL NOT NULL, " +
            COL_3 + " TEXT NOT NULL, " +
            COL_4 + " TEXT NOT NULL, " +
            COL_5 + " TEXT NOT NULL " +
            " );";

    public DatabaseAdapter(@Nullable Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE ADAPTER", CREATE_TABLE_PET_LOCATION);
        db.execSQL(CREATE_TABLE_PET_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long InserPetLocation(double lat, double lng,String date, String time,String petid){
        SQLiteDatabase db  = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_1,lat);
        values.put(COL_2,lng);
        values.put(COL_3,date);
        values.put(COL_4,time);
        values.put(COL_5,petid);

        long id = db.insert(TABLE_PET_LOCATION_DATA,null,values);
        db.close();
        return id;
    }

    public String getMaxTime(String Date){
        SQLiteDatabase db = this.getReadableDatabase();
        String getMaxDateQuery = "SELECT MAX(" + COL_4 + ") AS max from " + TABLE_PET_LOCATION_DATA
                + " WHERE " + COL_3 + "= '" + Date + "';";
        Cursor cursor = db.rawQuery(getMaxDateQuery,null);
        if (cursor != null)
            cursor.moveToFirst();
        String max = cursor.getString(cursor.getColumnIndex("max"));

        return max;
    }

    public PetLocation getPetLocationData(Integer id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PET_LOCATION_DATA + " WHERE "
                + COL_1 + " = " + id.toString();

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        PetLocation petLocation = new PetLocation(
                c.getInt(c.getColumnIndex(COL_0)),
                c.getDouble(c.getColumnIndex(COL_1)),
                c.getDouble(c.getColumnIndex(COL_2)),
                c.getString(c.getColumnIndex(COL_3)),
                c.getString(c.getColumnIndex(COL_4))
        );
        return petLocation;
    }

    public List<PetLocation> getAllPetLocations(String date, String petid){
        List<PetLocation> petLocations = new ArrayList<PetLocation>();
        String selectQuery = "SELECT * FROM " +
                TABLE_PET_LOCATION_DATA
                + " WHERE " +
                COL_3 + " = '" + date +"' and " +  COL_5 + " = '" + petid + "';";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);
        if(c.moveToFirst()){
            do{
                PetLocation category = new PetLocation(
                        c.getInt(c.getColumnIndex(COL_0)),
                        c.getDouble(c.getColumnIndex(COL_1)),
                        c.getDouble(c.getColumnIndex(COL_2)),
                        c.getString(c.getColumnIndex(COL_3)),
                        c.getString(c.getColumnIndex(COL_4))
                );
                petLocations.add(category);
            }while (c.moveToNext());
        }
        return petLocations;
    }
}
