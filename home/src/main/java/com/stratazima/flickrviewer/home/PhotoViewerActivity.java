package com.stratazima.flickrviewer.home;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidquery.AQuery;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.home
 * Author  Esau
 * Date    6/9/2014
 */
public class PhotoViewerActivity extends Activity {
    private JSONObject flickrObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        try {
            flickrObj = new JSONObject(getIntent().getStringExtra("flickrObj"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String username = null;
        String title = null;
        String largeURL = null;

        try {
            username = flickrObj.getString("username");
            title    = flickrObj.getString("title");
            largeURL = flickrObj.getString("largeURL");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AQuery aq = new AQuery(this);

        TextView usernameView = (TextView) findViewById(R.id.intent_username);
        ImageView largeURLView = (ImageView) findViewById(R.id.large_image);
        TextView titleView = (TextView) findViewById(R.id.intent_title);

        usernameView.setText(username);
        aq.id(largeURLView).image(largeURL);
        titleView.setText(title);
    }
}
