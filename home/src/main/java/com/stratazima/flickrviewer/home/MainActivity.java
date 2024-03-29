package com.stratazima.flickrviewer.home;

/**
 * Project FlickrViewer
 * Package com.stratazima.flickrviewer.home
 * Author  Esau
 * Date    6/2/2014
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
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
    JSONArray daJSONArray;
    ListView listView;
    final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        View listHeader = this.getLayoutInflater().inflate(R.layout.item_header, null);
        listView.addHeaderView(listHeader);

        mHandle = new Handler() {
            /**
             * Handler that saves/overwrites the file.
             */
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == RESULT_OK && msg.obj != null) {
                    String temp = (String) msg.obj;
                    jsonStorage.onWriteFile(temp);
                    setProgressBar(false);
                    onListCreate();
                }}
        };

        onListCreate();

        // Set delay for a new refresh.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onRefresh();
            }
        }, 2000);
    }

    /**
     * Refreshed the Flickr Feed according to when the
     * user hits the button to refresh. Also checks if
     * there  is current data to load.
     */

    private void onRefresh(){
        if (isNetworkOnline()) {
            setProgressBar(true);

            mContext = getApplicationContext();
            jsonStorage = DataStorage.getInstance(mContext);

            Messenger refreshMessenger = new Messenger(mHandle);
            Intent networkIntent = new Intent(this, NetworkServices.class);
            networkIntent.putExtra(MESSAGE, refreshMessenger);
            startService(networkIntent);
        } else {
            onNoNetworkDialog("Need Network to Load");
        }
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

    /**
     * Creates the list adapter and sends it to a
     * custom list adapter that will allow for image loading.
     */

    private void onListCreate() {
        mContext = getApplicationContext();
        jsonStorage = DataStorage.getInstance(mContext);
        daJSONArray = null;
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
            if (jsonStorage.onCheckFile()) {
                daJSONArray = jsonStorage.onReadFile();
                onNoNetworkDialog("Local Data Only");
            } else {
                onNoNetworkDialog("Connect to Network");
                return;
            }
        }

        if(daJSONArray != null) {
            String username = null;
            String title = null;
            String imageURL = null;
            String daRating = null;

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
                        daRating = tempObj.getString("rating");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                HashMap<String,String> displayMap = new HashMap<String, String>();
                displayMap.put("username", username);
                displayMap.put("title", title);
                displayMap.put("imageURL", imageURL);
                displayMap.put("rating", daRating);

                myList.add(displayMap);
            }

            String[] strings = new String[myList.size()];
            boolean isConnected = isNetworkOnline();
            CustomList adapter = new CustomList(this, strings, isConnected, myList);
            listView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent(MainActivity.this, PhotoViewerActivity.class);

                            JSONObject tempObject = null;
                            try {
                                tempObject = (JSONObject) daJSONArray.get(i-1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (tempObject != null) {
                                intent.putExtra("flickrObj", tempObject.toString());
                                intent.putExtra("position", Integer.toString(i-1));
                            }
                            startActivityForResult(intent, 1);
                        }
                    }
            );

            listView.setAdapter(adapter);
        }
    }

    /**
     * Network dialog to inform the users.
     */
    private void onNoNetworkDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Accepted");
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "Continued");
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog alertBuilder =  builder.create();
        alertBuilder.show();
    }

    /**
     * Receives data, writes to disk, and recreates list with update
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    daJSONArray.getJSONObject(Integer.parseInt(data.getStringExtra("position"))).put("rating", data.getStringExtra("rateResult"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonStorage.onWriteFile(daJSONArray.toString());
                onListCreate();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
