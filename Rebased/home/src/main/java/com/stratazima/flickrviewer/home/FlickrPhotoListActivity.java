package com.stratazima.flickrviewer.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.stratazima.flickrviewer.processes.DataStorage;
import com.stratazima.flickrviewer.processes.NetworkServices;

public class FlickrPhotoListActivity extends Activity implements FlickrPhotoListFragment.Callbacks {
    public static final String MESSAGE = "messenger";
    private Menu refreshMenu;
    private boolean mTwoPane;
    DataStorage jsonStorage;
    private Handler mHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickrphoto_list);

        // Handler to handle the incoming data from service
        mHandle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == RESULT_OK && msg.obj != null) {
                    String temp = (String) msg.obj;
                    jsonStorage.onWriteFile(temp);
                    setProgressBar(false);
                    FlickrPhotoListFragment flickrPhotoListFragment = (FlickrPhotoListFragment) getFragmentManager().findFragmentById(R.id.daListFrag);
                    flickrPhotoListFragment.onListCreate();
                }}
        };

        // Handles weather or not there is two sections
        if (findViewById(R.id.flickrphoto_detail_container) != null) {
            mTwoPane = true;
            ((FlickrPhotoListFragment) getFragmentManager()
                    .findFragmentById(R.id.flickrphoto_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    // Selects what should be opened, a fragment or an activity
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane && !id.equals("")) {
            Bundle arguments = new Bundle();
            arguments.putString(FlickrPhotoDetailFragment.ARG_ITEM_ID, id);
            FlickrPhotoDetailFragment fragment = new FlickrPhotoDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.flickrphoto_detail_container, fragment)
                    .commit();

        } else if (!id.equals("")){
            Intent intent = new Intent(FlickrPhotoListActivity.this, FlickrPhotoDetailActivity.class);
            intent.putExtra(FlickrPhotoDetailFragment.ARG_ITEM_ID, id);
            startActivityForResult(intent, 1);
        } else {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }

    // Refresh menu creation a handling.
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

    // Checks if there is a valid network.
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

    // Sets the Progress bar while the data is loading.
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

    // Refreshed the file and updates is with the handler.
    public void onRefresh(){
        if (isNetworkOnline()) {
            setProgressBar(true);

            Context mContext = getApplicationContext();
            jsonStorage = DataStorage.getInstance(mContext);

            Messenger refreshMessenger = new Messenger(mHandle);
            Intent networkIntent = new Intent(this, NetworkServices.class);
            networkIntent.putExtra(MESSAGE, refreshMessenger);
            startService(networkIntent);
        } else {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
