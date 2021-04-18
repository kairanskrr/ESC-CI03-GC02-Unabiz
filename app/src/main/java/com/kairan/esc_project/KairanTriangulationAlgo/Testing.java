package com.kairan.esc_project.KairanTriangulationAlgo;

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
import com.kairan.esc_project.KairanTriangulationAlgo.Mapping;
import com.kairan.esc_project.KairanTriangulationAlgo.NeuralNetwork;
import com.kairan.esc_project.KairanTriangulationAlgo.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 1. Retrieve data from database about the selected map once testing starts and ave in local?
 * 2. Get wifi scan result from testing activity
 * 3. Predict position of current location (K nearest)
 * */
public class Testing {

    //double[] x;
    private HashMap<String, Integer> bssid_rssi;
    private List<String> bssid;
    private HashMap<Point, HashMap<String, Integer>> position_ap;


    static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    static DatabaseReference database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());
    static DatabaseReference MapUrls = FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid());
    static HashMap<Point, HashMap<String,Integer>> Map1;


    /**
     * Initialize testing class with the scan result of the unknown position
     */
    public static void get_data_for_testing(String URLlink){
        //retrieve data from database
        HashMap<Point,HashMap<String, Integer>> dataSet = new HashMap<>();
        MapUrls.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    if (snapshot1.getValue().toString().equals(URLlink) ){
                        database.child(Objects.requireNonNull(snapshot1.getKey())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Map> map = (Map<String, Map>) snapshot.getValue();
                                for (String key: map.keySet()){
                                    HashMap<String, Integer> rssivalues = new HashMap<>();
                                    String[] separated = key.split(",");
                                    Point p = new Point(Double.parseDouble(separated[0]),Double.parseDouble(separated[1]));
//                                            Log.i("Test",p.toString());
//                                            Log.i("Test", separated[0]);
//                                            Log.i("Test", separated[1]);
                                    for(Object key1: map.get(key).keySet()){
                                        rssivalues.put(key1.toString(), Integer.valueOf(map.get(key).get(key1).toString()));
                                    }
                                    dataSet.put(p,rssivalues);
                                }
                                Map1 = dataSet;
                                for(Point p : Map1.keySet()){
                                    Log.i("Test",p.toString());
                                    for (String e : Map1.get(p).keySet()){
                                        Log.i("Test", e);
                                        Log.i("Test",Map1.get(p).get(e).toString());
                                    }}

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });}

    public Testing(String URLlink){
        //this.position_ap = Mapping.get_data_for_testing(URLlink);
    }

    public Testing(HashMap<Point, HashMap<String, Integer>> mappingData) {
        this.position_ap = mappingData;
    }

    public void setScanResults(List<ScanResult> scanResults) {
        //x = new double[Mapping.ap_list.size()];
        bssid_rssi = new HashMap<>();
        bssid = new ArrayList<>();
        // from the scanResults obtained, generate hashmap and List
        for (ScanResult ap : scanResults) {
            if (20 < Math.abs(ap.level) && Math.abs(ap.level) < 100) {
                bssid_rssi.put(ap.BSSID, ap.level);
                bssid.add(ap.BSSID);
            }
        }
    }

    public boolean isEmpty() {
        return position_ap.isEmpty();
    }


    /**
     * Based on receiving data, clean data set (i.e., only get those positions which have data of all the wifi aps that the unknown position can access), not sure if this is applicable*/

    /**
     * @param
     * @return dataset, a hashmap containing point x,y and a hashmap (BSSID : RSSI)
     */
    public HashMap<Point, HashMap> clean_data() {

        List<Point> position_list = new ArrayList<>(position_ap.keySet());
        HashMap<Point, HashMap> dataSet = new HashMap<>();

        for (int i = 0; i < position_ap.size(); i++) {
            HashMap<String, Integer> ap_info = new HashMap<>();
            for (String j : bssid) {
                // if position_ap contains this particular BSSID, j
                // if the inner hashmap BSSID: RSSI contains key bssid
                if (position_ap.get(position_list.get(i)).containsKey(j)) {
                    // put this bssid: RSSI into ap_info
                    ap_info.put(j, (int) position_ap.get(position_list.get(i)).get(j));
                } else {
                    break;
                }
                if (ap_info.size() == bssid.size()) {
                    // dataSet is a Hashmap containing
                    // position : ap_info (bssid : RSSI)
                    dataSet.put(position_list.get(i), ap_info);
                }
            }
        }
        return dataSet;

    }

    /*public Point getPrediction(){
        // train everytime we call getPrediction()?
        NeuralNetwork nn = Mapping.train_data(bssid);
        List<Double> output = nn.predict(x);
        // return user location
        return new Point(output.get(0),output.get(1));
    }*/

    /**
     * K nearest method:
     * 1. Calculate dev = sqrt((rss1-rss1')^2+(rss2-rss2')^2+......) for every position in the data set
     * 2. Compare dev, find the smallest and the second smallest and their positions
     * 3. Make prediction of current positions based on the positions found with smallest and second smallest dev
     */
    public Point predict() {

        /*// Assume using clean_data method, then no need to deal with the situation when some position does not have data for certain wifi aps
        HashMap<Point,HashMap> dataSet = clean_data();
        ArrayList<Point> positionSet = new ArrayList<Point>(dataSet.keySet());
        int num_of_positions = dataSet.size();
        int num_of_bssids = bssid.size();

        float nearest1 = Float.MAX_VALUE;
        float nearest2 = Float.MAX_VALUE;

        Point nearest1_position = new Point(0,0);
        Point nearest2_position = new Point(0,0);

        int sum = 0;
        if(dataSet.isEmpty()){
            return new Point(-1,-1);
        }
        else{
            for(int i=0;i<num_of_positions;i++){
                for(int j = 0; j< num_of_bssids; j++){
                    sum += Math.pow((int)dataSet.get(positionSet.get(i)).get(bssid.get(i))- bssid_rssi.get(bssid.get(j)),2);
                }
                float dev = (float) Math.sqrt(sum);
                if(dev<nearest1){
                    if(nearest1<nearest2){
                        nearest2 = dev;
                        nearest2_position = positionSet.get(i);
                    }
                    else{
                        nearest1 = dev;
                        nearest1_position = positionSet.get(i);
                    }

                } else if(dev<nearest2){
                    nearest2 = dev;
                    nearest2_position = positionSet.get(i);
                }
                sum = 0;
            }
        }*/

        // do not use clean_data method
        ArrayList<Point> position_list = new ArrayList<Point>(position_ap.keySet());

        float nearest1 = Float.MAX_VALUE;
        float nearest2 = Float.MAX_VALUE;

        Point nearest1_position = new Point(0, 0);
        Point nearest2_position = new Point(0, 0);

        int sum = 0;
        for (Point point : position_list) {
            for (String j : bssid) {
                if (position_ap.get(point).containsKey(j)) {
                    sum += Math.pow((int) position_ap.get(point).get(j) - bssid_rssi.get(j), 2);
                } else {
                    sum += Math.pow(bssid_rssi.get(j), 2);
                }
            }
            float dev = (float) Math.sqrt(sum);
            if (dev < nearest1) {
                if (nearest1 < nearest2) {
                    nearest2 = dev;
                    nearest2_position = point;
                } else {
                    nearest1 = dev;
                    nearest1_position = point;
                }

            } else if (dev < nearest2) {
                nearest2 = dev;
                nearest2_position = point;
            }
            sum = 0;
        }

        double x, y;

        if (nearest1 == 0 && nearest2 == 0) {
            x = (nearest1_position.getX() + nearest2_position.getX()) * 0.5;
            y = (nearest1_position.getY() + nearest2_position.getY()) * 0.5;
        } else {
            x = nearest1_position.getX() * nearest2 / (nearest1 + nearest2) +
                    nearest2_position.getX() * nearest1 / (nearest1 + nearest2);
            y = nearest1_position.getY() * nearest2 / (nearest1 + nearest2) +
                    nearest2_position.getY() * nearest1 / (nearest1 + nearest2);
        }

        return new Point(x, y);

//        // use thread
//        ArrayList<Point> position_list = new ArrayList<Point>(position_ap.keySet());
//        int num_of_positions = position_ap.size();
//
//        int num_of_threads = 3;
//        int length_of_sublist = num_of_positions/num_of_threads;
//        CalculationThread[] threads = new CalculationThread[num_of_threads];
//        Lock lock = new ReentrantLock();
//        for(int i = 0; i< num_of_threads-1;i++){
//            threads[i] = new CalculationThread(lock,position_list.subList(i*length_of_sublist,(i+1)*length_of_sublist));
//        }
//        threads[num_of_threads-1] = new CalculationThread(lock,position_list.subList((num_of_threads-1)*length_of_sublist,num_of_positions));
//
//        for(int i = 0; i < num_of_threads; i++){
//            threads[i].start();
//        }
//
//        try {
//            for (int i = 0; i < num_of_threads; i++) {
//                threads[i].join();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            System.out.println("THREAD ERROR");
//        }
//
//        float nearest1 = threads[0].getNearest1();
//        float nearest2 = threads[0].getNearest2();
//
//        Point nearest1_position = threads[0].getNearest1_position();
//        Point nearest2_position = threads[0].getNearest2_position();
//
//        double x = nearest1_position.getX()*nearest1/(nearest1+nearest2)+
//                nearest2_position.getX()*nearest2/(nearest1+nearest2);
//        double y = nearest1_position.getY()*nearest1/(nearest1+nearest2)+
//                nearest2_position.getY()*nearest2/(nearest1+nearest2);
//
//        return new Point(x,y);

    }

    public HashMap<String, Integer> getBssid_rssi() {
        return bssid_rssi;
    }

    public List<String> getBssid() {
        return bssid;
    }

    public HashMap<Point, HashMap<String, Integer>> getPosition_ap() {
        return position_ap;
    }


    class CalculationThread extends Thread {
        private HashMap<String, Integer> bssid_rssi;
        private List<Point> position_list;
        private List<String> bssid;
        private HashMap<Point, HashMap<String, Integer>> position_ap;

        private volatile float nearest1 = Float.MAX_VALUE;
        private volatile float nearest2 = Float.MAX_VALUE;

        private volatile Point nearest1_position = new Point(0, 0);
        private volatile Point nearest2_position = new Point(0, 0);

        private Lock lock;

        CalculationThread(Lock lock, List<Point> position_list, HashMap<String, Integer> bssid_rssi, HashMap<Point, HashMap<String, Integer>> position_ap, List<String> bssid) {
            this.lock = lock;
            this.position_list = position_list;
            this.bssid_rssi = bssid_rssi;
            this.position_ap = position_ap;
            this.bssid = bssid;
        }

        public void run() {
            int sum = 0;
            for (Point point : position_list) {
                for (String j : bssid) {
                    if (position_ap.get(point).containsKey(j)) {
                        sum += Math.pow((int) position_ap.get(point).get(j) - bssid_rssi.get(j), 2);
                    } else {
                        sum += Math.pow(bssid_rssi.get(j), 2);
                    }
                }
                float dev = (float) Math.sqrt(sum);
                lock.lock();
                if (dev < nearest1) {
                    if (nearest1 < nearest2) {
                        nearest2 = dev;
                        nearest2_position = point;
                    } else {
                        nearest1 = dev;
                        nearest1_position = point;
                    }

                } else if (dev < nearest2) {
                    nearest2 = dev;
                    nearest2_position = point;
                }
                lock.unlock();
                sum = 0;
            }
        }

        public Point getNearest1_position() {
            Point temp;
            lock.lock();
            temp = nearest1_position;
            lock.unlock();
            return temp;
        }

        public Point getNearest2_position() {
            Point temp;
            lock.lock();
            temp = nearest2_position;
            lock.unlock();
            return temp;
        }

        public float getNearest1() {
            float temp;
            lock.lock();
            temp = nearest1;
            lock.unlock();
            return temp;
        }

        public float getNearest2() {
            float temp;
            lock.lock();
            temp = nearest2;
            lock.unlock();
            return temp;
        }

    }
}