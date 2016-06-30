// ref: http://abhiandroid.com/ui/custom-spinner-examples.html
package com.taxiyab.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.taxiyab.Model.DestStructureLine;
import com.taxiyab.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MehrdadS on 6/20/2016.
 */
public class PickupTimeSpinnerAdapter<Long> extends BaseAdapter {
    private final Context context;
    private final ArrayList itemsList;

    public PickupTimeSpinnerAdapter(Context context, @NonNull ArrayList objects) {
        this.context = context;
        this.itemsList = objects;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.spinner_pickup_time_item, parent, false);

        long dt = java.lang.Long.parseLong(itemsList.get(position).toString());

        TextView lblTime = (TextView) rowView.findViewById(R.id.lblTime);
        TextView lblCount = (TextView) rowView.findViewById(R.id.lblCount);
        TextView lblText = (TextView) rowView.findViewById(R.id.lblText);


        if (dt == 0) {
            lblTime.setVisibility(View.GONE);
            lblCount.setVisibility(View.GONE);
            lblText.setText("هم اکنون");
        }else{
            lblTime.setText(android.text.format.DateFormat.format("hh:mm", dt).toString());
            lblCount.setText(String.valueOf(position));
        }
        return rowView;
    }
}