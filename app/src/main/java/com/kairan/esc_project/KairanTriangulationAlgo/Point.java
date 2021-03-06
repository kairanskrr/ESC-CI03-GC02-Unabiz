package com.kairan.esc_project.KairanTriangulationAlgo;

import androidx.annotation.NonNull;

/**
 * To store the position in the map in terms of (x,y)*/
public class Point {

    private double x;
    private double y;

    public Point(double x,double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @NonNull
    @Override
    public String toString() {
        return (int)x + "," + (int)y;
    }

}
