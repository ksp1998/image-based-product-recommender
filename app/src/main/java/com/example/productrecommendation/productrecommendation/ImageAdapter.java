package com.example.productrecommendation.productrecommendation;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.productrecommendation.productrecommendation.database.DatabaseHelper;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    // Declaration of objects

    Context context;
    int width;
    int height;

    public Bitmap[] images = null;

    ArrayList<Bitmap> bitmaps = null;


    // Getting History images or Favorites images from database

    public void getImages() {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        if(context instanceof HistoryActivity) {
            bitmaps = databaseHelper.getHistory();
        }
        if(context instanceof FavoriteActivity){
            bitmaps = databaseHelper.getFavorites();
        }
        images = new Bitmap[bitmaps.size()];
        images = bitmaps.toArray(images);
    }


    // Adapter Constructor for initializing objects

    public ImageAdapter(Context c, int w, int h) {
        context = c;
        width = w;
        height = h;
        getImages();
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int position) {
        return images[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setLayoutParams(new GridView.LayoutParams(width, height));
        image.setImageBitmap(images[position]);
        return image;
    }
}
