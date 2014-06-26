package com.stratazima.flickrviewer.home;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;
import com.stratazima.flickrviewer.dialogs.LoginFragment;
import com.stratazima.flickrviewer.processes.DataStorage;
import com.stratazima.flickrviewer.processes.NetworkServices;
import org.json.JSONArray;
import org.json.JSONObject;

public class FlickrPhotoListActivity extends Activity implements FlickrPhotoListFragment.Callbacks, SearchView.OnQueryTextListener {
    public static final String MESSAGE = "messenger";
    private Menu refreshMenu;
    public static boolean mTwoPane = false;
    private Handler mHandle;
    boolean isFavorite = false;
    DataStorage jsonStorage;

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
                    flickrPhotoListFragment.onListCreate(false, false);
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
        if (findViewById(R.id.flickrphoto_detail_container) != null && !id.equals("")) {
            Bundle arguments = new Bundle();
            arguments.putString(FlickrPhotoDetailFragment.ARG_ITEM_ID, id);
            FlickrPhotoDetailFragment fragment = new FlickrPhotoDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.flickrphoto_detail_container, fragment)
                    .addToBackStack("detail")
                    .commit();

        } else if (!id.equals("")){
            mTwoPane = false;
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

        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                FlickrPhotoListFragment flickrPhotoListFragment = (FlickrPhotoListFragment) getFragmentManager().findFragmentById(R.id.daListFrag);
                flickrPhotoListFragment.onSearchCancelled();
                return false;
            }
        });

        return true;
    }

    // Handles the action bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_refresh:
                onRefresh();
                return true;
            case R.id.action_shared_dialog:
                SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = preference.edit();
                if (preference.getString("username", "").isEmpty()) {
                    edit.putString("username", "guest");
                    edit.putString("password", "guest");
                    edit.apply();
                }

                DialogFragment dialogFragment = LoginFragment.newInstance(R.string.action_shared_dialog);
                dialogFragment.show(getFragmentManager(), "dialog");

                return true;
            case R.id.action_favorite:
                FlickrPhotoListFragment flickrPhotoListFragment = (FlickrPhotoListFragment) getFragmentManager().findFragmentById(R.id.daListFrag);

                if (isFavorite) {
                    isFavorite = false;
                    flickrPhotoListFragment.onListCreate(false, false);
                } else {
                    isFavorite = true;
                    flickrPhotoListFragment.onListCreate(false, true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Receive data from activity.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (findViewById(R.id.flickrphoto_detail_container) != null) {
                    mTwoPane = true;
                    Bundle arguments = new Bundle();
                    arguments.putString(FlickrPhotoDetailFragment.ARG_ITEM_ID, data.getStringExtra("intentResult"));
                    FlickrPhotoDetailFragment fragment = new FlickrPhotoDetailFragment();
                    fragment.setArguments(arguments);
                    getFragmentManager().beginTransaction()
                            .add(R.id.flickrphoto_detail_container, fragment, "photo_detail")
                            .addToBackStack("detail")
                            .commit();
                }
            }
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    // If the text changes it will sent it to adapter.
    @Override
    public boolean onQueryTextChange(String s) {
        FlickrPhotoListFragment flickrPhotoListFragment = (FlickrPhotoListFragment) getFragmentManager().findFragmentById(R.id.daListFrag);
        flickrPhotoListFragment.onSearching(s);
        return false;
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
