package com.taxiyab.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.taxiyab.MainActivity;
import com.taxiyab.Model.LineInfo;
import com.taxiyab.common.MyToast;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class LinesDB extends SQLiteOpenHelper {
    private static String TABLE_Lines = "Lines";
    private static String TABLE_Points = "Points";
    private static int db_version = 1;

    public LinesDB(Context context) {
        super(context, "TaxiYar", null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL("CREATE TABLE "+TABLE_Lines+" (lineId int PRIMARY KEY, src TEXT, dst TEXT, fare int, description TEXT, color int)");
            db.execSQL("CREATE TABLE "+TABLE_Points+" (latitude DOUBLE, longitude DOUBLE, lineId int)");
            db.execSQL("CREATE INDEX idx_latlng on "+TABLE_Points+" (latitude, longitude)");
        }catch(SQLiteException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try{
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_Lines);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_Points);
            onCreate(db);
        }catch(SQLiteException ex){
            ex.printStackTrace();
        }
    }

    public void addLines(List<LineInfo> lines){
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            for (LineInfo line : lines) {
                ContentValues values = new ContentValues();
                values.put("lineId", line.lineId);
                values.put("src", line.src);
                values.put("dst", line.dst);
                values.put("fare", line.fare);
                values.put("description", line.description);
                values.put("color", line.color);
                db.insert(TABLE_Lines, null, values);

                for (LatLng point : line.points) {
                    values = new ContentValues();
                    values.put("latitude", point.latitude);
                    values.put("longitude", point.longitude);
                    values.put("lineId", line.lineId);
                    db.insert(TABLE_Points, null, values);
                }
            }
        }catch(SQLiteException ex){
            ex.printStackTrace();
        }finally{
            if (db != null)
                db.close();
        }
    }

    public void forceUpgrade() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            onUpgrade(db, 1,1);
        }catch(SQLiteException ex){
            ex.printStackTrace();
        }finally{
            if (db != null)
                db.close();
        }
    }


    public int deleteAllLines() {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            return db.delete(TABLE_Points, null, null) + db.delete(TABLE_Lines, null, null); // TODO: a better return type
        }catch(SQLiteException ex){
            ex.printStackTrace();
        }finally{
            if (db != null)
                db.close();
        }
        return 0;
    }

    public void deleteLine(long lineId) {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.delete(TABLE_Points, "lineId" + " = ?", new String[] { String.valueOf(lineId) });
            db.delete(TABLE_Lines, "lineId" + " = ?", new String[] { String.valueOf(lineId) });
        }catch(SQLiteException ex){
            ex.printStackTrace();
        }
        finally{
            if (db != null)
                db.close();
        }
    }

    public List<LineInfo> findNearestLines(double centerLatitude, double centerLongitude, double threshold, int maxCount, boolean loadPoints){ // good practice: threshold = 0.04
        List<LineInfo> linesInfo = new ArrayList<LineInfo>();
        SQLiteDatabase db = null;
        try{
            db = this.getReadableDatabase();

            String countQuery = "select lin.*, p.distance from ("+
                    " SELECT lineId, min(abs(latitude-"+centerLatitude+")+abs(longitude-"+centerLongitude+")) as distance FROM " + TABLE_Points +
                    " where abs(latitude-"+centerLatitude+")<"+threshold+" and abs(longitude-"+centerLongitude+")<"+threshold+
                    " group by lineId " +
                    " order by min(abs(latitude-"+centerLatitude+")+abs(longitude-"+centerLongitude+")) asc limit "+ maxCount+
                    " ) p"+
                    " left outer join "+TABLE_Lines+" lin on p.lineId = lin.lineId"+
                    " where lin.lineId is not null"+
                    " order by distance asc";


                    //" order by min(abs(latitude-\"+latitude+\")+abs(longitude-\"+longitude+\")) asc";
            Cursor cursorLines = db.rawQuery(countQuery, null);
            //Cursor cursor = db.rawQuery(countQuery, new String[] { String.valueOf(latitude), String.valueOf(threshold), String.valueOf(longitude), String.valueOf(threshold)  });

            if (cursorLines == null || !cursorLines.moveToFirst() || cursorLines.getCount() == 0)
                return linesInfo;
            int countLines = cursorLines.getCount();
            for (int i=0; i < countLines; i++) {
                int lineId = cursorLines.getInt(0);
                String src = cursorLines.getString(1);
                String dst = cursorLines.getString(2);
                int fare = cursorLines.getInt(3);
                String description = cursorLines.getString(4);
                int color = cursorLines.getInt(5);
                LineInfo lineInfo = new LineInfo(lineId, src, dst, fare, description, color);
                cursorLines.moveToNext();

                if (loadPoints){
                    Cursor cursorPoints = db.rawQuery("SELECT latitude, longitude FROM " + TABLE_Points +" where lineId="+lineId, null);
                    if (cursorPoints == null || !cursorPoints.moveToFirst() || cursorPoints.getCount() == 0)
                        continue;
                    int countPoints = cursorPoints.getCount();
                    for (int j=0; j < countPoints; j++) {
                        double latitude = cursorPoints.getDouble(0);
                        double longitude = cursorPoints.getDouble(1);
                        lineInfo.addPoint(latitude, longitude);
                        cursorPoints.moveToNext();
                    }
                }
                linesInfo.add(lineInfo);
            }
            return linesInfo;
        }catch(SQLiteException ex){
            ex.printStackTrace();
            return new ArrayList<LineInfo>();
        }
        finally{
            if (db != null)
                db.close();
        }
    }

    public List<LineInfo> getLines(int selectedLineId){ // -1 for all lines
        List<LineInfo> linesInfo = new ArrayList<LineInfo>();
        SQLiteDatabase db = null;
        try{
            db = this.getReadableDatabase();
            String query = "SELECT lineId, src, dst, fare, description, color  FROM " + TABLE_Lines;
            if (selectedLineId != -1)
                query += " where lineId=" + selectedLineId;
            Cursor cursorLines = db.rawQuery(query, null);
            if (cursorLines == null || !cursorLines.moveToFirst() || cursorLines.getCount() == 0)
                return linesInfo;
            int countLines = cursorLines.getCount();
            for (int i=0; i < countLines; i++) {
                int lineId = cursorLines.getInt(0);
                String src = cursorLines.getString(1);
                String dst = cursorLines.getString(2);
                int fare = cursorLines.getInt(3);
                String description = cursorLines.getString(4);
                int color = cursorLines.getInt(5);
                LineInfo lineInfo = new LineInfo(lineId, src, dst, fare, description, color);
                cursorLines.moveToNext();

                Cursor cursorPoints = db.rawQuery("SELECT latitude, longitude FROM " + TABLE_Points +" where lineId="+lineId, null);
                if (cursorPoints == null || !cursorPoints.moveToFirst() || cursorPoints.getCount() == 0)
                    continue;
                int countPoints = cursorPoints.getCount();
                for (int j=0; j < countPoints; j++) {
                    double latitude = cursorPoints.getDouble(0);
                    double longitude = cursorPoints.getDouble(1);
                    lineInfo.addPoint(latitude, longitude);
                    cursorPoints.moveToNext();
                }

                linesInfo.add(lineInfo);
            }
            return linesInfo;
        }catch(SQLiteException ex){
            ex.printStackTrace();
            return new ArrayList<LineInfo>();
        }
        finally{
            if (db != null)
                db.close();
        }
    }

    public int getLinesCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getReadableDatabase();
            String countQuery = "SELECT * FROM " + TABLE_Lines;
            cursor = db.rawQuery(countQuery, null);
            return cursor.getCount();
        }catch(SQLiteException ex){
            return 0;
        }finally{
            if (cursor != null)
                cursor.close();
        }
    }

    public int getLinePointsCount(int lineId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getReadableDatabase();
            String countQuery = "SELECT * FROM " + TABLE_Points + " where lineId="+lineId;
            cursor = db.rawQuery(countQuery, null);
            return cursor.getCount();
        }catch(SQLiteException ex){
            return 0;
        }finally{
            if (cursor != null)
                cursor.close();
        }
    }

    public int getTotalPointsCount() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getReadableDatabase();
            String countQuery = "SELECT * FROM " + TABLE_Points;
            cursor = db.rawQuery(countQuery, null);
            return cursor.getCount();
        }catch(SQLiteException ex){
            return 0;
        }finally{
            if (cursor != null)
                cursor.close();
        }
    }

    public List<LatLng> getLinePoints(int lineId) {
        List<LatLng> list = new ArrayList<>();
        SQLiteDatabase db = null;
        try{
            db = this.getReadableDatabase();

            Cursor cursor = db.query(TABLE_Points, new String[] { "latitude", "longitude"}, "lineId=?",
                    new String[] { String.valueOf(lineId) }, null, null, null, null);
            if (cursor == null || !cursor.moveToFirst() || cursor.getCount() == 0)
                return list;
            int count = cursor.getCount();
            for (int i=0; i<count; i++) {
                list.add(new LatLng(cursor.getDouble(0), cursor.getDouble(1)));
                cursor.moveToNext();
            }
            return list;
        }catch(SQLiteException ex){
            ex.printStackTrace();
            return new ArrayList<LatLng>();
        }
        finally{
            if (db != null)
                db.close();
        }
    }

}
