package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.kairan.esc_project.UIStuff.CircleView;
import com.kairan.esc_project.UIStuff.PinView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kairan.esc_project.KairanTriangulationAlgo.Mapping;
import com.kairan.esc_project.KairanTriangulationAlgo.Point;
import com.kairan.esc_project.KairanTriangulationAlgo.WifiScan;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MappingActivity extends AppCompatActivity {

    SubsamplingScaleImageView imageToMap;
    TextView textView_currentPosition;
    Mapping mapping = new Mapping();
    private List<ScanResult> scanList;
    Button button_savePosition, button_complete_mapping;
    private PinView view;
    private CircleView circleView;

    // for the circle
    private Canvas mCanvas;
    private Paint mPaint = new Paint();
    private Bitmap mBitmap;
    private final float radius = 100f;
    private final int alpha = 100;


    private float x;
    private float y;
    private float x_bm;
    private float y_bm;


    DatabaseReference database;
    FirebaseUser user;
    StorageReference storage;

    String DownloadURL = null;
    Uri mImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        imageToMap = findViewById(R.id.scaleImage_waitingToMap);
        textView_currentPosition = findViewById(R.id.textViw_currentPosition);
        button_savePosition = findViewById(R.id.button_save_position);
        button_complete_mapping = findViewById(R.id.button_complete_mapping);
        view = findViewById(R.id.pinView_mapping);
        view.setVisibility(View.INVISIBLE);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        storage = FirebaseStorage.getInstance().getReference(user.getUid());


//        // retrieve from database
//        imageToMap.setImage(ImageSource.resource(R.drawable.b2l2));

        //Load the new image that is selected by the user
        Intent intent = getIntent();
            DownloadURL = intent.getStringExtra("Imageselected");
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MappingActivity.this).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            imageLoader.loadImage(DownloadURL,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //super.onLoadingComplete(imageUri, view, loadedImage);
                    mBitmap = loadedImage.copy(Bitmap.Config.ARGB_8888, true);
                    imageToMap.setImage(ImageSource.bitmap(mBitmap));
                }
            });



        // working on it... turned into touch to obtain coordinate instead of long press
        /*imageToMap.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // obtain x and y
                x = (float)Math.floor(event.getX()*100)/100;
                y = (float)Math.floor(event.getY()*100)/100;
                String locationInXY = new Point(x, y).toString();
                textView_currentPosition.setText("Your location is "+ locationInXY);
                Log.d("onTouch called", "onTouch");

                // the screen turns white
                *//*view = new PinView(getApplicationContext());

                view.setPin(new PointF(x, y));
                setContentView(view);*//*

                view.setVisibility(View.VISIBLE);
                view.setPin(new PointF(x,y));
                view.setX(x);
                view.setY(y);
                //setContentView(view);
                return true;
            }
        });*/

        imageToMap.setMinimumDpi(20);

        imageToMap.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener(){
                @Override
                public void onLongPress(MotionEvent e) {
                    x = (float)Math.floor(e.getX()*100)/100;
                    y = (float)Math.floor(e.getY()*100)/100;

                    PointF pointF = imageToMap.sourceToViewCoord(x,y);
                    x_bm = pointF.x;
                    y_bm = pointF.y;
                    textView_currentPosition.setText(imageToMap.viewToSourceCoord(x,y).toString());
                    Log.i("MAPPOSITION",imageToMap.viewToSourceCoord(x,y).toString());
                    super.onLongPress(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                view.setPin(new PointF(x,y));
                view.setX(x);
                view.setY(y);
                view.setVisibility(View.VISIBLE);
                return false;
            }
        });

        button_savePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = String.valueOf(textView_currentPosition.getText());
                if(text.isEmpty()){
                    Toast.makeText(MappingActivity.this,"Please indicate where you are first",Toast.LENGTH_LONG).show();
                }else{

                    // each time button clicked, 1 scan performed
                    WifiScan wifiScan = new WifiScan(getApplicationContext(), MappingActivity.this);
                    wifiScan.getWifiNetworksList();
                    scanList = wifiScan.getScanList();
                    if(scanList != null){

                        float x = Float.parseFloat(text.substring(7,text.indexOf(",")));
                        float y = Float.parseFloat(text.substring(text.indexOf(",")+2,text.length()-1));
                        // add data, adding to position_ap of mapping object

                        /*float x = Float.parseFloat(text.substring(7,text.indexOf(",")));
                        float y = Float.parseFloat(text.substring(text.indexOf(",")+2,text.length()-1));*/

                        mapping.add_data(new Point(x,y),scanList);
                        Toast.makeText(MappingActivity.this,"Save successfully",Toast.LENGTH_LONG).show();

                        // clear the textView_currentPosition
                        textView_currentPosition.setText("");
                        view.setVisibility(View.INVISIBLE);

                        // draw circle
                        mCanvas = new Canvas(mBitmap);
                        mPaint.setColor(Color.GRAY);
                        mPaint.setAlpha(alpha);
                        float centerX = x;
                        float centerY = y;
                        mCanvas.drawCircle(centerX, centerY, radius, mPaint);
                        v.invalidate();
                    }
                }
            }
        });

        button_complete_mapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapping.send_data_to_database(); // need to be implemented
                Log.i("TESTING", "This has been clicked");
//                Intent intent = new Intent(MappingActivity.this,SelectMenu.class);
//                startActivity(intent);
            }
        });

        /*Intent intent = getIntent();

        Log.i("IMAGE",String.valueOf(intent.getExtras().get(MappingMode.IMAGE_URL)));
        Log.i("IMAGE",String.valueOf(intent.getExtras().get(MappingMode.IMAGE_DEVICE)));

        String image_url = intent.getExtras().getString(MappingMode.IMAGE_URL);
        Bitmap image_device = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra(MappingMode.IMAGE_DEVICE),0,getIntent().getByteArrayExtra(MappingMode.IMAGE_DEVICE).length);

        //Bitmap image_device = (Bitmap)intent.getExtras().get(MappingMode.IMAGE_DEVICE);

        if(image_url!=null){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MappingActivity.this).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            imageLoader.loadImage(image_url,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    //super.onLoadingComplete(imageUri, view, loadedImage);
                    imageToMap.setImage(ImageSource.bitmap(loadedImage));
                }
            });
        }else{
            imageToMap.setImage(ImageSource.bitmap(image_device));
        }*/


        

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
            imageToMap.setImage(ImageSource.bitmap(bitmap));
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();
            imageToMap.setImage(ImageSource.uri(mImageUri));


        }
    }



//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
//            mImageUri = data.getData();
//            imageToMap.setImage(ImageSource.uri(mImageUri));
//
//
//        }
//    }





    }



    // make use of WifiManager to get the available Wifi APs nearby


