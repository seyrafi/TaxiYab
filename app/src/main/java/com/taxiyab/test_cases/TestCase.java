package com.taxiyab.test_cases;

import android.content.Context;
import android.graphics.Color;

import com.taxiyab.Model.LineInfo;
import com.taxiyab.db.LinesDB;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class TestCase {
    public static List<LineInfo> getLinesData(){
        List<LineInfo> list = new ArrayList<>();

        LineInfo line = new LineInfo(1, "فاطمی", "فلکه دوم صادقیه", 25000, "", Color.BLUE);
        line.addPoint(35.719891, 51.404580);
        line.addPoint(35.722106, 51.400597);
        line.addPoint(35.724322, 51.398605);
        line.addPoint(35.724861, 51.396319);
        line.addPoint(35.723124, 51.391746);
        line.addPoint(35.723902, 51.384444);
        line.addPoint(35.726537, 51.377438);
        line.addPoint(35.727435, 51.372939);
        line.addPoint(35.729291, 51.367259);
        line.addPoint(35.728214, 51.361875);
        line.addPoint(35.728247, 51.355244);
        line.addPoint(35.726540, 51.351296);
        line.addPoint(35.726819, 51.337392);
        line.addPoint(35.726784, 51.334988);
        line.addPoint(35.726611, 51.335092);
        line.addPoint(35.726625, 51.337618);
        line.addPoint(35.725058, 51.337149);
        line.addPoint(35.724369, 51.335723);
        list.add(line);

        line = new LineInfo(2, "میدان ولیعصر", "میدان ونک", 20000,  "از ولیعصر", Color.RED);
        line.addPoint(35.712288, 51.407246);
        line.addPoint(35.721395, 51.408758);
        line.addPoint(35.723754, 51.410559);
        line.addPoint(35.723896, 51.416255);
        line.addPoint(35.726963, 51.415674);
        line.addPoint(35.727576, 51.415092);
        line.addPoint(35.727151, 51.413756);
        line.addPoint(35.737247, 51.409164);
        line.addPoint(35.738427, 51.409106);
        line.addPoint(35.748191, 51.411722);
        line.addPoint(35.757011, 51.410269);
        list.add(line);

        line = new LineInfo(3, "میدان ونک", "میدان رسالت",34000 , "از مسیر صیاد", Color.MAGENTA);
        line.addPoint(35.757373, 51.410628);
        line.addPoint(35.758203, 51.421647);
        line.addPoint(35.758778, 51.427707);
        line.addPoint(35.756159, 51.435420);
        line.addPoint(35.750710, 51.438577);

        line.addPoint(35.748642, 51.438330);
        line.addPoint(35.748776, 51.437179);
        line.addPoint(35.749910, 51.437590);
        line.addPoint(35.751044, 51.446385);
        line.addPoint(35.755379, 51.455672);
        line.addPoint(35.756513, 51.463809);
        line.addPoint(35.755913, 51.465207);
        line.addPoint(35.749309, 51.464714);
        line.addPoint(35.745507, 51.463645);
        line.addPoint(35.740454, 51.460040);
        line.addPoint(35.740576, 51.459286);
        line.addPoint(35.740974, 51.459361);
        line.addPoint(35.741127, 51.460680);
        line.addPoint(35.740454, 51.465240);
        line.addPoint(35.739138, 51.474021);
        line.addPoint(35.736722, 51.487173);
        list.add(line);

        line = new LineInfo(4, "میدان ونک", "میدان رسالت",34000 , "از مسیر سید خندان", Color.GREEN);
        line.addPoint(35.757373, 51.410628);
        line.addPoint(35.758203, 51.421647);
        line.addPoint(35.758778, 51.427707);
        line.addPoint(35.756159, 51.435420);
        line.addPoint(35.750710, 51.438577);
        line.addPoint(35.749141, 51.438749);
        line.addPoint(35.746563, 51.436517);
        line.addPoint(35.744543, 51.435659);
        line.addPoint(35.741648, 51.435422);
        line.addPoint(35.740663, 51.434421);
        line.addPoint(35.740786, 51.433753);
        line.addPoint(35.741279, 51.433571);
        line.addPoint(35.742215, 51.434906);
        line.addPoint(35.742609, 51.438548);
        line.addPoint(35.742535, 51.443828);
        line.addPoint(35.741908, 51.446710);
        line.addPoint(35.741529, 51.451789);
        line.addPoint(35.740984, 51.457637);
        line.addPoint(35.741155, 51.459813);
        line.addPoint(35.740908, 51.461661);
        line.addPoint(35.740465, 51.463863);
        line.addPoint(35.740415, 51.465692);
        line.addPoint(35.739531, 51.471938);
        line.addPoint(35.737646, 51.481226);
        line.addPoint(35.736596, 51.487287);
        list.add(line);

        line = new LineInfo(5, "میدان انقلاب", "میدان امام حسین", 23000, "", Color.BLUE);
        line.addPoint(35.700760, 51.391616);
        line.addPoint(35.701096, 51.403374);
        line.addPoint(35.701372, 51.417543);
        line.addPoint(35.701616, 51.430921);
        line.addPoint(35.701800, 51.438534);
        line.addPoint(35.701953, 51.447955);
        list.add(line);

        line = new LineInfo(6, "میدان انقلاب", "کوی دانشگاه",18000 ,"" , Color.CYAN);
        line.addPoint(35.701436, 51.391235);
        line.addPoint(35.707507, 51.390369);
        line.addPoint(35.714154, 51.389582);
        line.addPoint(35.717285, 51.389661);
        line.addPoint(35.723228, 51.388795);
        line.addPoint(35.725336, 51.388789);
        line.addPoint(35.734345, 51.386585);
        list.add(line);

        return list;
    }

    public static void LoadTestCaseToDb(LinesDB db){
        List<LineInfo> linesInfo = TestCase.getLinesData();
        db.forceUpgrade();
        db.deleteAllLines();
        db.addLines(linesInfo);
    }
}
