package com.kairan.esc_project;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Mapping {

    private static HashMap<String,Integer> mac_rssi;
    private static HashMap<Point,HashMap> position_ap;
    static List<String> ap_list;
    static List<Point> position_list;
    static int num_of_data;

    static NeuralNetwork nn;


    public Mapping(){
        position_ap = new HashMap<>();
        position_list = new ArrayList<>();
        ap_list = new ArrayList<>();
        num_of_data = 0;
    }

    /**
     * Collect data from wifi scan results (bssid, rssi) and map it with its position on the floor map (x,y)*/

    public static void add_data(Point position, List<ScanResult> scanResult){

        mac_rssi = new HashMap<>();
        for(ScanResult ap:scanResult){
            if(20<Math.abs(ap.level)&&Math.abs(ap.level)<100){
                mac_rssi.put(ap.BSSID,ap.level);
            }
            if(!ap_list.contains(ap.BSSID)){
                ap_list.add(ap.BSSID);
            }
        }
        position_ap.put(position, mac_rssi);

        num_of_data++;
    }

    /**
     * Based on receiving data(list of bssid), get appropriate data set from database*/
    public static HashMap<Point,HashMap> get_data(List<String> bssid){
        HashMap<Point,HashMap> dataSet = new HashMap<>();

        for(int i =0; i<num_of_data; i++){
            HashMap<String,Integer> ap_info = new HashMap<>();
            for(String j: bssid){
                if(position_ap.get(position_list.get(i)).containsKey(j)){
                    ap_info.put(j,(int)position_ap.get(position_list.get(i)).get(j));
                }
                else{
                    break;
                }
                if(ap_info.size()==bssid.size()){
                    dataSet.put(position_list.get(i),ap_info);
                }
            }
        }
        return dataSet;
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
    public static NeuralNetwork train_data(List<String> bssid){

        HashMap<Point,HashMap> dataSet = get_data(bssid);
        ArrayList<Point> positionSet = new ArrayList<Point>(dataSet.keySet());
        int num_of_positions = dataSet.size();
        int num_of_bssids = bssid.size();
        nn = new NeuralNetwork(num_of_bssids, num_of_positions,2);
        double[][] x = new double[num_of_positions][num_of_bssids];

        for(int i=0;i<num_of_positions;i++){
            for(int j = 0; j< num_of_bssids; j++){
                // position.set(i) => Specific position which index is i in the positionSet
                // bssid.get(j) => Specific bssid which index is j in the list of bssid
                // dataSet.get(position.set(j)) => HashMap of (bssid,rssi) at that position
                // dataSet.get(position.set(j)).get(bssid.get(i)) => rssi value at position i, bssid j
                /************************************************************************************** (width: num of bssids)
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
                x[i][j]= (double)dataSet.get(positionSet.get(i)).get(bssid.get(j));
            }
        }

        double[][] y = new double[2][num_of_positions];
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
            y[0][i]= positionSet.get(i).getX();
            y[1][i]=positionSet.get(i).getY();
        }

        nn.fit(y,x,50000);   // train nn model
        return nn;
    }
}
