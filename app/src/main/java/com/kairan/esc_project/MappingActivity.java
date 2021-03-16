package com.kairan.esc_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.kairan.esc_project.KairanTriangulationAlgo.Mapping;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MappingActivity extends AppCompatActivity {

    SubsamplingScaleImageView imageToMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);

        imageToMap = findViewById(R.id.scaleImage_waitingToMap);

        Intent intent = getIntent();

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
        }
        

    }
}