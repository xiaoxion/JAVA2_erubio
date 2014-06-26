package com.stratazima.flickrviewer.home;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.androidquery.AQuery;
import com.stratazima.flickrviewer.processes.DataStorage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FlickrPhotoDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    private JSONObject flickrObj;
    float mRating;
    String id = null;

    public FlickrPhotoDetailFragment() {
    }

    // Populates the flickrObj with the incoming JSON data
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        try {
            flickrObj = new JSONObject(bundle.getString(ARG_ITEM_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Sets the UI and creates it
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flickrphoto_detail, container, false);
        onSetData(rootView);
        onSetRatingBar(rootView);
        onFlickrButton(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // Handles when going into landscape
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!FlickrPhotoListActivity.mTwoPane) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("intentResult", flickrObj.toString());
            getFragmentManager().popBackStack();
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            getActivity().finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    // Calls the on update JSON
    @Override
    public void onPause() {
        super.onPause();
        onUpdateJSON();
    }

    // Sets the UI data
    private void onSetData(View view) {
        String username = null;
        String realname = null;
        String location = null;
        String title = null;
        String largeURL = null;

        try {
            username = flickrObj.getString("username");
            realname = flickrObj.getString("realname");
            location = flickrObj.getString("location");
            title    = flickrObj.getString("title");
            largeURL = flickrObj.getString("largeURL");
            mRating  = flickrObj.getInt("rating");
            id       = flickrObj.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AQuery aq = new AQuery(getActivity());

        TextView usernameView = (TextView) view.findViewById(R.id.intent_username);
        TextView realNameView = (TextView) view.findViewById(R.id.intent_realname);
        TextView locationView = (TextView) view.findViewById(R.id.intent_location);
        ImageView largeURLView = (ImageView) view.findViewById(R.id.large_image);
        TextView titleView = (TextView) view.findViewById(R.id.intent_title);

        usernameView.setText(username);
        realNameView.setText(realname);
        locationView.setText(location);
        aq.id(largeURLView).image(largeURL);
        titleView.setText(title);
    }

    // Sets the rating bar so we can capture the data
    private void onSetRatingBar(View view) {
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        ratingBar.setRating(mRating);
        ratingBar.setOnRatingBarChangeListener(
                new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar rateBar, float v, boolean b) {
                        mRating = rateBar.getRating();
                        try {
                            flickrObj.put("rating", mRating);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    // Sets the Flickr Button so the user can check photo
    // on the web
    private void onFlickrButton(View view){
        Button button = (Button) view.findViewById(R.id.flickr_web);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = "http://flickr.com/photo.gne?id=" + id;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                }
        );
    }

    // Receives updated JSON object and writes it to the main file.
    public void onUpdateJSON() {
        DataStorage jsonStorage = DataStorage.getInstance(getActivity());
        JSONArray daJSONArray = jsonStorage.onReadFile();

        int tempInt;
        try {
            tempInt = flickrObj.getInt("position");
            daJSONArray.put(tempInt, flickrObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonStorage.onWriteFile(daJSONArray.toString());
    }
}
