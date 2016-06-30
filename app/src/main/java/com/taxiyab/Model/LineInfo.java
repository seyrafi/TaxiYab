package com.taxiyab.Model;

import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class LineInfo {
    public String src;
    public String dst;
    public int lineId;
    public int fare;
    public int color;
    public String description;
    public Marker srcMarker = null;
    public Marker dstMarker = null;

    public ArrayList<LatLng> points;

    public LineInfo(int lineId, String src, String dst, int fare, String description, int color){
        this.lineId = lineId;
        this.src = src;
        this.dst = dst;
        this.fare = fare;
        this.description = description;
        this.color = color;
        points = new ArrayList<>();
    }

    public void addPoint(LatLng point){
        points.add(point);
    }

    public void addPoint(double latitude, double longitude){
        points.add(new LatLng(latitude, longitude));
    }
}
