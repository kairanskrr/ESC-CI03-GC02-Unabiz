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
    private static HashMap<String, Integer> bssid_rssi;
    private static List<String> bssid;
    private static HashMap<Point, HashMap<String, Integer>> position_ap;
    static NeuralNetwork nn;
    static List<String> ap_list;
    static ArrayList<Point> positionSet;


    static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    static DatabaseReference database = FirebaseDatabase.getInstance().getReference("ScanResults").child(user.getUid());
    static DatabaseReference MapUrls = FirebaseDatabase.getInstance().getReference("MapURLs").child(user.getUid());
    static HashMap<Point, HashMap<String,Integer>> Map1;
    //static Point prediction;

    public Testing(String URLlink){
        //this.position_ap = Mapping.get_data_for_testing(URLlink);
    }

    public Testing(HashMap<Point, HashMap<String, Integer>> mappingData) {
        this.position_ap = mappingData;
    }

    public Testing(HashMap<Point, HashMap<String, Integer>> mappingData,List<String> ap) {
        this.position_ap = mappingData;
        this.ap_list = ap;
        this.positionSet = new ArrayList<Point>(position_ap.keySet());
        this.nn = train_data();
    }

    public static void setMap1(HashMap<Point, HashMap<String, Integer>> map1) {
        Map1 = map1;
    }

    /**
     * Initialize testing class with the scan result of the unknown position
     */
    public static Point get_data_for_testing(String URLlink){
        //retrieve data from database
        HashMap<Point,HashMap<String, Integer>> dataSet = new HashMap<>();
        final Point[] prediction = new Point[1];
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
                                prediction[0] = predict(Map1);
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
        });
        return prediction[0];
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
    public static Point predict(HashMap<Point, HashMap<String, Integer>> position_ap) {

        if(position_ap.isEmpty()){
            return null;
        }

        ArrayList<Point> position_list = new ArrayList<Point>(position_ap.keySet());

        float nearest1 = Float.MAX_VALUE;
        float nearest2 = Float.MAX_VALUE;
        float nearest3 = Float.MAX_VALUE;

        Point nearest1_position = new Point(0, 0);
        Point nearest2_position = new Point(0, 0);
        Point nearest3_position = new Point(0,0);

        int sum = 0;
        float min_similarity = Float.MAX_VALUE;
        Point point_with_min_sim = new Point(0,0);
        for (Point point : position_list) {
            System.out.println("point: "+point);
            System.out.println("length: "+position_ap.get(point).size());

            HashMap<String,Integer> x;
            float similarity = 0;
            float x_length = 0;
            float y_length = 0;

            if(position_ap.get(point).size()>bssid.size()){
                x = position_ap.get(point);
            }
            else{
                x = bssid_rssi;
            }

            for (String j : x.keySet()) {
                if (position_ap.get(point).containsKey(j)&&bssid_rssi.containsKey(j)) {
                    sum += Math.pow((int) position_ap.get(point).get(j) - bssid_rssi.get(j), 2);
//                    System.out.println("position ap: "+position_ap.get(point).get(j));
//                    System.out.println("bssid: "+bssid_rssi.get(j));
                    similarity += (int)position_ap.get(point).get(j) * bssid_rssi.get(j)*1f;

                } else {
                    //sum += Math.pow(bssid_rssi.get(j), 2);
                    sum += Integer.MAX_VALUE; // abs(rssi) is smaller, the signal is stronger => put a large number to indicate the ap is not detected in both positions
                    similarity += Float.MAX_VALUE;

                }
            }
            System.out.println("sum: "+sum);

            float dev = (float) Math.sqrt(sum);

            x_length = (float)Math.sqrt(x_length);
            y_length = (float)Math.sqrt(y_length);

            similarity = similarity/(x_length*y_length);
            if(min_similarity>similarity){
                min_similarity = similarity;
                point_with_min_sim = point;
            }

            System.out.println("similarity: "+min_similarity);

            // Nearest 1<2<3
            float temp;
            Point temp_point;
            Point dev_point = point;
            if (dev < nearest1) {
                temp = nearest1;
                temp_point = nearest1_position;
                nearest1 = dev;
                nearest1_position = dev_point;
                dev = temp;
                dev_point = temp_point;
            }
            if(dev < nearest2){
                temp = nearest2;
                temp_point = nearest2_position;
                nearest2 = dev;
                nearest2_position = dev_point;
                dev = temp;
                dev_point = temp_point;
            }
            if(dev < nearest3){
                nearest3 = dev;
                nearest3_position = dev_point;
            }
            sum = 0;
        }

        Log.i("TTTTT","nearest 1: "+ nearest1_position + "\tsum: "+nearest1);
        Log.i("TTTTT","nearest 2: "+ nearest2_position + "\tsum: "+nearest2);
        Log.i("TTTTT","nearest 3: "+ nearest3_position + "\tsum: "+nearest3);

        double x, y;
        if (nearest1 == 0 && nearest2 == 0 && nearest3 ==0) {
            x = (nearest1_position.getX() + nearest2_position.getX()+nearest3_position.getX())/3;
            y = (nearest1_position.getY() + nearest2_position.getY()+nearest3_position.getY())/3 ;
        }
        else if(nearest1 == 0 && nearest2 == 0){
            x = (nearest1_position.getX() + nearest2_position.getX())/2;
            y = (nearest1_position.getY() + nearest2_position.getY())/2 ;
        }
        else if(nearest1 == 0){
            x = nearest1_position.getX();
            y = nearest1_position.getY();
        }
        else {
            float v = 1 / nearest1 + 1 / nearest2 + 1 / nearest3;
            x = nearest1_position.getX() * (1/nearest1) / v +
                    nearest2_position.getX() * (1/nearest2) / v +
                    nearest3_position.getX() * (1/nearest3) / v;
            y = nearest1_position.getY() * (1/nearest1) / v +
                    nearest2_position.getY() * (1/nearest2) / v +
                    nearest3_position.getY() * (1/nearest3) / v;
        }
        return new Point(((int)x+point_with_min_sim.getX())/2, ((int)y+point_with_min_sim.getY())/2);
    }

    public Point predict() {

        if(position_ap.isEmpty()){
            return null;
        }

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

    public Point predict_thread(){

        if(position_ap.isEmpty()){
            return null;
        }
        // use thread
        ArrayList<Point> position_list = new ArrayList<Point>(position_ap.keySet());
        int num_of_positions = position_ap.size();

        int num_of_threads = 3;
        int length_of_sublist = num_of_positions/num_of_threads;
        CalculationThread[] threads = new CalculationThread[num_of_threads];
        Lock lock = new ReentrantLock();
        for(int i = 0; i< num_of_threads-1;i++){
            threads[i] = new CalculationThread(lock,position_list.subList(i*length_of_sublist,(i+1)*length_of_sublist),bssid_rssi,position_ap,bssid);
        }
        threads[num_of_threads-1] = new CalculationThread(lock,position_list.subList((num_of_threads-1)*length_of_sublist,num_of_positions),bssid_rssi,position_ap,bssid);

        for(int i = 0; i < num_of_threads; i++){
            threads[i].start();
        }

        try {
            for (int i = 0; i < num_of_threads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("THREAD ERROR");
        }

        float nearest1 = threads[0].getNearest1();
        float nearest2 = threads[0].getNearest2();

        Point nearest1_position = threads[0].getNearest1_position();
        Point nearest2_position = threads[0].getNearest2_position();

        double x = nearest1_position.getX()*nearest1/(nearest1+nearest2)+
                nearest2_position.getX()*nearest2/(nearest1+nearest2);
        double y = nearest1_position.getY()*nearest1/(nearest1+nearest2)+
                nearest2_position.getY()*nearest2/(nearest1+nearest2);

        return new Point(x,y);
    }

    /*****************************************************
     * Position (Point) * bssid (String) * rssi (Integer)*
     * ***************************************************
     *  (0,0)           * XX:XX:XX:XX:XX * -XX           *
     *  (0,0)           * XX:XX:XX:XX:XX * -XX           *
     *  (0,0)           * XX:XX:XX:XX:XX * -XX           *
     *  (0,5)           * XX:XX:XX:XX:XX * -XX           *
     *  (0,5)           * XX:XX:XX:XX:XX * -XX           *
     *  (0,5)           * XX:XX:XX:XX:XX * -XX           *
     *  ......          * ......         * ......        *
     *****************************************************/

    /**
     * Used in Neural Network model, train model using the data set that is obtained in get_data*/
    public static NeuralNetwork train_data(){

        //HashMap<Point,HashMap<String,Integer>> dataSet = position_ap;
        //List<String> ap_list = Mapping.getAp_list();
        //ArrayList<Point> positionSet = new ArrayList<Point>(position_ap.keySet());
        int num_of_positions = position_ap.size();
        int num_of_aps = ap_list.size();
        nn = new NeuralNetwork(num_of_aps, num_of_positions,num_of_positions);
        double[][] x = new double[num_of_positions][num_of_aps];

        for(int i=0;i<num_of_positions;i++){
            for(int j = 0; j< num_of_aps; j++){
                // position.set(i) => Specific position which index is i in the positionSet
                // bssid.get(j) => Specific bssid which index is j in the list of bssid
                // dataSet.get(position.set(j)) => HashMap of (bssid,rssi) at that position
                // dataSet.get(position.set(j)).get(bssid.get(i)) => rssi value at position i, bssid j
                /************************************************************************************** (width: num of aps)
                 * bssid (String)   * X1:X1:X1:X1:X1 * X2:X2:X2:X2:X2 * X3:X3:X3:X3:X3 * ......       *
                 * position (Point) *                                                                 *
                 * ************************************************************************************
                 *  (0,0)           * -XX            * -XX            * -XX            * ......       *
                 *  (0,5)           * -XX            * -XX            * -XX            * ......       *
                 *  (0,10)          * -XX            * -XX            * -XX            * ......       *
                 *  (5,0)           * -XX            * -XX            * -XX            * ......       *
                 *  (10,0)          * -XX            * -XX            * -XX            * ......       *
                 *  (5,5)           * -XX            * -XX            * -XX            * ......       *
                 *  ......          * ......         * ......         * ......         * ......       *
                 **************************************************************************************
                 (length : num of positions) */
                if(position_ap.get(positionSet.get(i)).containsKey(ap_list.get(j))){
                    x[i][j] = (double)position_ap.get(positionSet.get(i)).get(ap_list.get(j));
                }
                else{
                    x[i][j] = -Double.MAX_VALUE;
                }
            }
        }

        double[][] y = new double[num_of_positions][2];
        for(int i = 0; i< num_of_positions; i++){
            /*************
             * x   * y   *
             * 0   * 0   *
             * 0   * 5   *
             * 0   * 10  *
             * 5   * 0   *
             * ... * ... *
             *************
             (length: num of positions)
             * dimension: (num of positions)*2 */
            y[i][0]= positionSet.get(i).getX();
            y[i][1]=positionSet.get(i).getY();
        }

        nn.fit(y,x,50000);   // train nn model
        return nn;
    }

    public static Point predict_nn(){
        double[] x = new double[ap_list.size()];
        for(int i = 0;i<ap_list.size();i++){
            if(bssid_rssi.containsKey(ap_list.get(i))){
                x[i] = (double)bssid_rssi.get(ap_list.get(i));
            }
            else{
                x[i] = -Double.MAX_VALUE;
            }
        }
        List<Double> output = nn.predict(x);
        return new Point(output.get(0),output.get(1));
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