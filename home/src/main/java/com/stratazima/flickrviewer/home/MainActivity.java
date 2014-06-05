package com.stratazima.flickrviewer.home;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.home
 * Author  Esau
 * Date    6/2/2014
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.stratazima.flickrviewer.processes.NetworkServices;


public class MainActivity extends Activity {
    public static final String MESSAGE = "messenger";
    public static final String TYPE = "type";
    public static final String USER_ID = "user";
    private Menu refreshMenu;
    private Handler daHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        daHandle = new Handler() {
            String response = null;

            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == RESULT_OK && msg.obj != null) {
                    try {
                        response = (String) msg.obj;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    setProgressBar(false);
                }}
        };
        onRefresh();
    }

    /**
     * Refreshed the Flickr Feed according to when the
     * user hits the button to refresh. Also checks if
     * there  is current data to load.
     */

    private void onRefresh(){
        Messenger refreshMessenger = new Messenger(daHandle);
        Intent networkIntent = new Intent(this, NetworkServices.class);
        networkIntent.putExtra(MESSAGE, refreshMessenger);
        networkIntent.putExtra(TYPE, 0);
        startService(networkIntent);
    }

    /**
     * Simple refresh button to refresh the feed
     * with the most current flickr feed
     * with an animation to give the user a sense
     * of progress.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        refreshMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            setProgressBar(true);
            onRefresh();
        }
        return true;
    }

    public void setProgressBar (final boolean refreshing) {
        if (refreshMenu != null) {
            final MenuItem refreshedItem = refreshMenu.findItem(R.id.action_refresh);
            if (refreshedItem != null) {
                if (refreshing) {
                    refreshedItem.setActionView(R.layout.refresh_progress);
                } else {
                    refreshedItem.setActionView(null);
                }
            }
        }
    }
}
