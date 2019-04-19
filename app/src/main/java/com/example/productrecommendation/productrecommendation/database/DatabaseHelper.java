package com.example.productrecommendation.productrecommendation.database;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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

    // Creating SQLiteDatabase object
    private SQLiteDatabase database;

    // Creating variables for database name, table and columns
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "IBPR_Database";
    private static final String HISTORY = "HISTORY";
    private static final String FAVORITES = "FAVORITES";
    private static final String ID = "ID";
    private static final String IMAGE = "IMAGE";
    private static final String IS_FAVORITE = "IS_FAVORITE";

    // Application context object
    private Context context;

    // Query for creating History Table
    private static final String CREATE_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + HISTORY + "("+
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                IMAGE + " BLOB, " +
                IS_FAVORITE + " INTEGER DEFAULT 0" +
            ");";

    // Query for creating Favorites Table
    private static final String CREATE_FAVORITE_TABLE = "CREATE TABLE IF NOT EXISTS " + FAVORITES + "("+
                ID +" INTEGER, " +
                IMAGE + " BLOB" +
            ");";


    // Constructor for creating database and initializing Context object

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    // Creating tables

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_FAVORITE_TABLE);
    }


    // Dropping tables then again creating when upgrading database

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + FAVORITES);
        onCreate(db);
    }


    // Storing image to history when captured or uploaded by user

    public int addToHistory( byte[] image) {
        int id = -1;
        database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(IMAGE, image);
        try{
            int position = (int) database.insert(HISTORY, null, cv);
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to insert image!!!", Toast.LENGTH_SHORT).show();
        }
        String query = "SELECT " + ID + " FROM " + HISTORY;
        Cursor cursor = database.rawQuery(query,null);
        try{
            cursor.moveToLast();
            id = cursor.getInt(cursor.getColumnIndex(ID));
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Something gone wrong!!!", Toast.LENGTH_SHORT).show();
        }
        finally {
            cursor.close();
            database.close();
        }

        //return stored image id
        return id;
    }


    // Method to return list of History

    public ArrayList<Bitmap> getHistory(){
        final ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        Bitmap bitmapImage = null;
        byte[] byteImage;
        database = this.getWritableDatabase();
        String query = "SELECT " + IMAGE + " FROM " + HISTORY + "";
        Cursor cursor = database.rawQuery(query, null);
        try {
            cursor.moveToFirst();
            while (cursor.isAfterLast() != true) {
                byteImage = cursor.getBlob(cursor.getColumnIndex(IMAGE));
                bitmapImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                images.add(bitmapImage);
                cursor.moveToNext();
                //int r = cursor.getCount();
                //Log.i("Rows/Column/Position", "" + r + " / " + cursor.getColumnCount() + " / " + cursor.getPosition());
            }
        }
        catch (OutOfMemoryError e ) {
            Toast.makeText(context, "Oops, Memory Error Occurred!!!", Toast.LENGTH_SHORT).show();
            return null;
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to load History!!!", Toast.LENGTH_SHORT).show();
        }
        finally {
            cursor.close();
            database.close();
        }
        return images;
    }


    // Method to return list of Favorites

    public ArrayList<Bitmap> getFavorites(){
        database = this.getWritableDatabase();
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        Bitmap bitmapImage = null;
        byte[] byteImage;
        String query = "SELECT " + IMAGE + " FROM " + FAVORITES;
        Cursor cursor = database.rawQuery(query, null);
        try{
            cursor.moveToFirst();
            while (cursor.isAfterLast() != true) {

                byteImage = cursor.getBlob(cursor.getColumnIndex(IMAGE));
                bitmapImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                images.add(bitmapImage);
                cursor.moveToNext();
            }
            //int r = cursor.getCount();
            //Log.i("Rows/Column/Position", "" + r + " / " + cursor.getColumnCount() + " / " + cursor.getPosition());
        }
        catch (OutOfMemoryError e) {
            Toast.makeText(context, "Oops, Memory Error Occurred!!!", Toast.LENGTH_SHORT).show();
            return null;
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to load favorites!!!", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        database.close();
        return images;
    }


    // Method to return single image

    public Bitmap getResourceImage(int id, String table){
        Bitmap resourceImage = null;
        byte[] byteImage;
        database = this.getWritableDatabase();
        String query = "SELECT " + IMAGE + " FROM " + table + " WHERE "+ ID +" = " + id;
        Cursor cursor = database.rawQuery(query, null);
        try{
            if(cursor.moveToFirst()) {
                byteImage = cursor.getBlob(cursor.getColumnIndex(IMAGE));
                resourceImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
                //Log.i("Image" , "Found");
            }
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to load image !!!", Toast.LENGTH_SHORT).show();
        }
        finally {
            cursor.close();
            database.close();
        }
        return resourceImage;
    }


    // Method for delete entire history

    public void deleteHistory(){
        try{
            database = this.getWritableDatabase();
            String query = "DELETE FROM " + HISTORY;
            database.execSQL(query);
            database.close();
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to delete History!!!", Toast.LENGTH_SHORT).show();
        }
    }


    // Method for remove entire favorites

    public void deleteFavorites(){
        try{
            database = this.getWritableDatabase();
            String query = "DELETE FROM " + FAVORITES;
            database.execSQL(query);
            database.close();
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to remove favorites!!!", Toast.LENGTH_SHORT).show();
        }
    }


    // Method for delete single history image

    public void deleteHistoryImage(int id) {
        try{
            database = this.getWritableDatabase();
            String query = "DELETE FROM " + HISTORY + " WHERE " + ID + " = " + id;
            database.execSQL(query);
            database.close();
        }
        catch (Exception e) {
            Toast.makeText(context, "Ooops, Failed to delete image!!!", Toast.LENGTH_SHORT).show();
        }
    }


    // Return array of history images id to get image at particular position

    public ArrayList<Integer> getIds(String table) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        database = this.getWritableDatabase();
        String query = "SELECT " + ID + " FROM " + table;
        Cursor cursor = database.rawQuery(query, null);
        try{
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                ids.add(cursor.getInt(cursor.getColumnIndex(ID)));
                cursor.moveToNext();
            }
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Something gone wrong!!!", Toast.LENGTH_SHORT).show();
        }
        finally {
            cursor.close();
            database.close();
        }
        return ids;
    }


    // Method to ensures whether image is favorite or not

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


    // Method for add single history image to favorite

    public boolean addFavorite(int id, byte[] image) {
        database = this.getWritableDatabase();

        database.execSQL("UPDATE " + HISTORY + " SET " + IS_FAVORITE + " = 1 WHERE " + ID + " = " + id);

        ContentValues cv = new ContentValues();
        cv.put(ID, id);
        cv.put(IMAGE, image);
        try{
            database.insert(FAVORITES, null, cv);
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to add in Favorites!!!", Toast.LENGTH_SHORT).show();
        }
        finally {
            database.close();
        }
        return true;
    }


    // Method for remove single image from favorite

    public void removeFavorite(int id) throws SQLiteException {
        database = this.getWritableDatabase();

        database.execSQL("UPDATE " + HISTORY + " SET " + IS_FAVORITE + " = 0 WHERE " + ID + " = " + id);

        String query = "DELETE FROM " + FAVORITES + " WHERE " + ID + " = " + id;
        try{
            database.execSQL(query);
        }
        catch (Exception e) {
            Toast.makeText(context, "Oops, Failed to remove favorite!!!", Toast.LENGTH_SHORT).show();
        }
        finally {
            database.close();
        }
    }
}
