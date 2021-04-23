package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.kairan.esc_project.KairanTriangulationAlgo.Mapping2;
import com.kairan.esc_project.KairanTriangulationAlgo.Testing;
import com.kairan.esc_project.KairanTriangulationAlgo.Testing2;
import com.kairan.esc_project.UIStuff.CircleView;
import com.kairan.esc_project.UIStuff.PinView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.HashMap;
import java.util.List;


/**
 * Second activity of the mapping mode, where the user does the real mapping
 */

public class MappingActivity extends AppCompatActivity {

    SubsamplingScaleImageView imageToMap;
    TextView textView_currentPosition;
    Mapping mapping = new Mapping();
    Mapping2 mapping2 = new Mapping2();
    private List<ScanResult> scanList;
    private List<ScanResult> scanList2;
    Button button_savePosition, button_complete_mapping;
    private PinView view;

    // DEBUG: REMOVE LATER, SCROLL VIEW TO DISPLAY WIFI_AP
    private ScrollView scrollViewPositionAp;
    private TextView textViewPositionAP;

    // for the circleview on the map
    private Canvas mCanvas;
    private final Paint mPaint = new Paint();
    private Bitmap mBitmap;
    private final float radius = 100f;
    private final int alpha = 100;
    private PointF currPos;
    private Bitmap pin;

    private float x;
    private float y;

    // save data
    private static HashMap<Point,HashMap<String, Integer>> mappingData = new HashMap<>();
    private static List<String> aps = new ArrayList<>();

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
        textView_currentPosition = findViewById(R.id.textView_currentPosition);
        button_savePosition = findViewById(R.id.button_save_position);
        button_complete_mapping = findViewById(R.id.button_complete_mapping);
        view = findViewById(R.id.pinView_mapping);
        view.setVisibility(View.INVISIBLE);

