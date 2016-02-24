package com.miniflickr.qunwu.miniflickr;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.miniflickr.qunwu.miniflickr.entity.GalleryItem;
import com.miniflickr.qunwu.miniflickr.util.UrlManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Qun Wu on 2016/2/22.
 */
public class PhotoFragment extends Fragment {
    public static final String TAG = PhotoFragment.class.getSimpleName();

    private ProgressBar mProgressBar;
    private TextView mDescText;
    private ImageView mPhoto;

    private GalleryItem mItem;
    private RequestQueue mRq;
    private DownloadManager mDownloadManager;

    private boolean mLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        /**
         * Control whether a fragment instance is retained across Activity re-creation (such as from a configuration change).
         * This can only be used with fragments *(not)* in the back stack.
         * If set, the fragment , lifecycle will be slightly different when an activity is recreated:
         * */
        setRetainInstance(true);
    }

    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_photo_fragment, container, false);

        mItem = (GalleryItem)getActivity().getIntent().getSerializableExtra("item");

        mDownloadManager = (DownloadManager)getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
        mRq = Volley.newRequestQueue(getActivity());

        mProgressBar=(ProgressBar)view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        //mDescText = (TextView)view.findViewById(R.id.desc_text);
        mPhoto = (ImageView)view.findViewById(R.id.photo);

        /*
        Load a photo including its information
        1. load the photo itself into ImageView --> Glide, it's fast
        2. load the photo information into TextView --> StartLoading():JsonRequest/Response, it's slower than Glide
         */
        Log.d(TAG,"78:"+mItem.getUrl());
        Glide.with(this).
                load(mItem.getUrl()).
                thumbnail(0.5f).
                into(mPhoto);

        //download
        LinearLayout downloadView = (LinearLayout)view.findViewById(R.id.download);
        downloadView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                downloadPhoto();
                Toast.makeText(getActivity(),"Download Finished",Toast.LENGTH_LONG).show();
            }
        });

        //open in app
        LinearLayout openView = (LinearLayout)view.findViewById(R.id.open);
        openView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openApp();
            }
        });

        startLoading();
        return view;
    }

    //function to handle download process
    private void downloadPhoto(){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mItem.getUrl()));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("SharkFeed Download");
        request.setDescription(mItem.getUrl());
        mDownloadManager.enqueue(request);
    }

    private void openApp(){
        String url = UrlManager.getInstance().getFlickrUrl(mItem.getId());
        //url is the official website of Flickr App
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void startLoading(){
        String url = UrlManager.getInstance().getPhotoInfoUrl(mItem.getId());
        Log.d(TAG,"startLoading:"+url);
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            Log.d(TAG,"startdLoading: photo received ");
                            JSONObject photo = response.getJSONObject("photo");
                            JSONObject descObj = photo.getJSONObject("title");
                            String desc = descObj.getString("_content");
                            Log.d(TAG,"line one37:"+desc);
                            //mDescText.setText(desc);
                        }catch (JSONException e){
                            if (e!=null)
                                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                        }

                        mProgressBar.setVisibility(View.GONE);
                        mLoading=false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,"Error in StartLoading");
                    }
                });
        request.setTag(TAG);
        mRq.add(request);
    }

    private void stopLoading(){
        if(mRq != null)
            mRq.cancelAll(TAG);
    }

    //stop downloading when fragment is stopped
    @Override
    public void onStop(){
        super.onStop();;
        stopLoading();
    }
}
