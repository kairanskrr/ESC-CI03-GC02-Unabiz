package com.kairan.esc_project.KairanTriangulationAlgo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kairan.esc_project.KairanTriangulationAlgo.NeuralNetwork;
import com.kairan.esc_project.KairanTriangulationAlgo.Point;
import com.kairan.esc_project.MappingActivity;
import com.kairan.esc_project.UIStuff.PinView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Mapping {

    private HashMap<String,Integer> mac_rssi;
    // i want to see what is position_ap
//    private HashMap<Point,HashMap> position_ap;
    static public HashMap<Point,HashMap<String, Integer>> position_ap;
    static Map<String, HashMap> position_apclone = new HashMap<>();
    List<String> ap_list;
    static List<Point> position_list;
    static int num_of_data;

    static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    static DatabaseReference database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());
    static DatabaseReference MapUrls = FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid());
    static HashMap<Point, HashMap<String,Integer>> Map1;

    public Mapping(){
        position_ap = new HashMap<>();
        position_list = new ArrayList<>();
        ap_list = new ArrayList<>();
        num_of_data = 0;
    }

    /**
     * Collect data from wifi scan results (bssid, rssi) and map it with its position on the floor map (x,y)*/
    /**
     * User inputs position on the map, along with the obtained scanResult at that position
     * @param position
     * @param scanResult
     */

    public void add_data(Point position, List<ScanResult> scanResult){

        mac_rssi = new HashMap<>();

        // for 1 scan result itself, add the BSSID (MAC) and corresponding RSSI to mac_rssi hashmap
        for (int i = 0; i < scanResult.size(); i++){
            if(!ap_list.contains(scanResult.get(i).BSSID)){
                ap_list.add(scanResult.get(i).BSSID);
            }
            if(20<Math.abs(scanResult.get(i).level)&&Math.abs(scanResult.get(i).level)<70){
                mac_rssi.put(scanResult.get(i).BSSID,scanResult.get(i).level);
            }

        }

        // add mac_rssi entry to global position_ap hashmap (position : mac_rssi)
        position_ap.put(position, mac_rssi);
        if(mac_rssi != null){position_apclone.put(position.toString(), mac_rssi);}
        Log.i("TEST","position: " + position.toString());
        Log.i("TEST","wifi ap: " + mac_rssi.toString());
        Log.i("TEST",position_ap.toString());
        System.out.println("\n");

        num_of_data++;
    }

    public void send_data_to_database(String DownloadURL, Context context){
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long number = snapshot.getChildrenCount()+1;
                database.child(Long.toString(number)).setValue(position_apclone);
                FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid()).child(Long.toString(number)).setValue(DownloadURL);
                Toast.makeText(context, "Mapping has been completed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });}

    /**
     * Based on receiving data(list of bssid), get appropriate data set from database*/

    /**
     *
     * @return dataset, a hashmap containing point x,y and a hashmap (BSSID : RSSI)
     */


        /*for(int i =0; i<num_of_data; i++){
            HashMap<String,Integer> ap_info = new HashMap<>();
            for(String j: bssid){

                // if position_ap contains this particular BSSID, j
                // if the inner hashmap BSSID: RSSI contains key bssid
                if(position_ap.get(position_list.get(i)).containsKey(j)){
                    // put this bssid: RSSI into ap_info
                    ap_info.put(j,(int)position_ap.get(position_list.get(i)).get(j));
                }
                else{
                    break;
                }
                if(ap_info.size()==bssid.size()){
                    // dataSet is a Hashmap containing
                    // position : ap_info (bssid : RSSI)
                    dataSet.put(position_list.get(i),ap_info);
                }
            }
        }*/

    public HashMap<Point, HashMap<String, Integer>> getPosition_ap() {
        return position_ap;
    }

    public List<String> getAp_list() {
        return ap_list;
    }
}
