package com.kairan.esc_project.KairanTriangulationAlgo;

import android.net.wifi.ScanResult;

import com.kairan.esc_project.KairanTriangulationAlgo.Mapping;
import com.kairan.esc_project.KairanTriangulationAlgo.NeuralNetwork;
import com.kairan.esc_project.KairanTriangulationAlgo.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 1. Receive wifi data about current location
 * 2. Get appropriate data set from database (wifi data and position)
 * 3. Predict position of current location (two methods: nn model, K nearest)
 * */
public class Testing {

    //double[] x;
    private HashMap<String,Integer> bssid_rssi;
    private List<String> bssid;
    private HashMap<Point,HashMap> position_ap;


    /**
     Initialize testing class with the scan result of the unknown position
     */
    public Testing(){
        this.position_ap = retrieve_data_from_database();
    }

    public void setScanResults(List<ScanResult> scanResults){
        //x = new double[Mapping.ap_list.size()];
        bssid_rssi = new HashMap<>();
        bssid = new ArrayList<>();
        // from the scanResults obtained, generate hashmap and List
        for(ScanResult ap: scanResults){
            if(20<Math.abs(ap.level)&&Math.abs(ap.level)<100){
                bssid_rssi.put(ap.BSSID, ap.level);
                bssid.add(ap.BSSID);
            }
        }
    }

    /**
     * Retrieve data set from database*/

    /**
     *
     * @param ?
     * @return dataset, a hashmap containing point x,y and a hashmap (BSSID : RSSI)
     */
    public HashMap<Point,HashMap> retrieve_data_from_database(){
        // TODO: retrieve data from data base about certain map
        return null;
    }


    /**
     * Based on receiving data, clean data set (i.e., only get those positions which have data of all the wifi aps that the unknown position can access), not sure if this is applicable*/

    /**
     *
     * @param
     * @return dataset, a hashmap containing point x,y and a hashmap (BSSID : RSSI)
     */
    public HashMap<Point,HashMap> clean_data(){

        List<Point> position_list = new ArrayList<>(position_ap.keySet());
        HashMap<Point,HashMap> dataSet = new HashMap<>();

        for(int i =0; i<position_ap.size(); i++){
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
     * 3. Make prediction of current positions based on the positions found with smallest and second smallest dev*/
    public Point predict(){

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
        int num_of_positions = position_ap.size();
        int num_of_bssids = bssid.size();

        float nearest1 = Float.MAX_VALUE;
        float nearest2 = Float.MAX_VALUE;

        Point nearest1_position = new Point(0,0);
        Point nearest2_position = new Point(0,0);

        int sum = 0;
        for(Point point: position_list){
            for(String j:bssid){
                if(position_ap.get(point).containsKey(j)){
                    sum += Math.pow((int)position_ap.get(point).get(j)- bssid_rssi.get(j),2);
                }
                else{
                    sum += Math.pow(bssid_rssi.get(j),2);
                }
            }
            float dev = (float) Math.sqrt(sum);
            if(dev<nearest1){
                if(nearest1<nearest2){
                    nearest2 = dev;
                    nearest2_position = point;
                }
                else{
                    nearest1 = dev;
                    nearest1_position = point;
                }

            } else if(dev<nearest2){
                nearest2 = dev;
                nearest2_position = point;
            }
            sum = 0;
        }

        double x = nearest1_position.getX()*nearest1/(nearest1+nearest2)+
                nearest2_position.getX()*nearest2/(nearest1+nearest2);
        double y = nearest1_position.getY()*nearest1/(nearest1+nearest2)+
                nearest2_position.getY()*nearest2/(nearest1+nearest2);

        return new Point(x,y);
    }
}
