package com.stratazima.flickrviewer.home;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class FlickrPhotoDetailActivity extends Activity {

    // Implements the fragment and passes information
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flickrphoto_detail);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(FlickrPhotoDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(FlickrPhotoDetailFragment.ARG_ITEM_ID));
            FlickrPhotoDetailFragment fragment = new FlickrPhotoDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.flickrphoto_detail_container, fragment, "photo_detail")
                    .addToBackStack("detail")
                    .commit();
        }
    }

    // Sets back on action bar selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, FlickrPhotoListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
