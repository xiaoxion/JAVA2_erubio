package com.stratazima.flickrviewer.processes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.stratazima.flickrviewer.home.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.processes
 * Author  Esau
 * Date    6/6/2014
 */

public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final boolean isConnected;
    private final ArrayList<HashMap<String, String>> daArrayList;

    public CustomList(Activity context, String[] length, boolean isConnected, ArrayList<HashMap<String, String>> daArrayList) {
        super(context, R.layout.item_custom, length);

        this.context = context;
        this.isConnected = isConnected;
        this.daArrayList = daArrayList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        AQuery aq = new AQuery(context);

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.item_custom, null, true);

        TextView mainTitle = (TextView) rowView.findViewById(R.id.main_row);
        TextView subTitle = (TextView) rowView.findViewById(R.id.sub_row);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.flickr_image);
        RatingBar ratingBar = (RatingBar) rowView.findViewById(R.id.main_rating);

        mainTitle.setText(daArrayList.get(position).get("username"));
        subTitle.setText(daArrayList.get(position).get("title"));
        if (isConnected) {
            aq.id(imageView).image(daArrayList.get(position).get("imageURL"));
        }
        ratingBar.setRating(Float.parseFloat(daArrayList.get(position).get("rating")));
        return rowView;
    }
}
