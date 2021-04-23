package com.kairan.esc_project.KairanTriangulationAlgo;

import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.nostra13.universalimageloader.utils.L.d;
import static com.nostra13.universalimageloader.utils.L.i;

public class Testing2 {
    private List<String> bssid = new ArrayList<>();
    private HashMap<String, Integer> mac_rssi = new HashMap<>();
    private HashMap<Point, HashMap<String, Integer>> position_ap;
    static List<String> ap_list;
    static ArrayList<Point> positionSet;
    private String DownloadURL;

    public static final int K = 2;
    private final double alpha = 0.7;

    static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    static DatabaseReference database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());
    static DatabaseReference MapUrls = FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid());
    static HashMap<Point, HashMap<String,Integer>> Map1;


    public Testing2(HashMap<Point, HashMap<String, Integer>> mappingData, List<String> ap) {
        this.position_ap = mappingData;
        this.ap_list = ap;
        //this.position_ap = Mapping2.train_data(10,mappingData);
        this.positionSet = new ArrayList<Point>(position_ap.keySet());
    }

    public Testing2(){

    }

    public void setPosition_ap(HashMap<Point, HashMap<String, Integer>> position_ap) {
        this.position_ap = position_ap;
    }

    public static void setAp_list(List<String> ap_list) {
        Testing2.ap_list = ap_list;
    }

    public static void setPositionSet(ArrayList<Point> positionSet) {
        Testing2.positionSet = positionSet;
    }

    public void setDownloadURL(String downloadURL) {
        DownloadURL = downloadURL;
    }

    public void setScanResult(List<ScanResult> scanResult) {
        if(scanResult==null){
            return;
        }
        for (int i = 0; i < scanResult.size(); i++) {
            String bssid = scanResult.get(i).BSSID;
            bssid = bssid.substring(0, bssid.length() - 1);
            if (!ap_list.contains(bssid)) {
                ap_list.add(bssid);
            }
            if (Math.abs(scanResult.get(i).level) < 70) {
                if (mac_rssi.keySet() != null && mac_rssi.keySet().contains(bssid)) {
                    int level = (mac_rssi.get(bssid) + scanResult.get(i).level) / 2;
                    mac_rssi.put(bssid, level);
                } else {
                    mac_rssi.put(bssid, scanResult.get(i).level);
                }
            }
        }
        bssid = new ArrayList<>(mac_rssi.keySet());
    }

    public HashMap<String,Integer> cleanScanResult(List<ScanResult> scanResult){
        if(scanResult==null){
            return null;
        }
        HashMap<String,Integer> mac_rssi2 = new HashMap<>();
        for (int i = 0; i < scanResult.size(); i++) {
            String bssid = scanResult.get(i).BSSID;
            bssid = bssid.substring(0, bssid.length() - 1);
            if (Math.abs(scanResult.get(i).level) < 70) {
                if (mac_rssi2.keySet() != null && mac_rssi2.keySet().contains(bssid)) {
                    int level = (mac_rssi2.get(bssid) + scanResult.get(i).level) / 2;
                    mac_rssi2.put(bssid, level);
                } else {
                    mac_rssi2.put(bssid, scanResult.get(i).level);
                }
            }
        }
        return mac_rssi2;
    }

    public void setScanResult(List<ScanResult> scanResult,List<ScanResult> scanResult2) {
        HashMap<String,Integer> mac_rssi1 = cleanScanResult(scanResult);
        HashMap<String,Integer> mac_rssi2 = cleanScanResult(scanResult2);
        for(String mac:mac_rssi1.keySet()){
            mac_rssi.put(mac,mac_rssi1.get(mac));
        }
        for(String mac2:mac_rssi2.keySet()){
            if(mac_rssi.containsKey(mac2)){
                int temp = (mac_rssi.get(mac2)+mac_rssi2.get(mac2))/2;
                mac_rssi.put(mac2,temp);
            }else{
                mac_rssi.put(mac2,mac_rssi2.get(mac2));
            }
        }
        bssid = new ArrayList<>(mac_rssi.keySet());
    }

    public boolean isEmpty() {
        return mac_rssi.isEmpty();

    }

    public void add_scanList(List<ScanResult> scanResult){
        if(mac_rssi.isEmpty()){
            setScanResult(scanResult);
        }else{
            HashMap<String,Integer> mac_rssi1 = cleanScanResult(scanResult);
            HashMap<String,Integer> mac_rssi2 = mac_rssi;
            for(String p:mac_rssi1.keySet()){
                mac_rssi.put(p,mac_rssi1.get(p));
            }
            for(String mac:mac_rssi1.keySet()){
                mac_rssi.put(mac,mac_rssi1.get(mac));
            }
            for(String mac2:mac_rssi2.keySet()){
                if(mac_rssi.containsKey(mac2)){
                    int temp = (mac_rssi.get(mac2)+mac_rssi2.get(mac2))/2;
                    mac_rssi.put(mac2,temp);
                }else{
                    mac_rssi.put(mac2,mac_rssi2.get(mac2));
                }
            }
            bssid = new ArrayList<>(mac_rssi.keySet());
        }
    }


    /**
     * Single Weighted Euclidean Distance-Based WKNN Algorithm:
     * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6567165/
     */
    public Point predict() {

        if (position_ap.isEmpty()||mac_rssi.isEmpty()) {
            return null;
        }

        Map<Point, Float> distance_point = new HashMap<Point,Float>();
        Map<Point, Float> similarity_point = new HashMap<Point,Float>();
        ArrayList<Point> position_list = new ArrayList<Point>(position_ap.keySet());
        Log.i("TTTTT",position_ap.toString());

        for (Point point : position_list) {
            System.out.println("point: " + point);
            System.out.println("length: " + position_ap.get(point).size());

            HashMap<String, Integer> x;
            int sum = 0;
            float similarity = 0;
            float x_length = 0;
            float y_length = 0;

            if (position_ap.get(point).size() > bssid.size()) {
                x = position_ap.get(point);
            } else {
                x = mac_rssi;
            }

            for (String j : x.keySet()) {
                if (position_ap.get(point).containsKey(j) && mac_rssi.containsKey(j)) {
                    sum += Math.pow((int) position_ap.get(point).get(j) - mac_rssi.get(j), 2);
                    similarity += position_ap.get(point).get(j) * mac_rssi.get(j) * 1f;
                    x_length += Math.pow(position_ap.get(point).get(j),2);
                    y_length += Math.pow(position_ap.get(point).get(j),2);
                    //Log.i("TTTTT","sum1: " + sum);
                } else {
                    sum += 1000; // abs(rssi) is smaller, the signal is stronger => put a large number to indicate the ap is not detected in both positions
                    //Log.i("TTTTT","sum2: " + sum);
                }
            }

            //Log.i("TTTTT","sum: " + sum);
            float dev = (float) Math.sqrt(sum);
            Log.i("TTTTT","dev: "+dev);
            distance_point.put(point, dev);

            x_length = (float) Math.sqrt(x_length);
            y_length = (float) Math.sqrt(y_length);
            similarity = similarity / (x_length * y_length);
            Log.i("TTTTT","x_length: "+x_length);
            Log.i("TTTTT","y_length: "+y_length);
            System.out.println("similarity: " + similarity);
            similarity_point.put(point, similarity);
        }

        distance_point = sortByComparator(distance_point, true);
        similarity_point = sortByComparator(similarity_point, true);
        Point DP = getDP(distance_point);
        Point SP = getSP(similarity_point);

        Log.i("TTTTT","DP: "+DP.toString());
        Log.i("TTTTT","SP: "+SP.toString());

        return new Point(DP.getX() * alpha + SP.getX() * (1 - alpha), DP.getY() * alpha + SP.getY() * (1 - alpha));

    }

    public Point getDP(Map<Point, Float> input) {
        int count = 0;
        double x = 0;
        double y = 0;
        float[] values = new float[K];
        ArrayList<Point> points = new ArrayList<>(input.keySet());
        for(int i = 0; i< K;i++){
            Point point = points.get(i);
            if (input.get(point) <= 0.2) {
                x += point.getX();
                y += point.getY();
                count += 1;
            } else if (count != 0) {
                x = x / count;
                y = y / count;
                break;
            } else {
                x += point.getX()/K;
                y += point.getY()/K;
                values[i] = 1/input.get(point);

            }
            Log.i("TTTTT","i: "+i);
            Log.i("TTTTT","x: "+point.getX());
            Log.i("TTTTT","y: "+point.getY());
            Log.i("TTTTT","distance: "+input.get(point));
        }

        float v = 0;
        double xx = 0;
        double yy = 0;
        if(count==0){
            for(int i=0;i < K; i++){
                v += values[i];
            }
            Log.i("TTTTT","vvvv: "+v);
            for(int ii = 0;ii<K;ii++){
                xx += points.get(ii).getX()*(values[ii]/v);
                yy += points.get(ii).getY()*(values[ii]/v);
                Log.i("TTTTT","x2: "+xx);
                Log.i("TTTTT","y2: "+yy);
                Log.i("TTTTT","VALUE: "+values[ii]);
            }
            x = xx;
            y = yy;
        }

        return new Point(x, y);
    }

    public Point getSP(Map<Point, Float> input) {
        int count = 0;
        double x = 0;
        double y = 0;
        float[] values = new float[K];
        ArrayList<Point> points = new ArrayList<>(input.keySet());
        for(int i = 0; i< K;i++){
            Point point = points.get(points.size()-i-1);
            if (input.get(point) >Float.MAX_VALUE) {
                x += point.getX();
                y += point.getY();
                count += 1;
            } else if (count != 0) {
                x = x / count;
                y = y / count;
                break;
            } else {
                x += point.getX()/K;
                y += point.getY()/K;
                values[i] = input.get(point);
            }
            Log.i("TTTTT","i: "+i);
            Log.i("TTTTT","x: "+point.getX());
            Log.i("TTTTT","y: "+point.getY());
            Log.i("TTTTT","similarity: "+input.get(point));
        }

        float v = 0;
        double xx = 0;
        double yy = 0;
        if(count==0){
            for(int i=0;i < K; i++){
                v += values[i];
            }
            Log.i("TTTTT","vvvv: "+v);
            for(int ii = 0;ii<K;ii++){
                xx += points.get(ii).getX()*(values[ii]/v);
                yy += points.get(ii).getY()*(values[ii]/v);
                Log.i("TTTTT","VALUE: "+values[ii]);
                Log.i("TTTTT","x2: "+xx);
                Log.i("TTTTT","y2: "+yy);
            }
            Log.i("TTTTT","x2: "+xx);
            Log.i("TTTTT","y2: "+yy);
            x = xx;
            y = yy;
        }

        return new Point(x, y);
    }


    public List<String> getBssid() {
        return bssid;
    }

    public HashMap<Point, HashMap<String, Integer>> getPosition_ap() {
        return position_ap;
    }

    private static Map<Point, Float> sortByComparator(Map<Point, Float> unsortMap, final boolean order) {

        List<Map.Entry<Point, Float>> list = new LinkedList<Map.Entry<Point, Float>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Point, Float>>() {
            public int compare(Map.Entry<Point, Float> o1,
                               Map.Entry<Point, Float> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Point, Float> sortedMap = new LinkedHashMap<Point, Float>();
        for (Map.Entry<Point, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
