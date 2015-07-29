package com.antrromet.insomnia.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.antrromet.insomnia.Constants;
import com.antrromet.insomnia.InstagramFullScreenActivity;
import com.antrromet.insomnia.R;
import com.antrromet.insomnia.WebViewActivity;
import com.antrromet.insomnia.adapters.InstagramRecyclerAdapter;
import com.antrromet.insomnia.interfaces.OnVolleyResponseListener;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.utils.JSONUtils;
import com.antrromet.insomnia.utils.Logger;
import com.antrromet.insomnia.utils.PreferencesManager;
import com.antrromet.insomnia.widgets.EndlessRecyclerOnScrollListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class InstagramFragment extends BaseFragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, OnVolleyResponseListener, LoaderManager
                .LoaderCallbacks<Cursor>, InstagramRecyclerAdapter.OnItemClickListener,
        InstagramRecyclerAdapter.OnItemLongClickListener {

    private static final String TAG = InstagramFragment.class.getSimpleName();
    private static final String INSTAGRAM_PKG_NAME = "com.instagram.android";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Button mLoginButton;
    private InstagramRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private String mAccessToken;
    private boolean mFetchDataAgain;
    private boolean mIsManualRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_instagram, null);

        // Setting up the Refresh Layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_grey, R.color
                        .swipe_refresh_grey, R.color.swipe_refresh_grey,
                R.color.swipe_refresh_grey);

        // Setting up the Instagram login button
        mLoginButton = (Button) view.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);

        // Setting up the Recycler View
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new InstagramRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                loadMore();
            }
        });

        setVolleyListener(this);
        mAccessToken = PreferencesManager.getString(getActivity(), Constants.APP_PREFERENCES,
                Constants.SharedPreferenceKeys.INSTAGRAM_ACCESS_TOKEN);
        if (TextUtils.isEmpty(mAccessToken)) {
            mLoginButton.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mLoginButton.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);

            // Loading data from the cache
            getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders.INSTAGRAM_FEEDS
                    .id, null, this);

            requestFeeds(null);
        }

        return view;
    }

    private void loadMore() {
        String nextMaxId = PreferencesManager.getString(getActivity(), Constants.APP_PREFERENCES,
                Constants.SharedPreferenceKeys.INSTAGRAM_NEXT_MAX_ID, null);
        requestFeeds(nextMaxId);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            startActivityForResult(new Intent(getActivity(), WebViewActivity.class).putExtra
                    ("link", String.format(Constants.Urls.INSTAGRAM_LOGIN.link, getString(R
                            .string.instagram_client_id), getString(R.string
                            .instagram_redirect_uri))).putExtra
                    ("title", getString(R.string.instagram_login_title)), 101);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == Activity.RESULT_FIRST_USER) {
            mAccessToken = PreferencesManager.getString(getActivity(), Constants
                    .APP_PREFERENCES, Constants.SharedPreferenceKeys.INSTAGRAM_ACCESS_TOKEN);
            if (TextUtils.isEmpty(mAccessToken)) {
                showToast(getString(R.string.instagram_failed_login));
            } else {
                mLoginButton.setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                requestFeeds(null);
            }
        } else if (requestCode == 100 && data != null) {
            mRecyclerView.scrollToPosition(data.getIntExtra("position", mLayoutManager
                    .findFirstVisibleItemPosition()));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Fetches the feed for the logged in user
     */
    private void requestFeeds(String maxId) {
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setRefreshing(true);
        if (TextUtils.isEmpty(maxId)) {
            requestVolley(Constants.VolleyTags.INSTAGRAM_FEEDS, Request.Method.GET, String.format
                    (Constants.Urls.INSTAGRAM_FEEDS.link, mAccessToken), null, null);
        } else {
            requestVolley(Constants.VolleyTags.INSTAGRAM_FEEDS_LOAD_MORE, Request.Method.GET,
                    String.format(Constants.Urls.INSTAGRAM_FEEDS_LOAD_MORE.link, mAccessToken, maxId)
                    , null, null);
        }

    }

    @Override
    public void onRefresh() {
        if (isNetworkAvailable()) {
            mIsManualRefresh = true;
            requestFeeds(null);
        }
    }

    @Override
    public void OnSuccess(Constants.VolleyTags tag, Object responseObject) {
        if (getActivity() != null) {
            // Stop the animation and insert the data into DB
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(false);
            if (tag == Constants.VolleyTags.INSTAGRAM_FEEDS || tag == Constants.VolleyTags
                    .INSTAGRAM_FEEDS_LOAD_MORE) {
                if (responseObject != null) {
                    insertFeedsInDb((JSONObject) responseObject, tag.equals(Constants.VolleyTags.INSTAGRAM_FEEDS_LOAD_MORE));
                    getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders
                            .INSTAGRAM_FEEDS.id, null, this);
                }
            }
        }
    }

    @Override
    public void OnError(Constants.VolleyTags tag, VolleyError error) {
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Inserts the response in the DB
     *
     * @param responseObject jsonObject response that we get from the server
     */
    private void insertFeedsInDb(JSONObject responseObject, boolean isLoadMore) {
        // Parsing through the feeds
        JSONArray feedsArray = JSONUtils.optJSONArray(responseObject, Constants.ApiKeys.DATA
                .key);
        if (feedsArray != null) {
            ContentValues[] values = new ContentValues[feedsArray.length()];
            // ids is used to check the cache and to check if the cache is to be cleared
            String[] ids = new String[feedsArray.length()];
            for (int i = 0; i < feedsArray.length(); i++) {
                JSONObject feedObject = JSONUtils.optJSONObject(feedsArray, i);
                if (feedObject != null) {
                    ContentValues value = new ContentValues();
                    value.put(DBOpenHelper.COLUMN_TYPE, JSONUtils.optString(feedObject,
                            Constants.ApiKeys.TYPE.key));
                    value.put(DBOpenHelper.COLUMN_CREATED_TIME, Long.valueOf(JSONUtils.optString
                            (feedObject, Constants.ApiKeys.CREATED_TIME.key)));
                    value.put(DBOpenHelper.COLUMN_LINK, JSONUtils.optString(feedObject, Constants
                            .ApiKeys.LINK.key));
                    JSONObject likesObject = JSONUtils.optJSONObject(feedObject, Constants.ApiKeys
                            .LIKES.key);
                    if (likesObject != null) {
                        value.put(DBOpenHelper.COLUMN_LIKES_COUNT
                                , JSONUtils.optInt(likesObject, Constants.ApiKeys.COUNT.key));
                    }
                    JSONObject imagesObject = JSONUtils.optJSONObject(feedObject, Constants.ApiKeys
                            .IMAGES.key);
                    if (imagesObject != null) {
                        JSONObject standardResolutionObject = JSONUtils.optJSONObject(imagesObject,
                                Constants.ApiKeys.STANDARD_RESOLUTION.key);
                        if (standardResolutionObject != null) {
                            value.put(DBOpenHelper.COLUMN_STANDARD_RES_IMAGE
                                    , JSONUtils.optString(standardResolutionObject, Constants
                                    .ApiKeys.URL.key));
                        }
                    }
                    JSONObject captionObject = JSONUtils.optJSONObject(feedObject, Constants.ApiKeys
                            .CAPTION.key);
                    if (captionObject != null) {
                        value.put(DBOpenHelper.COLUMN_CAPTION, JSONUtils.optString(captionObject,
                                Constants.ApiKeys.TEXT.key));
                    }
                    value.put(DBOpenHelper.COLUMN_ID, JSONUtils.optString(feedObject, Constants
                            .ApiKeys.ID.key));
                    ids[i] = value.getAsString(DBOpenHelper.COLUMN_ID);
                    JSONObject userObject = JSONUtils.optJSONObject(feedObject, Constants.ApiKeys
                            .USER.key);
                    if (userObject != null) {
                        value.put(DBOpenHelper.COLUMN_USERNAME, JSONUtils.optString(userObject,
                                Constants.ApiKeys.USERNAME.key));
                        value.put(DBOpenHelper.COLUMN_FULL_NAME, JSONUtils.optString(userObject,
                                Constants.ApiKeys.FULL_NAME.key));
                    }
                    values[i] = value;
                }
            }

            // Storing the next paging id in Preferences
            JSONObject paginationObject = JSONUtils.optJSONObject(responseObject, Constants.ApiKeys
                    .PAGINATION.key);
            if (paginationObject != null) {
                String newNextMaxId = JSONUtils.optString(paginationObject, Constants.ApiKeys
                        .NEXT_MAX_ID
                        .key);

                String oldNextMaxId = PreferencesManager.getString(getActivity(), Constants
                        .APP_PREFERENCES, Constants.SharedPreferenceKeys.INSTAGRAM_NEXT_MAX_ID);
                checkCache(ids, oldNextMaxId, newNextMaxId, isLoadMore);
                // Insert in the cache
                getActivity().getContentResolver().bulkInsert(DBProvider.URI_INSTAGRAM, values);

                PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                        .SharedPreferenceKeys.INSTAGRAM_NEXT_MAX_ID, newNextMaxId);
                if (mFetchDataAgain) {
                    Logger.e(TAG, "Auto fetching next set of data!");
                    requestFeeds(newNextMaxId);
                }
            }
            mIsManualRefresh = false;
        }

    }

    /**
     * Check if the data in DB is too old. If yes then there wont be any matching id in the DB
     * and hence we need to delete the old data.
     * Otherwise the timeline time would be messed up, since on load more will be called after
     * fresh data that is going to be added and the previous old data.
     */
    private void checkCache(String[] ids, String oldNextMaxId, String newNextMaxId, boolean
            isLoadMore) {
        Cursor cursor = getActivity().getContentResolver().query(DBProvider.URI_INSTAGRAM, new
                String[]{DBOpenHelper.COLUMN_ID}, DBOpenHelper.COLUMN_ID + " in (" +
                makePlaceholders(ids.length) + ")", ids, null);
        boolean isDataIntersecting = false;
        mFetchDataAgain = false;
        if (!mIsManualRefresh && cursor != null) {
            int count = cursor.getCount();
            if (count > 0) {
                isDataIntersecting = true;
                if (oldNextMaxId != null && newNextMaxId != null && !oldNextMaxId.equals
                        (newNextMaxId)) {
                    mFetchDataAgain = true;
                }
            }
            cursor.close();
        }
        if (!mIsManualRefresh && !isDataIntersecting && !isLoadMore) {
            //Clear the DB
            Logger.e(TAG, "Clearing the Instagram cache");
            getActivity().getContentResolver().delete(DBProvider.URI_INSTAGRAM, null, null);
            PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                    .SharedPreferenceKeys.INSTAGRAM_NEXT_MAX_ID, null);
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
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getActivity() == null) {
            return null;
        }
        // Load with the latest feed on the top
        return new CursorLoader(getActivity(), DBProvider.URI_INSTAGRAM, null, null, null,
                DBOpenHelper.COLUMN_CREATED_TIME + " desc");
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (getActivity() != null) {
            // Update the cursor in the adapter
            mAdapter.setCursor(data);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(View view, int position, String id) {
        if (getActivity() != null) {
            startActivityForResult(new Intent(getActivity(), InstagramFullScreenActivity.class)
                    .putExtra("position", position), 100);
        }
    }

    @Override
    public void onItemLongClick(View view, String link) {
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(INSTAGRAM_PKG_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            startActivity(new Intent(getActivity(), WebViewActivity.class).putExtra("link", link)
                    .putExtra("title", getString(R.string.instagram_post)));
        } finally {
            Intent intent = pm.getLaunchIntentForPackage(INSTAGRAM_PKG_NAME);
            intent.setComponent(new ComponentName(INSTAGRAM_PKG_NAME, "com.instagram.android.activity.UrlHandlerActivity"));
            intent.setData(Uri.parse(link));
            startActivity(intent);
        }
    }

    /**
     * Is called when clicked on tab the second time, when the user is already present in the tab
     */
    public void onTabClicked() {
        mLayoutManager.scrollToPositionWithOffset(0, 0);
    }
}
