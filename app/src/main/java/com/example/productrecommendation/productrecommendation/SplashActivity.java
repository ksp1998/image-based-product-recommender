package com.example.productrecommendation.productrecommendation;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.content.Intent;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.productrecommendation.productrecommendation.database.DatabaseHelper;

import java.io.ByteArrayOutputStream;

public class SplashActivity extends AppCompatActivity {

    // ImageView which holds the splash screen icon
    private ImageView image;

    // Getting images from drawable to store in History
    private int[] images = new int[] {
            R.drawable.camera, R.drawable.cup, R.drawable.football, R.drawable.keyboard,
            R.drawable.mouse, R.drawable.pc, R.drawable.wall_clock
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        image = findViewById(R.id.image);
        image.animate().alpha(0.6f).setDuration(2000);

        getHistoryImagesFromDrawable();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }


    // This method will preload images in History from drawable

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getHistoryImagesFromDrawable() {

        SharedPreferences loadimages = getSharedPreferences("PREFS_NAME", 0);
        if (!loadimages.getBoolean("FIRST_RUN", false)) {
            // do the thing for the first time
            DatabaseHelper db = new DatabaseHelper(this);

            for(int i = 0; i < images.length; i++){
                Drawable d = getDrawable(images[i]);
                Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                byte[] bitmapdata = stream.toByteArray();
                db.addToHistory(bitmapdata);
            }
            SharedPreferences.Editor editor = loadimages.edit();
            editor.putBoolean("FIRST_RUN", true);
            editor.commit();
        }
    }
}
