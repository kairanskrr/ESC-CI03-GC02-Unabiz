package com.kairan.esc_project.KairanTriangulationAlgo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Mapping2 {
    private HashMap<String,Integer> mac_rssi = new HashMap<>();
    List<String> ap_list = new ArrayList<>();
    public HashMap<Point,HashMap<String, Integer>> position_ap = new HashMap<>();
    Map<String, HashMap> position_apclone = new HashMap<>();
    List<Point> position_list;
    int num_of_data;
    private final float learning_rate = 0.2f;
    private final int ERROR = 10;

    static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    static DatabaseReference database = FirebaseDatabase.getInstance().getReference("ScanResults");
    static DatabaseReference MapUrls = FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid());
    static HashMap<Point, HashMap<String,Integer>> Map1;

    private int imageHeight;
    private int imageWidth;
    private final int denominator = 100;
    private int gridHeight;
    private int gridWidth;

    public static final List<String> B2L2 = Arrays.asList("");
    public static final List<String> CCL2 = Arrays.asList("");
    public static final List<String> CCL1_= Arrays.asList("");

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
        setGridHeight(imageHeight/denominator);
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
        setGridWidth(imageWidth/denominator);
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public List<String> getAp_list() {
        return ap_list;
    }

    public HashMap<Point, HashMap<String, Integer>> getPosition_ap() {
        return position_ap;
    }

    public HashMap<String,Integer> cleanData(List<ScanResult> scanResult){
        HashMap<String,Integer> mac_rssi = new HashMap<>();
        for (int i = 0; i < scanResult.size(); i++){
            String bssid = scanResult.get(i).BSSID;
            bssid = bssid.substring(0,bssid.length()-1);
            if(!ap_list.contains(bssid)){
                ap_list.add(bssid);
            }
            if(Math.abs(scanResult.get(i).level)<70){
                if(mac_rssi.keySet()!=null&&mac_rssi.keySet().contains(bssid)){
                    int level = (mac_rssi.get(bssid)+scanResult.get(i).level)/2;
                    mac_rssi.put(bssid,level);
                }
                else{
                    mac_rssi.put(bssid,scanResult.get(i).level);
                }
            }
        }
        return mac_rssi;
    }

    public void add_data(Point position, List<ScanResult> scanResult){
        mac_rssi = cleanData(scanResult);
        position_ap.put(position, mac_rssi);
        if(mac_rssi != null){position_apclone.put(position.toString(), mac_rssi);}
        num_of_data++;
    }

    public void add_data(Point position, List<ScanResult> scanResult,List<ScanResult> scanResult2){
        HashMap<String,Integer> mac_rssi1 = cleanData(scanResult);
        HashMap<String,Integer> mac_rssi2 = cleanData(scanResult2);
        for(String p:mac_rssi1.keySet()){
            mac_rssi.put(p,mac_rssi1.get(p));
        }
        for(String p:mac_rssi2.keySet()){
            if(mac_rssi.containsKey(p)){
                int temp = (mac_rssi.get(p)+mac_rssi2.get(p))/2;
                mac_rssi.put(p,temp);
            }else{
                mac_rssi.put(p,mac_rssi2.get(p));
            }
        }
        Log.i("TTTTT","mac_rssi: "+mac_rssi);
        position_ap.put(position, mac_rssi);
        if(mac_rssi != null){position_apclone.put(position.toString(), mac_rssi);}
        num_of_data++;
    }

    public void send_data(String DownloadURL, Context context){
        position_list = new ArrayList<>(position_ap.keySet());
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long number = snapshot.getChildrenCount()+1;
                database.child(user.getUid()).child(Long.toString(number)).setValue(position_apclone);
                FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid()).child(Long.toString(number)).setValue(DownloadURL);
                Toast.makeText(context, "Mapping has been completed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void train_data(int numOfRounds){
        for(int i=0;i<numOfRounds;i++){
            train_data();
        }
    }

    public void train_data(){
        int num = new Random().nextInt(num_of_data);
        position_list = new ArrayList<>(position_ap.keySet());
        Point q = position_list.get(num);
        HashMap<String,Integer> mr1 = position_ap.get(q);
        double min_pos_val = Double.MIN_VALUE;
        double min_sig_val = Double.MAX_VALUE;
        Point min_pos = new Point(0,0);
        Point min_sig = new Point(0,0);
        for(Point point: position_ap.keySet()){
            double diff_pos = Math.sqrt(Math.pow(point.getX()-q.getX(),2)+Math.pow(point.getY()-q.getY(),2));
            double diff_sig = 0;
            HashMap<String,Integer> mr2 = position_ap.get(point);
            for(String bs: mr1.keySet()){
                if(mr2.containsKey(bs)){
                    diff_sig += Math.pow(mr1.get(bs)-mr2.get(bs),2);
                }
                else{
                    diff_sig += Double.MAX_VALUE;
                }
            }
            diff_sig = Math.sqrt(diff_sig);
            if(diff_pos<min_pos_val){
                min_pos_val = diff_pos;
                min_pos = point;
            }
            if(diff_sig<min_sig_val){
                min_sig_val = diff_sig;
                min_sig = point;
            }
        }
        double xx = 0;
        double yy = 0;
        HashMap<String,Integer> mrr = new HashMap<>();
        if(min_sig.getX()-q.getX()>ERROR){
            xx = q.getX()+learning_rate*(min_sig.getX()-q.getX());
        }else if(q.getX()-min_sig.getX()>ERROR){
            xx = q.getX()-learning_rate*(q.getX()-min_sig.getX());
        }else{
            xx = q.getX();
        }
        if(min_sig.getY()-q.getY()>ERROR){
            yy = q.getY()+learning_rate*(min_sig.getY()-q.getY());
        }else if(q.getY()-min_sig.getY()>ERROR){
            yy = q.getY()-learning_rate*(q.getY()-min_sig.getY());
        }else{
            yy = q.getY();
        }

        HashMap<String,Integer> mr3 = position_ap.get(min_pos);
        for(String bss: mr1.keySet()){
            double level = 0;
            if(mr3.containsKey(bss)){
                if(mr3.get(bss)-mr1.get(bss)>0){
                    level = mr1.get(bss)+learning_rate*(mr3.get(bss)-mr1.get(bss));
                }else{
                    level = mr1.get(bss)-learning_rate*(mr1.get(bss)-mr3.get(bss));
                }
            }
            mr3.put(bss,(int)level);
        }

        position_ap.remove(q);
        position_ap.put(new Point(xx,yy),mrr);
    }


}
