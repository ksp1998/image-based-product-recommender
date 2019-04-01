package com.example.productrecommendation.productrecommendation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.productrecommendation.productrecommendation.detect.Classifier;
import com.example.productrecommendation.productrecommendation.detect.TensorFlowImageClassifier;

public class ResultActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "label.txt";
    private static final int INPUT_SIZE = 224;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nav_bar;
    private DatabaseHelper dh;
    private int previousActivity;
    private int id;
    private Bitmap bitmap;
    private Class contextClass;
    private Bundle extras;
    private ImageView image;
    private String url[];
    private LinearLayout resultPanel;
    private ProgressBar progressBar;

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_result);
        getSupportActionBar().setTitle("Result");

        init();
    }

    public void init() {
        drawerLayout = findViewById(R.id.drawer);
        nav_bar = findViewById(R.id.nav);
        image = findViewById(R.id.image);
        dh = new DatabaseHelper(this);
        resultPanel = findViewById(R.id.resultPanel);
        progressBar = findViewById(R.id.progressBar);

        extras = getIntent().getExtras();
        id = extras.getInt("id");
        previousActivity = extras.getInt("previousActivity");
        Log.i("previousactivity", ""+previousActivity);

        initTensorFlowAndLoadModel();
        setupToolbar();
        getResultData();
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    Log.i("TensorFlow : " , "Loaded Successfully");
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
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
                switch(menuItem.getItemId()) {
                    case R.id.home:
                        intent = new Intent(ResultActivity.this, HomeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.history:
                        intent = new Intent(ResultActivity.this, HistoryActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.favorites:
                        intent = new Intent(ResultActivity.this, FavoriteActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.help:
                        intent = new Intent(ResultActivity.this, HelpActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getResultData() {

        if (previousActivity == 2) {
            contextClass = FavoriteActivity.class;
            bitmap = dh.getResourceImage(id, "FAVORITES");
            image.setImageBitmap(bitmap);
        } else {
            contextClass = HistoryActivity.class;
            bitmap = dh.getResourceImage(id, "HISTORY");
            image.setImageBitmap(bitmap);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    onImage();
                    progressBar.setVisibility(View.GONE);
            }
        },1000);
    }

    public void onImage() {

        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

        url = new String[3];

        if(results.isEmpty() != true){
            for(int i = 0; i < results.size(); i++){

                String item = results.get(i).toString();

                String name = "";
                for(int k = 0; k < item.length(); k++) {
                    char ch = item.charAt(k);
                    if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == ' ' || ch == '-'){
                        if(item.charAt(k-1) == ' ')
                            name += Character.toString(ch).toUpperCase();
                        else
                            name += ch;
                    }
                }
                String accuracy = "";
                for(int c = item.length()-7; c < item.length(); c++) {
                    char ch = item.charAt(c);
                    if((ch >= '0' && ch <= '9') || ch == '.'){
                        accuracy += ch;
                    }
                }

                LinearLayout resultView = new LinearLayout(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(100,8,100,8);
                resultView.setLayoutParams(params);
                resultView.setOrientation(LinearLayout.VERTICAL);
                //resultView.setBackgroundColor(Color.rgb(238,238,238));
                resultView.setBackgroundResource(R.drawable.result_view_style);
                resultView.setPadding(8,8,8,8);
                resultPanel.addView(resultView);

                TextView productName = new TextView(this);
                productName.setTextSize(18.0f);
                productName.setTextColor(Color.BLACK);
                productName.setPadding(0,5,0,5);
                productName.setText(i+1 + ") Product : " + name.trim());
                resultView.addView(productName);

                TextView productAccuracy = new TextView(this);
                productAccuracy.setTextSize(17.0f);
                if((Float.parseFloat(accuracy) < 50.0f))
                    productAccuracy.setTextColor(Color.RED);
                else
                    productAccuracy.setTextColor(Color.rgb(9,107,5));
                productAccuracy.setPadding(50,5,0,5);
                productAccuracy.setText("Accuracy : " + accuracy + " %");
                resultView.addView(productAccuracy);

                TextView productLink = new TextView(this);
                productLink.setTextSize(16.0f);
                productLink.setTextColor(Color.rgb(0, 80, 255));
                productLink.setPadding(50,5,0,5);
                url[i] = "https://www.flipkart.com/search?q=" + name.trim().replace(" ", "-");
                productLink.setText(url[i].toLowerCase());
                resultView.addView(productLink);

                final int res = i;
                productLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if(res == 0)
                            intent.setData(Uri.parse(url[0]));
                        else if(res == 1)
                            intent.setData(Uri.parse(url[1]));
                        else
                            intent.setData(Uri.parse(url[2]));
                        startActivity(intent);
                    }
                });
            }
            Toast.makeText(this, "Click on the link to find product", Toast.LENGTH_SHORT).show();
        }
        else{
            TextView errorMsg = new TextView(this);
            errorMsg.setTextSize(20.0f);
            errorMsg.setTextColor(Color.RED);
            errorMsg.setPadding(0,5,0,5);
            errorMsg.setText("No product Found!");
            resultPanel.addView(errorMsg);
        }

    }

    @Override
    public void onBackPressed() {
        if(previousActivity == 0)
            contextClass = HomeActivity.class;
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
