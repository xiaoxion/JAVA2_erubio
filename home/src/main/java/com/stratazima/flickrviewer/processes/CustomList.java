package com.stratazima.flickrviewer.processes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.stratazima.flickrviewer.home.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.processes
 * Author  Esau
 * Date    6/6/2014
 */

public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] values;
    private final ArrayList<HashMap<String, String>> daArrayList;

    public CustomList(Activity context, String[] length, String[] values, ArrayList<HashMap<String, String>> daArrayList) {
        super(context, R.layout.custom_listview, length);

        this.context = context;
        this.values = values;
        this.daArrayList = daArrayList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        AQuery aq = new AQuery(context);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.custom_listview, null, true);

        TextView mainTitle = (TextView) rowView.findViewById(R.id.main_row);
        TextView subTitle = (TextView) rowView.findViewById(R.id.sub_row);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.flickr_image);

        mainTitle.setText(daArrayList.get(position).get(values[0]));
        subTitle.setText(daArrayList.get(position).get(values[1]));
        aq.id(imageView).image(daArrayList.get(position).get(values[2]));
        return rowView;
    }
}
