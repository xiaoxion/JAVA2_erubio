package com.stratazima.flickrviewer.home;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.home
 * Author  Esau
 * Date    6/2/2014
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.stratazima.flickrviewer.processes.DataStorage;
import com.stratazima.flickrviewer.processes.NetworkServices;


public class MainActivity extends Activity {
    public static final String MESSAGE = "messenger";
    private Menu refreshMenu;
    private Handler mHandle;
    private Context mContext;
    DataStorage jsonStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandle = new Handler() {
            /**
             * Handler that saves/overwrites the file and makes a toast
             * for debug reasons and give user feedback.
             * Will remove later on.
             */
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == RESULT_OK && msg.obj != null) {
                    mContext = getApplicationContext();
                    jsonStorage = DataStorage.getInstance(mContext);
                    String temp = (String) msg.obj;
                    if (jsonStorage.onWriteFile(temp)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Loaded", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    setProgressBar(false);
                }}
        };
        mContext = getApplicationContext();
        jsonStorage = DataStorage.getInstance(mContext);

        if (isNetworkOnline()) {
            if (jsonStorage.onCheckFile()) {
                jsonStorage.onReadFile();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onRefresh();
                        jsonStorage.onReadFile();
                    }
                }, 3000);

            } else {
                onRefresh();
                jsonStorage.onReadFile();
            }
        } else {
            if (jsonStorage.onCheckFile()) {
                jsonStorage.onReadFile();
            }
            Toast toast = Toast.makeText(getApplicationContext(), "Please Connect to Network", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Refreshed the Flickr Feed according to when the
     * user hits the button to refresh. Also checks if
     * there  is current data to load.
     */

    private void onRefresh(){
        Messenger refreshMessenger = new Messenger(mHandle);
        Intent networkIntent = new Intent(this, NetworkServices.class);
        networkIntent.putExtra(MESSAGE, refreshMessenger);
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

    /**
     * Checks if network is online
     */

    public boolean isNetworkOnline() {
        boolean status = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.isConnected()) {
                status= true;
            }
        }

        return status;
    }
}
