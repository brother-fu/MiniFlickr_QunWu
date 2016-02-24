package com.miniflickr.qunwu.miniflickr;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.miniflickr.qunwu.miniflickr.util.UrlManager;

/**
 * Created by Qun Wu on 2016/2/18.
 */
public class GalleryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_gallery);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        //not sure why not call super.onNewIntent(intent)
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    /*
    new Intern --> new keyword to search
    1. get the keyword/query from intent --> intent.getStringExtra(SearchManager.Query)
    2. set the keyword into PreferenceManager(maintains shared values in app)
    3. get fragment from FragmentManagement
    5. refresh fragment
     */
    private void handleIntent(Intent intent){
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //query is the key word received from search_input
            String query = intent.getStringExtra(SearchManager.QUERY);
Log.d(GalleryActivity.class.getSimpleName(), ": query is->" + query);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
            suggestions.saveRecentQuery(query,null);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(UrlManager.PREF_SEARCH_QUERY, query)
                    .commit();

            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.gallery_fragment);
            if (fragment != null) {
                //refresh is a function defined in GalleryFragment
                ((GalleryFragment) fragment).refresh();
            }
        }
    }
}
