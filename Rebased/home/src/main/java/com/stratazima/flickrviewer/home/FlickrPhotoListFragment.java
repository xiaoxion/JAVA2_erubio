package com.stratazima.flickrviewer.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.*;
import android.widget.ListView;

import com.stratazima.flickrviewer.processes.CustomList;
import com.stratazima.flickrviewer.processes.DataStorage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FlickrPhotoListFragment extends ListFragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    DataStorage jsonStorage;
    JSONArray daJSONArray;

    public interface Callbacks {
        public void onItemSelected(String id);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };
    public FlickrPhotoListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getActivity().getLayoutInflater().inflate(R.layout.item_header, null);
        this.getListView().addHeaderView(view);
        onListCreate();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        try {
            mCallbacks.onItemSelected(daJSONArray.getJSONObject(position-1).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    public boolean isNetworkOnline() {
        boolean status = false;

        if (getActivity() != null) {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null) {
                if (netInfo.isConnected()) {
                    status= true;
                }
            }
        }

        return status;
    }

    public void onListCreate() {
        Context mContext = getActivity().getApplicationContext();
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
            CustomList adapter = new CustomList(getActivity(), strings, isConnected, myList);

            setListAdapter(adapter);
        }
    }

    private void onNoNetworkDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog alertBuilder =  builder.create();
        alertBuilder.show();
    }


}
