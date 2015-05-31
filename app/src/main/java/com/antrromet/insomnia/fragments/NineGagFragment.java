package com.antrromet.insomnia.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.antrromet.insomnia.Application;
import com.antrromet.insomnia.Constants;
import com.antrromet.insomnia.Constants.Loaders;
import com.antrromet.insomnia.R;
import com.antrromet.insomnia.adapters.NineGagRecyclerAdapter;
import com.antrromet.insomnia.interfaces.OnVolleyResponseListener;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.utils.JSONUtils;
import com.antrromet.insomnia.utils.Logger;
import com.antrromet.insomnia.utils.PreferencesManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class NineGagFragment extends BaseFragment implements OnVolleyResponseListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = NineGagFragment.class.getSimpleName();
    private NineGagRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_nine_gag, null);

        // Setting up the Refresh Layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_blue, R.color
                        .swipe_refresh_grey, R.color.swipe_refresh_blue,
                R.color.swipe_refresh_grey);

        // Setting up the Recycler View
        mRecyclerView = (RecyclerView) view.findViewById(R.id.nine_gag_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new NineGagRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // Loading data from the cache
        getActivity().getSupportLoaderManager().restartLoader(Loaders.NINE_GAG_FEEDS.id, null,
                this);

        // Requesting new data
        setVolleyListener(this);
        requestNewFeeds(String.valueOf(0));

        return view;
    }

    /**
     * Call the 9Gag feeds API
     */
    private void requestNewFeeds(String pagingId) {

        // Adding request to request queue only if network is available else show the error to
        // the user
        if (isNetworkAvailable()) {
            // Start refreshing animation
            mSwipeRefreshLayout.setEnabled(false);
            mSwipeRefreshLayout.setRefreshing(true);
            requestVolley(Constants.VolleyTags.NINE_GAG_FEEDS, Request.Method.GET, String.format
                    (Constants.Urls.NINE_GAG_FEEDS.link, pagingId), null, null);
        }

    }

    @Override
    public void OnSuccess(Constants.VolleyTags tag, Object responseObject) {
        if (getActivity() != null) {
            // Stop the animation and insert the data into DB
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
            if (tag == Constants.VolleyTags.NINE_GAG_FEEDS) {
                if (responseObject != null) {
                    insertFeedsInDb((JSONObject) responseObject);
                    getActivity().getSupportLoaderManager().restartLoader(Loaders.NINE_GAG_FEEDS
                            .id, null, this);
                }
            }
        }
    }

    /**
     * Inserts the response in the DB
     *
     * @param responseObject jsonObject response that we get from the server
     */
    private void insertFeedsInDb(JSONObject responseObject) {
        // Parsing through the feeds
        JSONArray feedsArray = JSONUtils.optJSONArray(responseObject, Constants.NineGagKeys.DATA
                .key);
        if (feedsArray != null) {
            ContentValues[] values = new ContentValues[feedsArray.length()];
            // ids is used to check the cache and to check if the cache is to be cleared
            String[] ids = new String[feedsArray.length()];
            for (int i = feedsArray.length(); i >= 0; i--) {
                JSONObject feedObject = JSONUtils.optJSONObject(feedsArray, i);
                if (feedObject != null) {
                    ContentValues value = new ContentValues();
                    value.put(DBOpenHelper.COLUMN_ID, JSONUtils.optString(feedObject, Constants
                            .NineGagKeys.ID.key));
                    ids[i] = value.getAsString(DBOpenHelper.COLUMN_ID);
                    value.put(DBOpenHelper.COLUMN_CAPTION, JSONUtils.optString(feedObject,
                            Constants.NineGagKeys.CAPTION.key));
                    JSONObject imagesObject = JSONUtils.optJSONObject(feedObject, Constants
                            .NineGagKeys.IMAGES.key);
                    if (imagesObject != null) {
                        value.put(DBOpenHelper.COLUMN_IMAGE_NORMAL, JSONUtils.optString
                                (imagesObject, Constants.NineGagKeys.NORMAL.key));
                        value.put(DBOpenHelper.COLUMN_IMAGE_LARGE, JSONUtils.optString
                                (imagesObject, Constants.NineGagKeys.LARGE.key));
                    }
                    value.put(DBOpenHelper.COLUMN_LINK, JSONUtils.optString(feedObject, Constants
                            .NineGagKeys.LINK.key));
                    JSONObject votesObject = JSONUtils.optJSONObject(feedObject, Constants
                            .NineGagKeys.VOTES.key);
                    if (imagesObject != null) {
                        value.put(DBOpenHelper.COLUMN_VOTES_COUNT, JSONUtils.optInt(votesObject,
                                Constants.NineGagKeys.COUNT.key));
                    }
                    values[i] = value;
                }
            }

            // Check for the deletion of cache only when its a new first time request
            String nextPageId = PreferencesManager.getString(getActivity(), Constants
                    .APP_PREFERENCES, Constants.SharedPreferenceKeys.NINE_GAG_NEXT_PAGE_ID);
            if (TextUtils.isEmpty(nextPageId)) {
                checkCache(ids);
            }
            // Insert in the cache
            getActivity().getContentResolver().bulkInsert(DBProvider.URI_NINE_GAG, values);

            // Storing the next paging id in Preferences
            JSONObject pagingObject = JSONUtils.optJSONObject(responseObject, Constants.NineGagKeys
                    .PAGING.key);
            if (pagingObject != null) {
                nextPageId = JSONUtils.optString(pagingObject, Constants.NineGagKeys.NEXT.key);
                PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                        .SharedPreferenceKeys.NINE_GAG_NEXT_PAGE_ID, nextPageId);
            }
        }

    }

    /**
     * Check if the data in DB is too old. If yes then there wont be any matching id in the DB
     * and hence we need to delete the old data.
     * Otherwise the timeline time would be messed up, since on load more will be called after
     * fresh data that is going to be added and the previous old data.
     */
    private void checkCache(String[] ids) {
        Cursor cursor = getActivity().getContentResolver().query(DBProvider.URI_NINE_GAG, new
                String[]{DBOpenHelper.COLUMN_ID}, DBOpenHelper.COLUMN_ID + " in (" +
                makePlaceholders(ids.length) + ")", ids, null);
        boolean isDataIntersecting = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                isDataIntersecting = true;
            }
            cursor.close();
        }
        if (!isDataIntersecting) {
            //Clear the DB
            Logger.e(TAG, "Clearing the 9Gag cache");
            getActivity().getContentResolver().delete(DBProvider.URI_NINE_GAG, null, null);
        }
    }

    /**
     * Creates the placeholder string that looks something like ?, ?, ?...
     * Used for quering the DB while clearing cache
     *
     * @param len num of placeholders
     * @return the placeholder string
     */
    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    @Override
    public void OnError(Constants.VolleyTags tag, VolleyError error) {
        if (tag == Constants.VolleyTags.NINE_GAG_FEEDS) {
            // Stopping the animation
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        // Not showing Toast message because that's already handled in the base classes
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getActivity() == null) {
            return null;
        }
        // Load with the latest feed on the top
        return new CursorLoader(getActivity(), DBProvider.URI_NINE_GAG, null, null, null,
                DBOpenHelper.COLUMN_INSERTION_TIME + " desc");
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        if (getActivity() != null) {
            // Update the cursor in the adapter
            mAdapter.setCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    /**
     * Is called when clicked on tab the second time, when the user is already present in the tab
     */
    public void onTabClicked() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void onRefresh() {
        requestNewFeeds(String.valueOf(0));
    }

    private void onLoadMore(){
        String nextPageId = PreferencesManager.getString(getActivity(), Constants.APP_PREFERENCES,
                Constants.SharedPreferenceKeys.NINE_GAG_NEXT_PAGE_ID, String.valueOf(0));
        requestNewFeeds(TextUtils.isEmpty(nextPageId) ? String.valueOf(0) : nextPageId);
    }

    @Override
    public void onDestroyView() {
        Application.cancelPendingRequests(Constants.VolleyTags.NINE_GAG_FEEDS);
        // Setting the next page id to null when this fragment is destroyed. This would typically
        // be called whent the app is closed
        PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                .SharedPreferenceKeys.NINE_GAG_NEXT_PAGE_ID, null);
        super.onDestroyView();
    }
}
