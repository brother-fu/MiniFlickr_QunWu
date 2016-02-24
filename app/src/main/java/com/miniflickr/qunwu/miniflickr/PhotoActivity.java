package com.miniflickr.qunwu.miniflickr;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if(fragment == null){
            fragment = new PhotoFragment();
            /*
            beginTransaction():
            Start a series of edit operations on the Fragments associated with this FragmentManager.
             */
            fm.beginTransaction()
                /**
                 * add():
                 * Add a fragment to the activity state.
                 * This fragment may optionally also have its view (if {@link Fragment#onCreateView Fragment.onCreateView}
                 * returns non-null) into a container view of the activity.
                 * ( the container is FragmentLayout --> id is fragmentContainer)
                 * */
                    .add(R.id.fragmentContainer,fragment)
                    .commit();
        }
    }
}
