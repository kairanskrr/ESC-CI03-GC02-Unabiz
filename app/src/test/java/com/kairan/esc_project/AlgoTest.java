package com.kairan.esc_project;

import com.kairan.esc_project.KairanTriangulationAlgo.Point;
import com.kairan.esc_project.KairanTriangulationAlgo.Testing;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AlgoTest {

    private Testing testing;
    private HashMap<String, Integer> map;
    private HashMap<Point, HashMap> hashMap;
    private Point point;

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void AlgoTest() {

        point = new Point(10, 10);
        map = new HashMap<>();
        map.put("1", 10);
        hashMap = new HashMap<>();
        hashMap.put(point, map);
//        private HashMap hashMap = HashMap<Point, HashMap<String, Integer>>
//        testing = new Testing();
    }
}
