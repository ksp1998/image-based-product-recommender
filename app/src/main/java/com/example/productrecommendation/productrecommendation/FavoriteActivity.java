package com.example.productrecommendation.productrecommendation;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nav_bar;
    private int width, height;
    private  int pos;
    private ArrayList<Bitmap> bitmaps;
    private DatabaseHelper dh;
    private GridView gridView;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_favorite);
        getSupportActionBar().setTitle("Favorites");

        init();
    }


    public void init() {
        drawerLayout = findViewById(R.id.drawer);
        nav_bar = findViewById(R.id.nav);
        dh = new DatabaseHelper(this) ;
        text = findViewById(R.id.text);
        gridView = findViewById(R.id.gridView);
        width = getWindowManager().getDefaultDisplay().getWidth()/3;
        height = getWindowManager().getDefaultDisplay().getHeight()/4;

        setupToolbar();
        addFavoriteData();
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
                        intent = new Intent(FavoriteActivity.this, HomeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.history :
                        intent = new Intent(FavoriteActivity.this, HistoryActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.favorites :
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.help :
                        intent = new Intent(FavoriteActivity.this, HelpActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        menu.getItem(1).setVisible(false);

        bitmaps = dh.getFavorites();
        if(bitmaps.size() != 0) {
            text.setVisibility(View.INVISIBLE);
        }
        else{
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item))
            return true;

        if(item.getItemId() == R.id.delete)
            removeFavorites();

        return super.onOptionsItemSelected(item);
    }

    public void addFavoriteData() {

        gridView.setAdapter(new ImageAdapter(this, width, height));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("previousActivity", 2);
                startActivity(intent);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                pos = position;

                PopupMenu popup = new PopupMenu(FavoriteActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch(item.getItemId()){
                            case R.id.delete :
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(FavoriteActivity.this);
                                builder1.setMessage("Remove this image from favorites");
                                builder1.setCancelable(true);

                                builder1.setPositiveButton(
                                        "Delete",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                ArrayList<Integer> ids = dh.getIds("HISTORY");
                                                int selected_image_id = ids.get(pos);
                                                dh.removeFavorite(selected_image_id);
                                                Toast.makeText(FavoriteActivity.this, "Image Deleted!", Toast.LENGTH_SHORT).show();
                                                finish();
                                                startActivity(getIntent());
                                            }
                                        });

                                builder1.setNegativeButton(
                                        "Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                                break;
                            case R.id.cancel :
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
                return true;
            }
        });

    }

    public void removeFavorites(){

        final DatabaseHelper dh = new DatabaseHelper(this) ;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(FavoriteActivity.this);
        builder1.setMessage("Do you really want to remove entire Favorites?");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dh.deleteFavorites();
                        finish();
                        startActivity(getIntent());
                        Toast.makeText(FavoriteActivity.this, "Favorites Removed!", Toast.LENGTH_SHORT).show();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        }
        else {
            Intent intent = new Intent(FavoriteActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}
