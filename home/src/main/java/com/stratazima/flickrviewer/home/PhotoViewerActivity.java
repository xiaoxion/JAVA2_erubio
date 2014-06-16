package com.stratazima.flickrviewer.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.androidquery.AQuery;
import com.stratazima.flickrviewer.processes.DataStorage;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.home
 * Author  Esau
 * Date    6/9/2014
 */
public class PhotoViewerActivity extends Activity {
    RatingBar ratingBar;
    private JSONObject flickrObj;
    float mRating;
    int mPosition;
    String id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        mPosition = Integer.parseInt(getIntent().getStringExtra("position"));

        onSetData();
        onSetRatingBar();
        onFlickrButton();
    }

    /**
     * Pulls the data from intent and then sets it on the
     */
    private void onSetData() {
        try {
            flickrObj = new JSONObject(getIntent().getStringExtra("flickrObj"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

        AQuery aq = new AQuery(this);

        TextView usernameView = (TextView) findViewById(R.id.intent_username);
        TextView realnameView = (TextView) findViewById(R.id.intent_realname);
        TextView locationView = (TextView) findViewById(R.id.intent_location);
        ImageView largeURLView = (ImageView) findViewById(R.id.large_image);
        TextView titleView = (TextView) findViewById(R.id.intent_title);

        usernameView.setText(username);
        realnameView.setText(realname);
        locationView.setText(location);
        aq.id(largeURLView).image(largeURL);
        titleView.setText(title);
    }

    /**
     * Saves rating bar data and sets it.
     */
    private void onSetRatingBar() {
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        ratingBar.setRating(mRating);
        ratingBar.setOnRatingBarChangeListener(
                new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar rateBar, float v, boolean b) {
                        mRating = rateBar.getRating();
                    }
                }
        );
    }

    /**
     * Saves and restores the data when orientation changes
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat("rating", mRating);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRating = savedInstanceState.getFloat("rating");
    }

    /**
     * Returns data to main activity
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("rateResult", Float.toString(mRating));
            returnIntent.putExtra("position", Integer.toString(mPosition));
            setResult(RESULT_OK, returnIntent);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Adds button view and sends implicit intent to action_view
     */
    private void onFlickrButton(){
        Button button = (Button) findViewById(R.id.flickr_web);
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
}
