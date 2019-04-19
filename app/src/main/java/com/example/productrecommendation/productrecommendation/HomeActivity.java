package com.example.productrecommendation.productrecommendation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.productrecommendation.productrecommendation.database.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class HomeActivity extends AppCompatActivity {

    // Declaration of View objects

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nav_bar;

    // Declaration of objects

    private int id;
    private boolean doubleBackToExitPressedOnce = false;

    //// Creating database object for accessing database class
    private DatabaseHelper dh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("Home");

        //calling initial method
        init();
    }


    // Initializing objects and calling necessary methods

    public void init(){
        drawerLayout = findViewById(R.id.drawer);
        nav_bar = findViewById(R.id.nav);
        dh = new DatabaseHelper(this);

        setupToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupToolbar() {
        toggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nav_bar.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch(menuItem.getItemId()){
                    case R.id.home :
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.history :
                        intent = new Intent(HomeActivity.this, HistoryActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.favorites :
                        intent = new Intent(HomeActivity.this, FavoriteActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.help :
                        intent = new Intent(HomeActivity.this, HelpActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }


    // Opening Camera when button clicked

    public void openCamera(View v) {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera,1);
    }


    // Opening External storage when button clicked

    public void uploadImage(View v) {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Bitmap imageBitmap = null;
        boolean found = false;
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            found = true;
        }
        else if(requestCode == 100 && resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                imageBitmap = BitmapFactory.decodeStream(imageStream);
                found = true;
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // If image found then
        if(found) {
            addToHistory(imageBitmap);
        }
    }


    // Storing captured or uploaded image to history table in database

    public void addToHistory(Bitmap imageBitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byte[] imageInByte = stream.toByteArray();

            id = dh.addToHistory(imageInByte);

            if (id != -1) {
                getResult();
                //toastMessage("Image stored in History");
            }
            else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            //Log.i("Success", "FALSE");
            Toast.makeText(this, "Oops!!! Error Ocurred ", Toast.LENGTH_SHORT).show();
        }
    }


    // Redirect to ResultActivity when image is captured or uploaded

    public void getResult() {
        Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }


    // Override function for exiting application when double back clicked

    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawers();
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                ActivityCompat.finishAffinity(HomeActivity.this);
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }
}
