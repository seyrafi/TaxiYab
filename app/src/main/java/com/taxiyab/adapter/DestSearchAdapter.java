package com.taxiyab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.taxiyab.Model.DestStructureLine;
import com.taxiyab.R;

import java.util.List;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class DestSearchAdapter extends ArrayAdapter<DestStructureLine> {
    private final Context context;
    private final List<DestStructureLine> itemsList;

    public DestSearchAdapter(Context context, List<DestStructureLine> itemsList) {
        super(context, R.layout.dest_search_list_item_line, itemsList);
        this.context = context;
        this.itemsList = itemsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.dest_search_list_item_line, parent, false);

        DestStructureLine destInfo = itemsList.get(position);

        //ImageView imgFavorite = (ImageView) rowView.findViewById(R.id.imgFavorite);
        TextView lblDest = (TextView) rowView.findViewById(R.id.lblDest);
        TextView lblSrc = (TextView) rowView.findViewById(R.id.lblSrc);
        TextView lblFare = (TextView) rowView.findViewById(R.id.lblFare);

        lblDest.setText(destInfo.dest);
        lblSrc.setText(destInfo.src);
        lblFare.setText(destInfo.fare);
        return rowView;
    }
}