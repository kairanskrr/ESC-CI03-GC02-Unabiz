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
import com.kairan.esc_project.KairanTriangulationAlgo.WifiScan;
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
    FirebaseUser user;
    DatabaseReference database;
    StorageReference storage;
    List<ScanResult> scanList;
    String DownloadURL = null;
    Testing testing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_mode);
        image_mappedMap = findViewById(R.id.image_mappedFloorPlan);
        button_selectMap = findViewById(R.id.button_selectMap);
        button_getLocation = findViewById(R.id.button_getLocation);
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
                    image_mappedMap.setImage(ImageSource.bitmap(loadedImage));
                }
            });

            // instantiate Test Object
            testing = new Testing(DownloadURL);
        }

        /**
         Purpose: get prediction of user current position
         Steps:
         1. Get Wifi scan results
         2. Retrieve data from database
         3. Perform the algorithm written in Testing class to get predicted position
         */
        /*button_getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BUTTON", "ButtonGetLocation!");

                // perform 1 scan
                WifiScan wifiScan = new WifiScan(getApplicationContext(),TestingMode.this);
                // store results of scan into wifiScan.scanList
                wifiScan.getWifiNetworksList();
                // store this list into scanList
                scanList = wifiScan.getScanList();
                if(scanList != null){
                    testing.setScanResults(scanList);
                    // using predict() knn to predict where user is
                    Point result = testing.predict();
                    if(result.getX()<0 || result.getY()<0){
                        Toast.makeText(TestingMode.this, "Not able to make prediction for current position",Toast.LENGTH_LONG).show();
                    }

                    else{
                        textView_predictedPosition.setText(result.toString());
                    }
                }
                else{
                    Toast.makeText(TestingMode.this, "Unable to get WiFi scan result",Toast.LENGTH_LONG).show();
                }
            }
        });*/

        /**
         Select map which has been mapped from database
         */
        button_selectMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d("BUTTON", "ButtonSelectMapcalled");
//                storage.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//                        image_mappedMap.setImage (ImageSource.bitmap(bitmap));
//                    }
//                });
                Intent intent = new Intent(TestingMode.this, StorageChoser.class);
                intent.putExtra("CallingActivity", "TestingMode");
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
            //PreviewImage.setImageBitmap(bitmap);
            image_mappedMap.setImage(ImageSource.bitmap(bitmap));
        }
    }



}