        // DEBUG: REMOVE LATER, SCROLL VIEW TO DISPLAY WIFI_AP
        scrollViewPositionAp = findViewById(R.id.scrollViewPosition_ap);
        textViewPositionAP = findViewById(R.id.textViewposition_ap);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());
        storage = FirebaseStorage.getInstance().getReference(user.getUid()).child("Mapped");

        // retrieve from database
        // Load the new image that is selected by the user

        /**
         * Getting the image that the user first wanted in the MappingMode and displaying it using the URL of the map
         */
        Intent intent = getIntent();
            DownloadURL = intent.getStringExtra("Imageselected");
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MappingActivity.this).build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            imageLoader.loadImage(DownloadURL,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mBitmap = loadedImage.copy(Bitmap.Config.ARGB_8888, true);
                    imageToMap.setImage(ImageSource.bitmap(mBitmap));
                }
            });

        // setting the zoom
        imageToMap.setMinimumDpi(20);

        /**
         * When a user long press on the screen, the user shld see a location pin in the map
         */
        imageToMap.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getApplicationContext(),new GestureDetector.SimpleOnGestureListener(){
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public void onLongPress(MotionEvent e) {
                    x = (float)Math.floor(e.getX()*100)/100;
                    y = (float)Math.floor(e.getY()*100)/100;
                    Log.i("TTTTT","HEIGHT: "+imageToMap.getSHeight());
                    Log.i("TTTTT","WIDTH: "+imageToMap.getSWidth());
                    // textView_currentPosition.setText(imageToMap.viewToSourceCoord(x,y).toString());
                    currPos = imageToMap.viewToSourceCoord(x,y);
                    // current position printed out, this position is wrt to the image!
                    textView_currentPosition.setText("X: " + currPos.x + " Y: " + currPos.y);
                    Log.i("MAPPOSITION",imageToMap.viewToSourceCoord(x,y).toString());
                    super.onLongPress(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                // moving position pin
                view.setX(x);
                view.setY(y);
                view.setVisibility(View.VISIBLE);
                return false;
            }
        });

        /**
         * When the user saves the position of the map, the app scans and get a scanresult, then save the data of the location,
         * The app then displays to the user a circled area around the point that has been mapped,
         * In order to show the location that has been mapped around it
         */
        button_savePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = String.valueOf(textView_currentPosition.getText());
                if(text.isEmpty()){
                    Toast.makeText(MappingActivity.this,"Please indicate where you are first",Toast.LENGTH_LONG).show();
                }else{
                    Log.d("PRESS", "BUTTON SAVE PRESSED");

                    // each time button clicked, 1 scan performed
                    Toast.makeText(MappingActivity.this,"Scanning WiFi...",Toast.LENGTH_LONG).show();
                    WifiScan wifiScan = new WifiScan(getApplicationContext(), MappingActivity.this);
                    wifiScan.getWifiNetworksList();
                    scanList = wifiScan.getScanList();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    wifiScan.getWifiNetworksList();
                    scanList2 = wifiScan.getScanList();


                    if(scanList != null && scanList2 != null){
                        // mapping.add_data first filters scanList then append to position_ap.
                        // debug because the point on screen is not the same as the one stored in position_ap
                        mapping.add_data(new Point(currPos.x, currPos.y), scanList);
                        mapping2.add_data(new Point(currPos.x, currPos.y), scanList,scanList2);

                        // print out point
                        Log.d("PRESS", "The Point is + " + String.valueOf(new Point(currPos.x, currPos.y)));
                        Log.d("PRESS", "position_ap is" + mapping.position_ap);
                        // DEBUG: REMOVE LATER, SCROLL VIEW TO DISPLAY WIFI_AP
                        textViewPositionAP.setText(String.valueOf(mapping.position_ap));

                        Log.d("PRESS", "ADDED DATA COMPLETE!!");
                        Toast.makeText(MappingActivity.this,"Save successfully",Toast.LENGTH_LONG).show();
                        // debug log to make list shop
                        Log.d("PRESS", "PRESSED SAVE");
                        // debug to show scanList
                        Log.d("PRESS", scanList.toString());

                        // clear the textView_currentPosition
                        textView_currentPosition.setText("");
                        view.setVisibility(View.INVISIBLE);

                        // draw circle
                        mCanvas = new Canvas(mBitmap);
                        mPaint.setColor(getResources().getColor(R.color.coordinate_color));
                        mPaint.setStrokeWidth(10);
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setAlpha(alpha);

                        // offset x and y so that it appears at centre of arrow
                        mCanvas.drawCircle(currPos.x, currPos.y, radius, mPaint);

                        // draw a circle at the center of the big one to indicate where you pressed
                        mCanvas.drawCircle(currPos.x, currPos.y, 10, mPaint);

                        pin = BitmapFactory.decodeResource(getResources(), R.drawable.green_pin);
                        mCanvas.drawBitmap(pin,currPos.x-(pin.getWidth()/2),currPos.y -(pin.getHeight()),null);
                        v.invalidate();
                    }
                }
            }
        });

        /**
         * When the user press complete mapping, all the data of the different location of the map will be sent to the database,
         * mapping completed
         */
        button_complete_mapping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mappingData = mapping.getPosition_ap();
                mappingData = mapping2.getPosition_ap();

                if (mappingData.size() < Testing2.K) {
                    Toast.makeText(MappingActivity.this, String.format("At least %d entried are needed to complete mapping", Testing2.K), Toast.LENGTH_LONG).show();
                } else {
                    //mapping.send_data_to_database(DownloadURL, getApplicationContext());
                    mapping2.send_data(DownloadURL, getApplicationContext());
//                    DatabaseReference database2 = FirebaseDatabase.getInstance().getReference("MappedMaps").child(user.getUid());
//                    database2.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            long number = snapshot.getChildrenCount()+1;
//                            database2.child(Long.toString(number)).setValue(DownloadURL);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                        }
//                    });
                    //mapping2.train_data(5);
                    aps = mapping.getAp_list();
                    aps = mapping2.getAp_list();

                    HashMap<Point, HashMap<String, Integer>> test = mapping.getPosition_ap();
                    List<Point> test_point = new ArrayList<>(test.keySet());
                    for (Point x : test_point) {
                        Log.i("AAAAAA", x.toString());
                        Log.i("AAAAAA", test.get(x).toString());
                    }
                    Log.i("TTTTT", mappingData.toString());
                    Log.i("TTTTT", aps.size() + "\t" + aps.toString());
                    Intent intent = new Intent(MappingActivity.this, SelectMenu.class);
                    startActivity(intent);
                }
            }
        });
    }


    public static HashMap<Point, HashMap<String, Integer>> getMappingData() {
        return mappingData;
    }

    public static List<String> getAPs(){
        Log.i("TTTTT","return aps"+aps.size()+"\t"+aps.toString());
        return aps;
    }

    /**
     * Download the image using URL from the internet to display
     */
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
    }