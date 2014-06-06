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
import android.view.View;
import android.widget.*;
import com.stratazima.flickrviewer.processes.CustomList;
import com.stratazima.flickrviewer.processes.DataStorage;
import com.stratazima.flickrviewer.processes.NetworkServices;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


public class MainActivity extends Activity {
    public static final String MESSAGE = "messenger";
    private Menu refreshMenu;
    private Handler mHandle;
    private Context mContext;
    DataStorage jsonStorage;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        View listHeader = this.getLayoutInflater().inflate(R.layout.header_listview, null);
        listView.addHeaderView(listHeader);

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
                    onListCreate();
                }}
        };

        onListCreate();

        if (isNetworkOnline()){
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onRefresh();
                }
            }, 2000);
        }
    }

    /**
     * Refreshed the Flickr Feed according to when the
     * user hits the button to refresh. Also checks if
     * there  is current data to load.
     */

    private void onRefresh(){
        setProgressBar(true);
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

    public void onListCreate() {
        mContext = getApplicationContext();
        jsonStorage = DataStorage.getInstance(mContext);
        JSONArray daJSONArray = null;
        ArrayList<HashMap<String,String>> myList = new ArrayList<HashMap<String, String>>();

        /**
         * Checks the network state and runs the appropriate view.
         * Also handles if there is data and no connection so the
         * user can view their data.
         */

        if (isNetworkOnline()) {
            if (jsonStorage.onCheckFile()) {
                daJSONArray = jsonStorage.onReadFile();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Please Connect to Network", Toast.LENGTH_SHORT);
            toast.show();
            if (jsonStorage.onCheckFile()) {
                daJSONArray = jsonStorage.onReadFile();
            } else {
                return;
            }
        }

        if(daJSONArray != null) {
            String username = null;
            String title = null;
            String imageURL = null;

            for (int i = 0; i < daJSONArray.length(); i++) {
                JSONObject tempObj = null;
                try {
                    tempObj =  daJSONArray.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (tempObj != null) {
                    try {
                        username = tempObj.getString("username");
                        title = tempObj.getString("title");
                        imageURL = tempObj.getString("imageURL");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                HashMap<String,String> displayMap = new HashMap<String, String>();
                displayMap.put("username", username);
                displayMap.put("title", title);
                displayMap.put("imageURL", imageURL);

                myList.add(displayMap);
            }

            String[] strings = new String[myList.size()];
            String[] values = {"username", "title","imageURL"};
            CustomList adapter = new CustomList(this, strings, values, myList);
            listView.setAdapter(adapter);
        }
    }
}
