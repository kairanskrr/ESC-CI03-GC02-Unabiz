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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference("WIFI");    // create reference to firebase, create wifi header

    private StringBuilder stringBuilder = new StringBuilder();
    private TextView textViewWifiNetworks;
    private Button buttonClick;
    private List<ScanResult> scanList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewWifiNetworks = findViewById(R.id.txtWifiNetworks);
        buttonClick = findViewById(R.id.button_click);
        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWifiNetworksList();
            }
        });




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
                stringBuilder = new StringBuilder();
                scanList = wifiManager.getScanResults();
                System.out.println(scanList.size());
                stringBuilder.append("\n Number Of Wifi connections : " + " " + scanList.size() + "\n\n");
                for (int i = 0; i < scanList.size(); i++) {
                    stringBuilder.append(new Integer(i + 1).toString() + ". ");
                    stringBuilder.append(String.format("Name: %s,\nBSSID: %s,\nRSSI: %s\n",(scanList.get(i)).SSID,(scanList.get(i)).BSSID,(scanList.get(i)).level));
                    String name = scanList.get(i).SSID;
                    Integer rssi = scanList.get(i).level; //multiple rssi values with the same network can be detected
                    database.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.hasChild(name)){database.child(name).setValue(rssi);}
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    }); // Send name of wifi and the rssi number into database, only if the wifi data is not already inside firebase
                    //sb.append("\n\n");
                }

                textViewWifiNetworks.setText(stringBuilder);
                System.out.println(stringBuilder);
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
