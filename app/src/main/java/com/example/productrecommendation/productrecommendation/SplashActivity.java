package com.example.productrecommendation.productrecommendation;

import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.Thread;

public class SplashActivity extends AppCompatActivity {

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        image = findViewById(R.id.image);
        image.animate().alpha(0.6f).setDuration(2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }
}
