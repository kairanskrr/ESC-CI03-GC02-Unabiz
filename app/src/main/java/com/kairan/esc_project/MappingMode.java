package com.kairan.esc_project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

public class MappingMode extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST =1;
    private Uri mImageUri;

    EditText URLEntry;
    ImageView PreviewImage;
    Button DeviceUpload, UrlUpload, ConfirmURL;
    FirebaseUser user;
    DatabaseReference database;
    StorageReference storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping_mode);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("WIFI").child(user.getUid());
        storage = FirebaseStorage.getInstance().getReference("Uploads");

        DeviceUpload = findViewById(R.id.DeviceUpload);
        UrlUpload = findViewById(R.id.UrlUpload);
        PreviewImage = findViewById(R.id.PreviewImage);
        ConfirmURL = findViewById(R.id.ConfirmURL);
        URLEntry = findViewById(R.id.UrlEntry);

        URLEntry.setVisibility(View.GONE);
        ConfirmURL.setVisibility(View.GONE);



        DeviceUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoser();
            }
        });

        UrlUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UrlUpload.setVisibility(View.GONE);
                URLEntry.setVisibility(View.VISIBLE);
                ConfirmURL.setVisibility(View.VISIBLE);
            }
        });

        ConfirmURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String URLlink = URLEntry.getText().toString();
                if (URLlink.isEmpty()){
                    Toast.makeText(MappingMode.this, "Please Enter An URL", Toast.LENGTH_SHORT).show();
                }
                else{
                    LoadImage loadImage = new LoadImage(PreviewImage);
                    loadImage.execute(URLlink);
                    UrlUpload.setVisibility(View.VISIBLE);
                    URLEntry.setVisibility(View.GONE);
                    ConfirmURL.setVisibility(View.GONE);
                }
            }
        });

    }

    private void openFileChoser() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public LoadImage(ImageView PreviewImage){
            this.imageView = PreviewImage;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String URLlink = strings[0];
            Bitmap bitmap = null;
            try {
                InputStream inputStream = new java.net.URL(URLlink).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            PreviewImage.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.with(this).load(mImageUri).into(PreviewImage);
        }
    }
}