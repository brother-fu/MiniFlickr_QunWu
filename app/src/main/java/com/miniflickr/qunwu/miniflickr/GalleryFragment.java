package com.miniflickr.qunwu.miniflickr;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.miniflickr.qunwu.miniflickr.entity.GalleryItem;
import com.miniflickr.qunwu.miniflickr.util.UrlManager;
import com.reginald.swiperefresh.CustomSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qun Wu on 2016/2/18.
 */

/*
Architecture:
        GalleryActivity --> it's a activity (corresponding layout: 'activity_layout')
            has a Fragment(layout: activity_gallery has Fragment, id is 'gallery_fragment')--> it's a view defined in active_gallery

        Fragment: GalleryFragment --> it's a view in GalleryActivity ('activity_layout' defines it as a view in GalleryActivity)
            require empty constructor
            the corresponding layout for GalleryFragment is 'activity_gallery_fragment'
            (the view also can have a layout and use 'inflater.inflate()' to initialize a view of it--> inflate() returns a view of corresponding view layout)
            has a CustomSwipeRefreshLayout

        No CustomSwipeRefreshLayout(Introduce SwipeRefreshLayout instead): it's a view, not layout
             This layout should be made the parent of the view that will be refreshed, as a result of the gesture
             support one direct child(the child is RecyclerView)
             Problem: if the child is another List/Grid view which also has onScrollListener/refresh function, there will be a conflict when scroll up to refresh
                      the solution is to let the refresh/onScrollListener work when the first view in the list is display (only scroll up has this problem)

        RecyclerView:(the only child of CustomSwipeRefreshLayout)
            it's a flexible view for providing a 'limited window' into a large data set (similar to list/grid view)
            have the scroll listener(if it's refreshed only when scroll down, it won't have the scroll conflict listed in SwipeRefreshLayout --> cause it's scroll up problem)
            use adapter for each imgView in RecyclerView
            adapter: a subclass of RecyclerView.Adapter

        RecyclerAdapter: Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView
 */
public class GalleryFragment extends Fragment{
    private static final String TAG = GalleryFragment.class.getSimpleName();
    private static final int COLUMN_NUM = 3;
    private static final int ITEM_PER_PAGE = 100;

    //maintain the http requests
    private RequestQueue mRq;
   /*
   recyclerView is very similar to GridView and use gridLayout as well
   for this project, use addOnScrollListener
    */
    private RecyclerView mRecyclerView;
    //grid layout manager for RecyclerView
    private GridLayoutManager mLayoutManager;
    /*
    swipe action can update content in layout
    we just need to define setOnRefreshListener()
    it's a view because we use .findViewById(R.id.SWIPE_ID)
     */
    private CustomSwipeRefreshLayout mCustomSwipeRefreshLayout;

    /*
    overview: A flexible view for providing a limited window into a large data set
    use adapter for each grid/imgView in GridView
    adapter: a subclass of RecyclerView.Adapter
     */
    private GalleryAdapter mAdapter;

    //is loading or not
    private boolean mLoading = false;
    //if the server side has more picts
    private boolean mHasMore = true;

    private SearchView mSearchView;

    public GalleryFragment() {
        // Required empty public constructor
    }

    /*
    initialize something except the fragment view
    the view is initialized and return in onCreateView
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //initialize the instance of the view of fragment with 'activity_gallery_fragment.xml'(layout)
        View fragment = inflater.inflate(R.layout.activity_gallery_fragment, container,false);
        //initial RecyclerView
        mRecyclerView = (RecyclerView) fragment.findViewById(R.id.recycler_view);
        //note: it's a view obtained by function 'findViewById'
        mCustomSwipeRefreshLayout = (CustomSwipeRefreshLayout) fragment.findViewById(R.id.swipe_refresh);
        //this.getActivity is FragmentActivity
        mRq = Volley.newRequestQueue(this.getActivity());

        mCustomSwipeRefreshLayout.setOnRefreshListener(
                new CustomSwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                });

        //set layout for RecyclerView instance, mRecyclerView
        mLayoutManager = new GridLayoutManager(getActivity(), COLUMN_NUM);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //set Adapter for RecyclerView, the Context in GalleryAdaper is getActivity()
        mAdapter = new GalleryAdapter(getActivity(), new ArrayList<GalleryItem>());
        mRecyclerView.setAdapter(mAdapter);
        //to improve the performance, RecyclerView has a fixed size
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItem = mLayoutManager.getItemCount();
                int lastItemPos = mLayoutManager.findLastVisibleItemPosition();
                if (mHasMore && !mLoading && totalItem - 1 != lastItemPos) {
                    startLoading();
                }
            }
        });
        //load the initial 100 pictures
        startLoading();
        return fragment;
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLoading();
    }

    private void startLoading(){
        Log.d(TAG, "line 166: startLoading");
        //mark current app status is under loading, so cannot handle next request(see line-141)
        mLoading = true;
        //how many items are already loaded
        int totalItem = mLayoutManager.getItemCount();
        //next page of photos we need to search from server
        final int page = totalItem / ITEM_PER_PAGE + 1;

        /*
        get the keyword/query to search photos from the server
        GalleryActivity: line-46 --> set a new keyword/query for searching
        GalleryFragment line 328 --> set the keyword/query to null
         */
        String query = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getString(UrlManager.PREF_SEARCH_QUERY, null);

