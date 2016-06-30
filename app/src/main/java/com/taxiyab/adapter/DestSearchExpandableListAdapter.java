// ref: http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
package com.taxiyab.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.taxiyab.FragmentMap;
import com.taxiyab.Model.DestStructureGooglePlace;
import com.taxiyab.Model.DestStructureLine;
import com.taxiyab.Model.DestStructureBase;
import com.taxiyab.R;

/**
 * Created by MehrdadS on 6/21/2016.
 */
public class DestSearchExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private List<Pair<String, List<DestStructureBase>>> _listDataChild;

    public List<Pair<String, List<DestStructureBase>>> getData(){
        return _listDataChild;
    }

    public DestSearchExpandableListAdapter(Context context, List<String> listDataHeader, List<Pair<String, List<DestStructureBase>>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(groupPosition).second.get(childPosititon);
        //return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        DestStructureBase obj = (DestStructureBase) getChild(groupPosition, childPosition);
        if (obj.objectType == DestStructureBase.DestStructureType.LOCAL_LINES_DEST) {
            DestStructureLine dst = (DestStructureLine)obj;

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.dest_search_list_item_line, null);
            }

            TextView lblDest = (TextView) convertView.findViewById(R.id.lblDest);
            TextView lblSrc = (TextView) convertView.findViewById(R.id.lblSrc);
            TextView lblFare = (TextView) convertView.findViewById(R.id.lblFare);
            ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);

            lblDest.setText(dst.dest);
            lblSrc.setText(dst.src);
            lblFare.setText(String.valueOf(dst.fare));
        }
        else if (obj.objectType == DestStructureBase.DestStructureType.GOOLE_PLACES_MAP) {
            DestStructureGooglePlace dst = (DestStructureGooglePlace)obj;

            if (convertView == null) {
                LayoutInflater infalater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalater.inflate(R.layout.dest_search_list_item_google_place, null);
            }

            TextView lblName = (TextView) convertView.findViewById(R.id.lblName);
            lblName.setText(dst.description);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(groupPosition).second.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        ExpandableListView eLV = (ExpandableListView) parent;
        eLV.expandGroup(groupPosition);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}