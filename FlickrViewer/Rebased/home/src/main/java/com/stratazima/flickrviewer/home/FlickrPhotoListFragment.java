package com.stratazima.flickrviewer.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.stratazima.flickrviewer.home.dummy.DummyContent;
import com.stratazima.flickrviewer.processes.CustomList;
import com.stratazima.flickrviewer.processes.DataStorage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class FlickrPhotoListFragment extends ListFragment {
    private Context mContext;

    DataStorage jsonStorage;
    JSONArray daJSONArray;
    ListView listView;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FlickrPhotoListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: replace with a real list adapter.
        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                DummyContent.ITEMS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_flickrphoto_list, container, false);
        listView = (ListView) view.findViewById(R.id.flickrphoto_list);
        View header = inflater.inflate(R.layout.item_header, null);

        listView.addHeaderView(header);
        onListCreate();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
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

    /**
     * Checks if network is online
     */

    public boolean isNetworkOnline() {
        boolean status = false;
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.isConnected()) {
                status= true;
            }
        }

        return status;
    }

    private void onListCreate() {
        mContext = getActivity().getApplicationContext();
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

            listView.setAdapter(adapter);
        }
    }
    /**
     * Network dialog to inform the users.
     */
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
