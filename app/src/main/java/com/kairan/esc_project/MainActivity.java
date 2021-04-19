package com.kairan.esc_project;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kairan.esc_project.KairanTriangulationAlgo.Mapping;
import com.kairan.esc_project.KairanTriangulationAlgo.Point;
import com.kairan.esc_project.KairanTriangulationAlgo.Testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This activity is just for testing purposes
 */

public class MainActivity extends AppCompatActivity {
    FirebaseUser user;
    DatabaseReference database;

    private StringBuilder stringBuilder = new StringBuilder();
    private TextView textViewWifiNetworks;
    private Button buttonClick, FirebaseData;
    private List<ScanResult> scanList;
    private ListView listView_wifiList;
    private ArrayList<String> wifiList;
    private ArrayAdapter arrayAdapter;

    int currentchildnumber =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());

        textViewWifiNetworks = findViewById(R.id.textViewWifiNetworks);
        listView_wifiList = findViewById(R.id.listView_wifi);
        buttonClick = findViewById(R.id.button_click);
        FirebaseData = findViewById(R.id.FirebaseData);

        wifiList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,wifiList);

        buttonClick.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getWifiNetworksList();
                textViewWifiNetworks.setVisibility(View.GONE);
                listView_wifiList.setAdapter(arrayAdapter);
            }
        });

        FirebaseData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Testing.get_data_for_testing("https://firebasestorage.googleapis.com/v0/b/unabizproject.appspot.com/o/q4ySFmLhL9RbqccQ8BMid6oZWht1%2Fexternal%2Fimages%2Fmedia%2F30?alt=media&token=2f1f5024-3eb2-4209-8621-dcc7698119dc");
        }});

    }
    // make use of WifiManager to get the available Wifi APs nearby
    /**
     * for wifi scan result:
     * BSSID: address of the access point (MAC address)
     * SSID: network name
     * level: detected signal level in dBm (RSSI)*/

    private void getWifiNetworksList() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        final WifiManager wifiManager =
                (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        registerReceiver(new BroadcastReceiver() {

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onReceive(Context context, Intent intent) {
                //stringBuilder = new StringBuilder();
                scanList = wifiManager.getScanResults();
                System.out.println(scanList.size());
                wifiList.add("Number Of Wifi connections : " + " " + scanList.size() + "\n\n");
                for (int i = 0; i < scanList.size(); i++) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(new Integer(i + 1).toString() + ". ");
                    stringBuilder.append(String.format("Name: %s,\nBSSID: %s,\nRSSI: %s\n",(scanList.get(i)).SSID,(scanList.get(i)).BSSID,(scanList.get(i)).level));
                    wifiList.add(stringBuilder.toString());
                    String Mac_address = scanList.get(i).BSSID;
                    Integer rssi = scanList.get(i).level;
                    database.child("Scan " + currentchildnumber).child("Nearby WIFI Data Values").child(Mac_address).setValue(rssi);
                    //sb.append("\n\n");
                }
                arrayAdapter.notifyDataSetChanged();
                //textViewWifiNetworks.setText(stringBuilder);
                //System.out.println(stringBuilder);
            }

        }, filter);

        boolean startScan = wifiManager.startScan();
        if(!startScan){
            Toast.makeText(MainActivity.this,"Please Enable Access of Location",Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
    }
}
