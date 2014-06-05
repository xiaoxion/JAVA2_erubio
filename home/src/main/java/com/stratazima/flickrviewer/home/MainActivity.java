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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.stratazima.flickrviewer.processes.JSONStorage;
import com.stratazima.flickrviewer.processes.NetworkServices;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;


public class MainActivity extends Activity {
    public static final String MESSAGE = "messenger";
    public static final String TYPE = "type";
    private Menu refreshMenu;
    private Handler daHandle;
    JSONStorage jsonStorage = JSONStorage.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        daHandle = new Handler() {
            String response = null;

            /**
             * Handler that saves/overwrites the file and makes a toast
             * for debug reasons and give user feedback.
             * Will remove later on.
             */
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == RESULT_OK && msg.obj != null) {
                    if (jsonStorage.onWriteFile((String) msg.obj)) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
                        toast.show();
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

    /**
     * Handles Reading of the file
     */

    private JSONObject onReadFile() {
        JSONObject daObject = null;
        String rand = null;
        try {
            FileInputStream fis = openFileInput("flickr.json");
            InputStreamReader isr = new InputStreamReader(fis);

            fis.read(rand.getBytes());
            fis.close();

            daObject = new JSONObject(rand);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return daObject;
    }
}
