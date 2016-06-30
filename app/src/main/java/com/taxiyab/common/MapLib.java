package com.taxiyab.common;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.taxiyab.Model.LineInfo;
import com.taxiyab.R;
import com.taxiyab.Service.MyAsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by MehrdadS on 6/26/2016.
 */
public class MapLib {

    public interface ReverseGeocodingHandler {
        public void handle(Address addressInfo);
    }

    private static BitmapDescriptor icon = null;
    private static List<LineInfo> currentLinesInfo = null;
    private static int currentBoldedLineId = -1;

    public static Address MapReverseGeocodingSync(Context context, LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        try {
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() == 0)
                return null;
            return  addresses.get(0);
        } catch (IOException ex) {
            return null;
        }
    }

    public static void MapReverseGeocodingAsync(final Context context, final LatLng latLng, final ReverseGeocodingHandler reverseGeocodingHandler){
        MyAsyncTask.DoJob(context, new MyAsyncTask.MyAsyncTaskJob() {
            @Override
            public MyAsyncTask.MyAsyncTaskResultBase perform(MyAsyncTask.MyAsyncTaskParameterBase parameter) {
                // Find the address to show the user
                Address address = MapReverseGeocodingSync(context, latLng);
                if (address != null)
                    return new MyAsyncTask.MyAsyncTaskResultBase(address);
                return new MyAsyncTask.MyAsyncTaskResultBase("");
            }
        }, new MyAsyncTask.MyAsyncTaskPostJob() {
            @Override
            public void perform(MyAsyncTask.MyAsyncTaskResultBase result) {
                if (result != null && result.obj != null) {
                    if (reverseGeocodingHandler != null)
                        reverseGeocodingHandler.handle((Address)result.obj);
                }
            }
        }, new MyAsyncTask.MyAsyncTaskParameterBase(latLng), null, null, new MyAsyncTask.MyAsyncTaskUIHandler() {
            @Override
            public void handle(MyAsyncTask.MyAsyncTaskResultBase result) {
                // no UI lock
            }
        }, null, "");
    }
    public static LineInfo MapDrawPolyGon(GoogleMap map, LineInfo lineInfo, int penColor, float penWidth, boolean putMarkers) {
        PolylineOptions polyLineOptions = new PolylineOptions();

        ArrayList<LatLng> points = lineInfo.points;

        polyLineOptions.addAll(points);
        polyLineOptions.width(penWidth);
        polyLineOptions.color(penColor);

        if (points.size() > 0) {
            if (putMarkers) {
                lineInfo.srcMarker = map.addMarker(new MarkerOptions().position(points.get(0)).title(lineInfo.src).snippet(lineInfo.description));
                if (icon == null)
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ping_small_blue);
                lineInfo.srcMarker.setIcon(icon);
                lineInfo.srcMarker.setAnchor(Constants.ic_ping_small_blue_anchorU, Constants.ic_ping_small_blue_anchorV);
            }
            if (points.size() > 1) {
                if (putMarkers) {
                    lineInfo.dstMarker = map.addMarker(new MarkerOptions().position(points.get(points.size() - 1)).title(lineInfo.description).snippet(lineInfo.description));
                    lineInfo.dstMarker.setIcon(icon);
                    lineInfo.dstMarker.setAnchor(Constants.ic_ping_small_blue_anchorU, Constants.ic_ping_small_blue_anchorV);
                }
                map.addPolyline(polyLineOptions);
            }
        }
        return lineInfo;
    }

    public static void MapDrawLine(GoogleMap mMap, LineInfo lineInfo, float penWidth) {
        MapDrawPolyGon(mMap, lineInfo, lineInfo.color, penWidth, true);
        if (penWidth == Constants.PEN_WIDTH_LINE_BOLD) {
            MapDrawPolyGon(mMap, lineInfo, Color.WHITE, Constants.PEN_WIDTH_LINE_SMALL, false);
            MapDrawPolyGon(mMap, lineInfo, Color.BLACK, Constants.PEN_WIDTH_LINE_TINY, false);
        }
    }

    public static void MapDrawLines(GoogleMap mMap, List<LineInfo> linesInfo, int boldedLineId) {
        if (linesInfo == null) {
            mMap.clear();
            return;
        }
        if (currentLinesInfo != null && linesInfo.size() == currentLinesInfo.size() && currentBoldedLineId == boldedLineId) {
            boolean allFound = true;
            for (LineInfo lineInfo : linesInfo){
                boolean found = false;
                for (LineInfo currentLineInfo : currentLinesInfo){
                    if (lineInfo.lineId == currentLineInfo.lineId){
                        found = true;
                        continue;
                    }
                }
                if (!found) {
                    allFound = false;
                    break;
                }
            }
            if (allFound)
                return;
        }

        if (currentLinesInfo != null)
            for (LineInfo lineInfo : currentLinesInfo) {
                if (lineInfo.srcMarker != null)
                    lineInfo.srcMarker.remove();
                if (lineInfo.dstMarker != null)
                    lineInfo.dstMarker.remove();
            }
        mMap.clear();
        for (LineInfo lineInfo : linesInfo)
            if (lineInfo.lineId == boldedLineId)
                MapDrawLine(mMap, lineInfo, Constants.PEN_WIDTH_LINE_BOLD);
            else
                MapDrawLine(mMap, lineInfo, Constants.PEN_WIDTH_LINE_NORMAL);
        currentLinesInfo = linesInfo;
        currentBoldedLineId = boldedLineId;
    }

}

