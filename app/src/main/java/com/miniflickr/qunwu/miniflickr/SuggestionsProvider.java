package com.miniflickr.qunwu.miniflickr;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Owner on 2/22/2016.
 */

/* SearchRecentSuggestionsProvider: This superclass can be used to create a simple search suggestions provider for your application.
   It creates suggestions (as the user types) based on recent queries and/or recent views.

   -->SuggestionsProvider/SearchRecentSuggestionsProvider/ContentProvider are actually databases at local
        * */
public class SuggestionsProvider extends SearchRecentSuggestionsProvider {
    /*
    xml/searchable_input.xml, this files defines the real searchSuggestAuthority, so the SuggestionsProvider.AUTHORITY must be same with it

    Both of these two Authority must be --> {domain name of project} + {sub package name of the provider class} + {class name of provider}
        ---> so we can identify an unique class path of provider class
     */
    public static final String AUTHORITY = "com.miniflickr.qunwu.miniflickr."+SuggestionsProvider.class.getSimpleName();

    public static final int MODE = DATABASE_MODE_QUERIES;

    public SuggestionsProvider(){
        /**
         * In order to use {SearchRecentSuggestionsProvider} class, you must extend it, and call this setup function from your
         * constructor.  In your application or activities, you must provide the 'same values' when
         * you create the {@link android.provider.SearchRecentSuggestions} helper.
         *
         * @param authority This must match the authority that you've declared in your manifest.
         * @param mode You can use mode flags here to determine certain functional aspects of your
         * database.  Note, this value should not change from run to run, because when it does change,
         * your suggestions database may be wiped.
         *
         * @see #DATABASE_MODE_QUERIES
         * @see #DATABASE_MODE_2LINES
         */
        setupSuggestions(SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
    }
}
