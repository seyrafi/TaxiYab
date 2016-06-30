package com.taxiyab.Model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class DestStructureLine extends DestStructureBase{
    public int lineId;
    public String src;
    public String dest;
    public int fare;
    public LatLng srcLatLng;
    public LatLng dstLatLng;

    public DestStructureLine(int lineId, String src, String dest, int fare, LatLng srcLatLng, LatLng dstLatLng) {
        this.lineId = lineId;
        this.src = src;
        this.dest = dest;
        this.fare = fare;
        this.objectType = DestStructureType.LOCAL_LINES_DEST;
        this.srcLatLng = srcLatLng;
        this.dstLatLng = dstLatLng;
    }
}
