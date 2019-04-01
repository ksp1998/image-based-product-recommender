package com.example.productrecommendation.productrecommendation;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.WindowCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ViewImageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nav_bar;
    private int position;
    private DatabaseHelper dh;
    private int previousActivity;
    private Bitmap bitmap;
    private int id;
    private Bundle extras;
    private ImageView showImage;
    private String table;
    private Class contextClass;
    private Button result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_view_image);

        init();
    }


    public void init() {

        drawerLayout = findViewById(R.id.drawer);
        nav_bar = findViewById(R.id.nav);
        result = findViewById(R.id.result);
        showImage = findViewById(R.id.showImage);
        dh = new DatabaseHelper(this);

        extras = getIntent().getExtras();
        position = extras.getInt("position");
        previousActivity = extras.getInt("previousActivity");

        if(previousActivity == 1) {
            getSupportActionBar().setTitle("History");
            table = "HISTORY";
            ArrayList<Integer> ids = dh.getIds(table);
            id = ids.get(position);
            contextClass = new HistoryActivity().getClass();
        }
        else {
            getSupportActionBar().setTitle("Favorites");
            table = "FAVORITES";
            ArrayList<Integer> ids = dh.getIds(table);
            id = ids.get(position);
            contextClass = new FavoriteActivity().getClass();
        }

        getSupportActionBar().setSubtitle("Selected Image");
        setupToolbar();
        showImage();
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
                        intent = new Intent(ViewImageActivity.this, HomeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.history :
                        intent = new Intent(ViewImageActivity.this, HistoryActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.favorites :
                        intent = new Intent(ViewImageActivity.this, FavoriteActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.help :
                        intent = new Intent(ViewImageActivity.this, HelpActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    public void showImage() {

        showImage.setImageBitmap(dh.getResourceImage(id, table));
        showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int from;
                int to;
                if(getSupportActionBar().isShowing()){
                    getSupportActionBar().hide();
                    from = Color.WHITE;
                    to = Color.BLACK;
                    result.setVisibility(View.INVISIBLE);
                }
                else{
                    getSupportActionBar().show();
                    from = Color.BLACK;
                    to = Color.WHITE;
                    result.setVisibility(View.VISIBLE);
                }

                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), from, to);
                colorAnimation.setDuration(250);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        drawerLayout.setBackgroundColor((int) animator.getAnimatedValue());
                    }
                });
                colorAnimation.start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        if(dh.isFavorite(id)) {
            menu.getItem(1).setIcon(R.drawable.ic_remove_favorite);
            menu.getItem(1).setTitle("Remove from Favorites");
        }
        else {
            menu.getItem(1).setIcon(R.drawable.ic_add_favorite);
            menu.getItem(1).setTitle("Add to Favorites");
        }
        if(previousActivity == 2) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.delete :
                deleteHistory();
                break;
            case R.id.addRemoveFavorite :
                addOrRemoveFavorite(item);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteHistory() {
        dh = new DatabaseHelper(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(ViewImageActivity.this);
        builder.setMessage("Delete this image from history");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int b_id) {
                        dh.deleteHistoryImage(id);
                        Toast.makeText(ViewImageActivity.this, "Image Deleted!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ViewImageActivity.this, HistoryActivity.class);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder.create();
        alert11.show();
    }


    public void addOrRemoveFavorite(MenuItem item) {

        String msg;
        bitmap = dh.getResourceImage(id, table);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] image = stream.toByteArray();

        if(dh.isFavorite(id)) {
            dh.removeFavorite(id);
            item.setIcon(R.drawable.ic_add_favorite);
            item.setTitle("Add to Favorites");
            msg = "Removed from Favorites";
            if(previousActivity == 2) {
                Intent intent = new Intent(this, contextClass);
                startActivity(intent);
            }
        }
        else {
            dh.addFavorite(id, image);
            item.setIcon(R.drawable.ic_remove_favorite);
            item.setTitle("Remove from Favorites");
            msg = "Added to Favorites";
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void getResult(View v) {

        Intent intent = new Intent(ViewImageActivity.this, ResultActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("previousActivity", previousActivity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
        else {
            Intent intent = new Intent(this, contextClass);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}