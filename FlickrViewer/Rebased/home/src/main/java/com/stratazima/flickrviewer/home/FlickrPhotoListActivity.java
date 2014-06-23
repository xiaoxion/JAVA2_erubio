package com.stratazima.flickrviewer.home;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import com.stratazima.flickrviewer.home.R;
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
public class FlickrPhotoListActivity extends Activity
        implements FlickrPhotoListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickrphoto_list);

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

    /**
     * Callback method from {@link FlickrPhotoListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        JSONObject tempObject = null;
        try {
            tempObject = (JSONObject) daJSONArray.get(i-1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mTwoPane) {
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

        } else {
            Intent intent = new Intent(FlickrPhotoListActivity.this, FlickrPhotoDetailActivity.class);
            intent.putExtra("flickrObj", tempObject.toString());
            intent.putExtra("position", Integer.toString(i-1));
            startActivityForResult(intent, 1);
        }
    }
}