        /*
        generate the query string with parameter:
            String query is keyword to search with, set in line-39 GalleryActivity
         */
        String url = UrlManager.getInstance().getItemUrl(query, page);
        Log.d(TAG, "line 188: keyword is "+ query+", page is "+page+", url is :"+url);
        /*
         public JsonObjectRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener) {
         */
        JsonObjectRequest request = new JsonObjectRequest(url,
                //the request is under process
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.d(TAG, "line 197 onResponse " + response);
                        //an list of photos needs to be added into RecyclerView
                        List<GalleryItem> result = new ArrayList<>();
                        try {
                            //"photos" contains an array of photos returned from server
                            JSONObject photos = response.getJSONObject("photos");
                            if (photos.getInt("pages") == page) {
                                mHasMore = false;
                            }
                            //get all photos from "photos" which is an array of photos
                            JSONArray photoArr = photos.getJSONArray("photo");
                            for (int i = 0; i < photoArr.length(); i++) {
                                //get a single phone named "itemObj"
                                JSONObject itemObj = photoArr.getJSONObject(i);
                                //new a GalleryItem obj by columns in "itemObj"(the information of a photo in json format)
                                GalleryItem item = new GalleryItem(
                                        itemObj.getString("id"),
                                        itemObj.getString("secret"),
                                        itemObj.getString("server"),
                                        itemObj.getString("farm")
                                );
                                result.add(item);
                            }
                        } catch (JSONException e) {

                        }
                        Log.d(TAG,"line 223:"+mAdapter.getItemCount());
                        mAdapter.addAll(result);
                        /*
                        function of BaseAdapter
                        Notifies the attached observers that the underlying data has been changed and any View reflecting the data set should refresh itself.
                         */
                        mAdapter.notifyDataSetChanged();
                        Log.d(TAG, "line 230:" + mAdapter.getItemCount());
                        mLoading = false;
                        //notify that SwipeFreshLayout stops refresh
                        mCustomSwipeRefreshLayout.refreshComplete();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        //set the tag of request here, delete requests with TAG in function 'stopLoading()'
        request.setTag(TAG);
        // add the request to the queue
        mRq.add(request);
    }

    /*
    This function is called when a new keyword needs to be searched
    1. clear the data already in Adapter
    2. load the new data into RecyclerView
     */
    public void refresh(){
        mAdapter.clear();
        startLoading();
    }

    /**
     * Cancels all requests in this queue with the given tag. Tag must be non-null
     * and equality is by identity.
     */
    private void stopLoading() {
        if (mRq != null) {
            mRq.cancelAll(TAG);
        }
    }

    @Override
    /*
    set (Searchability meta-data), obtained from SearchInfo specified by ComponentName, for searchView
    1. inflate Menu which contains the SearchView
    2. get SearchView from Menu by ID
    3. get SearchManager
    4. get ComponentName from getActivity()/FragmentActivity
    5. get SearchInfo(Searchability meta-data) by ComponentName
    6. set SearchInfo into SearchView

    Addition:
    provide the search suggestions/search history. Coding in 'if mSearchView != null {}' block -- line 291
     */
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //line-111 set the hasOptionsMenu as true
        inflater.inflate(R.menu.search_bar, menu);

        //get the searchView, casting from searchView
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();

        if (mSearchView != null) {
            /**
             * Sets a listener to inform when a suggestion is focused or clicked.
             * @param listener: the listener to inform of suggestion selection events.
             */
            mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int position) {

                    String suggestion = getSuggestion(position);

                    if (mSearchView != null && suggestion != null) {
                        mSearchView.setQuery(suggestion, true);
                    }

                    return true;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    String suggestion = getSuggestion(position);

                    if (mSearchView != null&&suggestion!=null)
                        mSearchView.setQuery(suggestion,true);

                    return true;
                }

                private String getSuggestion(int position) {
                    String suggest = null;
                    if (mSearchView != null) {
                        Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                        suggest = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                    }

                    return suggest;
                }
            });
        }
        //initialize the searchManager by Context.SEARCH_SERVICE
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        //getComponentName(): Returns the complete component name for this activity
        ComponentName name = getActivity().getComponentName();
        /*
        See: SearchManager#getSearchableInfo(ComponentName)
        SearchableInfo: Searchability meta-data for an activity. Only applications that *(search other applications)* should need to use this class.
         */
        SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
        //set the searchInfo(Searchability meta-data) for SearchView
        mSearchView.setSearchableInfo(searchInfo);
    }

    /*
    super.onOptionsItemSelected: Return false to allow normal menu processing to proceed, true to consume it here
    Param: MenuItem item is the selected item
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean selectionHandled = false;
        //to check which item is selected by its R.id
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                selectionHandled = true;
                break;
            case R.id.menu_item_move:
                if(mRecyclerView != null) {
                    //go back to the top of RecyclerView
                    mRecyclerView.smoothScrollToPosition(0);
                }
                selectionHandled = true;
                break;
            case R.id.menu_item_clear:

                /*
                clear the suggestion history

                 * Although provider utility classes are typically static, this one must be constructed
                 * because it needs to be initialized using the same values that you provided in your
                 * {@link android.content.SearchRecentSuggestionsProvider}.
                 *
                 * @param authority This must match the authority that you've declared in your manifest.
                 * @param mode You can use mode flags here to determine certain functional aspects of your
                 * database.  Note, this value should not change from run to run, because when it does change,
                 * your suggestions database may be wiped.
                 */
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
                suggestions.clearHistory();

                if(mSearchView != null) {
                    mSearchView.setQuery("", false);
                    mSearchView.setIconified(false);
                }

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(UrlManager.PREF_SEARCH_QUERY, null)
                        .commit();
                refresh();
                selectionHandled = true;
                break;
            default:
                selectionHandled = super.onOptionsItemSelected(item);
                break;
        }
        return selectionHandled;
    }
}
