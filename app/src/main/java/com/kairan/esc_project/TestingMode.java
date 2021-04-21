package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kairan.esc_project.KairanTriangulationAlgo.Point;
import com.kairan.esc_project.KairanTriangulationAlgo.Testing;
import com.kairan.esc_project.KairanTriangulationAlgo.Testing2;
import com.kairan.esc_project.KairanTriangulationAlgo.WifiScan;
import com.kairan.esc_project.UIStuff.CircleView;
import com.kairan.esc_project.UIStuff.PinView;
import com.kairan.esc_project.mappingModeDisplay.StorageChoser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TestingMode extends AppCompatActivity {
    SubsamplingScaleImageView image_mappedMap;
    TextView textView_predictedPosition;
    Button button_selectMap;
    Button button_getLocation;
    Button button_back;
    FirebaseUser user;
    DatabaseReference database;
    StorageReference storage;
    List<ScanResult> scanList;
    String DownloadURL = null;
    Testing testing;
    Testing2 testing2;

    Canvas mCanvas;
    private final Paint mPaint = new Paint();
    private Bitmap mBitmap;
    private final float radius = 100f;
    private final int alpha = 100;
    private Bitmap pin;
    private Bitmap loadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_mode);
        image_mappedMap = findViewById(R.id.image_mappedFloorPlan);
        button_selectMap = findViewById(R.id.button_selectMap);
        button_getLocation = findViewById(R.id.button_getLocation);
        button_back = findViewById(R.id.button_back);
        textView_predictedPosition = findViewById(R.id.textView_predictedPosition);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());
        storage = FirebaseStorage.getInstance().getReference(user.getUid());

        Intent intent = getIntent();
        if (intent.getStringExtra("Imageselected") != null) {
            DownloadURL = intent.getStringExtra("Imageselected");

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(TestingMode.this).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            imageLoader.loadImage(DownloadURL, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //super.onLoadingComplete(imageUri, view, loadedImage);
                    loadImage = loadedImage;
                    mBitmap = loadedImage.copy(Bitmap.Config.ARGB_8888, true);
                    image_mappedMap.setImage(ImageSource.bitmap(mBitmap));
                }
            });
            // instantiate Test Object
            //testing = new Testing(DownloadURL);  //
            testing = new Testing(MappingActivity.getMappingData());
            testing2 = new Testing2(MappingActivity.getMappingData(),MappingActivity.getAPs());
            //testing = new Testing(MappingActivity.getMappingData(),MappingActivity.getAPs());
        }



        /**
         Purpose: get prediction of user current position
         Steps:
         1. Get Wifi scan results
         2. Retrieve data from database
         3. Perform the algorithm written in Testing class to get predicted position
         */
        button_getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BUTTON", "ButtonGetLocation!");

                // perform 1 scan
                WifiScan wifiScan = new WifiScan(getApplicationContext(),TestingMode.this);
                // store results of scan into wifiScan.scanList
                wifiScan.getWifiNetworksList();
                // store this list into scanList
                scanList = wifiScan.getScanList();
                Log.i("AAAAA",scanList.toString());
                if(scanList != null){
                    testing2.setScanResult(scanList);
                    Point result = testing2.predict();
                    testing.setScanResults(scanList);
                    // using predict() knn to predict where user is:
                    // use local data
                    //Point result = testing.predict();

                    // use firebase retrieving data (not working: dataSet is still empty)
//                    Log.i("TTTTT","Download URL: "+DownloadURL);
//                    Point result = Testing.get_data_for_testing(DownloadURL);

                    // use NN model
                    //Point result = Testing.predict_nn();
                    if(result == null){
                        Log.i("TTTTT","data set is empty");
                        Toast.makeText(TestingMode.this,"Please complete mapping first before testing",Toast.LENGTH_LONG).show();
                    }
                    else if(result.getX()<0 || result.getY()<0){
                        Toast.makeText(TestingMode.this, "Not able to make prediction for current position",Toast.LENGTH_LONG).show();
                    }
                    else{
                        //Toast.makeText(TestingMode.this,"Prediction has been made!",Toast.LENGTH_SHORT).show();
                        textView_predictedPosition.setText(result.toString());

                        // draw circle
                        mCanvas = new Canvas(mBitmap);
                        mCanvas.drawColor(0,PorterDuff.Mode.CLEAR);
                        mCanvas.drawBitmap(loadImage.copy(Bitmap.Config.ARGB_8888,true),0,0,null);
                        mPaint.setColor(Color.BLACK);
                        mPaint.setStrokeWidth(10);
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setAlpha(alpha);
                        // offset x and y so that it appears at centre of arrow
                        Log.i("TTTTT","x: "+result.getX());
                        Log.i("TTTTT","y: "+result.getY());
                        mCanvas.drawCircle((float)result.getX(), (float)result.getY(), radius, mPaint);
                        pin = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
                        Log.i("TTTTT","draw bitmap");
                        mCanvas.drawBitmap(pin,(float)result.getX()-(pin.getWidth()/2),(float)result.getY() -(pin.getHeight()),null);
                        //v.draw(mCanvas);
                        Log.i("TTTTT","invalidate");
                        v.invalidate();
                    }
                }
                else{
                    Toast.makeText(TestingMode.this, "Unable to get WiFi scan result",Toast.LENGTH_LONG).show();
                }
            }
        });

        /**
         Select map which has been mapped from database
         */
        button_selectMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestingMode.this, StorageChoser.class);
                intent.putExtra("CallingActivity", "TestingMode");
                startActivity(intent);
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestingMode.this,SelectMenu.class);
                startActivity(intent);
            }
        });

    }
    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        SubsamplingScaleImageView imageView;
        URL url;
        public LoadImage(SubsamplingScaleImageView PreviewImage){
            this.imageView = PreviewImage;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String URLlink = strings[0];
            Bitmap bitmap = null;
            try {
                if(!URLlink.isEmpty()){
                    url = new URL(URLlink);
                }
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                //InputStream inputStream = new java.net.URL(URLlink).openStream();
                InputStream inputStream = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            image_mappedMap.setImage(ImageSource.bitmap(bitmap));
        }
    }



}