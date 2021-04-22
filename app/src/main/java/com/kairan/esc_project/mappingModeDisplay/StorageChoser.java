package com.kairan.esc_project.mappingModeDisplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.kairan.esc_project.MappingActivity;
import com.kairan.esc_project.R;
import com.kairan.esc_project.TestingMode;

import java.util.ArrayList;

/**
 * This class contains all the maps inside the database,
 * whenever a map is chosen,
 * the map's url is taken and sent to the calling activity(testing or mapping mode)
 */
public class StorageChoser extends AppCompatActivity implements ImageAdapter.OnNoteListener {
    RecyclerView recyclerView;
    RecyclerView.Adapter imageAdapter;
    RecyclerView.LayoutManager layoutManager;
    FirebaseUser user;
    StorageReference storage;
    DatabaseReference database;
    ProgressBar progressBar;
    ArrayList<String> outsideimagelist = new ArrayList<>();
    String CallingActivity= null;


//Set up the recyclerview for the images that are taken from firebase, and the Onclick functions


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_choser);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance().getReference(user.getUid());
        database = FirebaseDatabase.getInstance().getReference("MappedMaps").child(user.getUid());

        ArrayList<String> imagelist = new ArrayList<>();

        recyclerView = findViewById(R.id.RecyclerView);
        progressBar = findViewById(R.id.ProgressBar);
        layoutManager = new LinearLayoutManager(this);
        imageAdapter = new ImageAdapter(this, imagelist,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        CallingActivity = intent.getStringExtra("CallingActivity");

        if (CallingActivity.equals("TestingMode")){
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        imagelist.add(snapshot1.getValue().toString());
                        outsideimagelist.add(snapshot1.getValue().toString());

                    }
                    recyclerView.setAdapter(imageAdapter);
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }


        else{storage.child("document").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference fileRef : listResult.getItems()){
                    if (fileRef != null){fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imagelist.add(uri.toString());
                            outsideimagelist.add(uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            recyclerView.setAdapter(imageAdapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });}
                }
            }
        });
        }}

// Sending the file
    @Override
    public void onNoteClick(int position) {
//        Log.i("TESTT","onNoteClick");
//        Log.i("TESTT",CallingActivity);
//        boolean m = (CallingActivity.equals("TestingMode"));
//        Log.i("TESTT",String.valueOf(m));
//        Log.i("TESTT",CallingActivity);
        String mNote = outsideimagelist.get(position);
        if (CallingActivity.equals("TestingMode")){
            Intent intent = new Intent(getApplicationContext(), TestingMode.class);
            intent.putExtra("Imageselected", mNote);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(getApplicationContext(), MappingActivity.class);
            intent.putExtra("Imageselected", mNote);
            startActivity(intent);}
        }
    }



