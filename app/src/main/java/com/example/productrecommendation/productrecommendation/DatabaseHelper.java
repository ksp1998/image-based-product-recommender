package com.example.productrecommendation.productrecommendation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase database;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "IBPR_Database";
    private static final String HISTORY = "HISTORY";
    private static final String FAVORITES = "FAVORITES";
    private static final String ID = "ID";
    private static final String IMAGE = "IMAGE";
    private static final String IS_FAVORITE = "IS_FAVORITE";
    private Context context;

    private static final String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + HISTORY + "("+
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            IMAGE + " BLOB, " +
            IS_FAVORITE + " INTEGER DEFAULT 0);";

    private static final String CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS " + FAVORITES + "("+
            ID +" INTEGER, " +
            IMAGE + " BLOB);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + FAVORITES);
        onCreate(db);
    }

    public int[] addToHistory( byte[] image) throws SQLiteException {
        int[] res = new int[2];
        database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(IMAGE, image);
        long position = database.insert(HISTORY, null, cv);
        res[0] = (int) position;
        String query = "SELECT " + ID + " FROM " + HISTORY;
        Cursor cursor = database.rawQuery(query,null);
        cursor.moveToLast();
        res[1] = cursor.getInt(cursor.getColumnIndex(ID));
        cursor.close();
        database.close();
        return res;
    }

    public ArrayList<Bitmap> getHistory(){
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        Bitmap bitmapImage = null;
        byte[] byteImage;
        database = this.getWritableDatabase();
        String query = "SELECT " + IMAGE + " FROM " + HISTORY + "";
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (cursor.isAfterLast() != true) {
            try {
                byteImage = cursor.getBlob(cursor.getColumnIndex(IMAGE));
                bitmapImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                images.add(bitmapImage);
                cursor.moveToNext();
            }
            catch (OutOfMemoryError e){
                Toast.makeText(context, "Out Of Memory Error", Toast.LENGTH_SHORT).show();
            }
        }
        int r = cursor.getCount();
        Log.i("Rows/Column/Position", "" + r + " / " + cursor.getColumnCount() + " / " + cursor.getPosition());
        cursor.close();
        database.close();
        return images;
    }

    public Bitmap getResourceImage(int id, String table){
        Bitmap resourceImage = null;
        byte[] byteImage;
        database = this.getWritableDatabase();
        String query = "SELECT " + IMAGE + " FROM " + table + " WHERE "+ ID +" = " + id;
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            byteImage = cursor.getBlob(cursor.getColumnIndex(IMAGE));
            resourceImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            Log.i("Image" , "Found");
        }
        cursor.close();
        database.close();
        return resourceImage;
    }

    public void deleteHistory(){

        database = this.getWritableDatabase();
        String query = "DELETE FROM " + HISTORY;
        database.execSQL(query);
        database.close();
    }


    public void deleteHistoryImage(int id) {
        database = this.getWritableDatabase();
        String query = "DELETE FROM " + HISTORY + " WHERE " + ID + " = " + id;
        database.execSQL(query);
        database.close();
    }


    public ArrayList<Integer> getIds(String table) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        database = this.getWritableDatabase();
        String query = "SELECT " + ID + " FROM " + table;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            ids.add(cursor.getInt(cursor.getColumnIndex(ID)));
            cursor.moveToNext();
        }
        cursor.close();
        database.close();
        return ids;
    }


    public ArrayList<Bitmap> getFavorites(){
        database = this.getWritableDatabase();
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        Bitmap bitmapImage = null;
        byte[] byteImage;
        String query = "SELECT " + IMAGE + " FROM " + FAVORITES;
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (cursor.isAfterLast() != true) {
            try {
                byteImage = cursor.getBlob(cursor.getColumnIndex(IMAGE));
                bitmapImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                images.add(bitmapImage);
                cursor.moveToNext();
            }
            catch (OutOfMemoryError e){
                Toast.makeText(context, "Out Of Memory Error", Toast.LENGTH_SHORT).show();
            }
        }
        int r = cursor.getCount();
        Log.i("Rows/Column/Position", "" + r + " / " + cursor.getColumnCount() + " / " + cursor.getPosition());
        cursor.close();
        database.close();
        return images;
    }


    public boolean addFavorite(int id, byte[] image) throws SQLiteException {
        database = this.getWritableDatabase();

        //database.execSQL("UPDATE " + HISTORY + " SET " + IS_FAVORITE + " = 1 WHERE " + ID + " = " + hid);

        ContentValues cv = new ContentValues();
        cv.put(ID, id);
        cv.put(IMAGE, image);
        database.insert(FAVORITES, null, cv);
        database.close();
        return true;
    }


    public void deleteFavorites(){
        database = this.getWritableDatabase();
        String query = "DELETE FROM " + FAVORITES;
        database.execSQL(query);
        database.close();
    }


    public boolean isFavorite(int id) {
        database = this.getWritableDatabase();
        String query = "SELECT " + ID + " FROM " + FAVORITES + " WHERE "+ ID +" = " + id;
        Cursor cursor = database.rawQuery(query, null);
        if(cursor.moveToFirst()) {
            cursor.close();
            database.close();
            return true;
        }
        cursor.close();
        database.close();
        return false;
    }


    public void removeFavorite(int id) throws SQLiteException {
        database = this.getWritableDatabase();

        //database.execSQL("UPDATE " + HISTORY + " SET " + IS_FAVORITE + " = 0 WHERE " + ID + " = " + id);

        String query = "DELETE FROM " + FAVORITES + " WHERE " + ID + " = " + id;
        database.execSQL(query);
        database.close();
    }
}
