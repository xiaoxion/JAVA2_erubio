package com.stratazima.flickrviewer.home;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.stratazima.flickrviewer.home.R;
import com.stratazima.flickrviewer.processes.DataStorage;
import com.stratazima.flickrviewer.processes.NetworkServices;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * An activity representing a list of FlickrPhotos. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link FlickrPhotoDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FlickrPhotoListFragment} and the item details
 * (if present) is a {@link FlickrPhotoDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link FlickrPhotoListFragment.Callbacks} interface
 * to listen for item selections.
 */
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

        if (findViewById(R.id.flickrphoto_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((FlickrPhotoListFragment) getFragmentManager()
                    .findFragmentById(R.id.flickrphoto_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane && !id.equals("")) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
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
