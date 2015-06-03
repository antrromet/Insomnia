package com.antrromet.insomnia.fragments;

import android.content.ContentValues;
import android.content.Intent;
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

import com.antrromet.insomnia.Constants;
import com.antrromet.insomnia.R;
import com.antrromet.insomnia.adapters.FacebookRecyclerAdapter;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.utils.JSONUtils;
import com.antrromet.insomnia.utils.Logger;
import com.antrromet.insomnia.utils.PreferencesManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class FacebookFragment extends BaseFragment implements LoaderManager
        .LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FacebookFragment.class.getSimpleName();
    private FacebookRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        View view = View.inflate(getActivity(), R.layout.fragment_facebook, null);

        // Setting up the Refresh Layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_blue, R.color
                        .swipe_refresh_grey, R.color.swipe_refresh_blue,
                R.color.swipe_refresh_grey);

        // Setting up the Recycler View
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FacebookRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // Setup facebook login
        final LoginButton mLoginButton = (LoginButton) view.findViewById(R.id.login_button);
        mCallbackManager = CallbackManager.Factory.create();
        if (isFacebookLoggedIn()) {
            mLoginButton.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            // Loading data from the cache
            getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders.FACEBOOK_FEEDS.id,
                    null, this);
            requestFeed();
        } else {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            mLoginButton.setVisibility(View.VISIBLE);
            mLoginButton.setReadPermissions("read_stream");
            mLoginButton.setFragment(this);
            // Callback registration
            mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    showToast(R.string.successfully_logged_in);
                    mAccessToken = loginResult.getAccessToken();
                    mLoginButton.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    requestFeed();
                }

                @Override
                public void onCancel() {
                    // App code
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                }
            });
        }

        return view;
    }

    /**
     * Request the required info from the FB graph API
     */
    private void requestFeed() {
        if (isNetworkAvailable()) {
            mSwipeRefreshLayout.setEnabled(false);
            mSwipeRefreshLayout.setRefreshing(true);
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,from,to,message,full_picture,link,type,status_type");
            parameters.putString("date_format", "U");
            new GraphRequest(
                    mAccessToken,
                    "/me/home",
                    parameters,
                    HttpMethod.GET, new GraphRequest.Callback() {

                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    mSwipeRefreshLayout.setEnabled(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (getActivity() != null) {
                        Logger.d(TAG, graphResponse.getRawResponse());
                        JSONObject responseObject = graphResponse.getJSONObject();
                        if (responseObject != null) {
                            insertFeedsIntoDb(responseObject);
                            getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders
                                    .FACEBOOK_FEEDS.id, null, FacebookFragment.this);
                        }
                    }
                }
            }).executeAsync();
        }
    }

    /**
     * Insert the Facebook feeds in the Database
     */
    private void insertFeedsIntoDb(JSONObject responseObject) {
        // Parsing through the feeds
        JSONArray feedsArray = JSONUtils.optJSONArray(responseObject, Constants.ApiKeys.DATA
                .key);
        if (feedsArray != null) {
            ContentValues[] values = new ContentValues[feedsArray.length()];
            String[] ids = new String[feedsArray.length()];
            for (int i = feedsArray.length(); i >= 0; i--) {
                JSONObject feedObject = JSONUtils.optJSONObject(feedsArray, i);
                if (feedObject != null) {
                    ContentValues value = new ContentValues();
                    value.put(DBOpenHelper.COLUMN_ID, JSONUtils.optString(feedObject, Constants
                            .ApiKeys.ID.key));
                    ids[i] = value.getAsString(DBOpenHelper.COLUMN_ID);
                    JSONObject fromObject = JSONUtils.optJSONObject(feedObject, Constants.ApiKeys
                            .FROM.key);
                    if (fromObject != null) {
                        value.put(DBOpenHelper.COLUMN_FROM, JSONUtils.optString(fromObject,
                                Constants.ApiKeys.NAME.key));
                    }
                    value.put(DBOpenHelper.COLUMN_STATUS_TYPE, JSONUtils.optString(feedObject,
                            Constants.ApiKeys.STATUS_TYPE.key));
                    if (value.getAsString(DBOpenHelper.COLUMN_STATUS_TYPE) != null && value
                            .getAsString(DBOpenHelper.COLUMN_STATUS_TYPE).equals("wall_post")) {
                        JSONObject toObject = JSONUtils.optJSONObject(feedObject, Constants.ApiKeys
                                .TO.key);
                        if (toObject != null) {
                            JSONArray dataArray = JSONUtils.optJSONArray(toObject, Constants
                                    .ApiKeys.DATA.key);
                            if (dataArray != null) {
                                JSONObject firstObject = JSONUtils.optJSONObject(dataArray, 0);
                                if (firstObject != null) {
                                    value.put(DBOpenHelper.COLUMN_TO, JSONUtils.optString
                                            (firstObject, Constants.ApiKeys.NAME.key));
                                }
                            }
                        }
                    }
                    value.put(DBOpenHelper.COLUMN_MESSAGE, JSONUtils.optString(feedObject,
                            Constants.ApiKeys.MESSAGE.key));
                    value.put(DBOpenHelper.COLUMN_PICTURE, JSONUtils.optString
                            (feedObject, Constants.ApiKeys.FULL_PICTURE.key));
                    value.put(DBOpenHelper.COLUMN_TYPE, JSONUtils.optString(feedObject, Constants
                            .ApiKeys.TYPE.key));
                    value.put(DBOpenHelper.COLUMN_CREATED_TIME, JSONUtils.optLong(feedObject,
                            Constants.ApiKeys.CREATED_TIME.key));
                    values[i] = value;
                }
            }

            // Check for the deletion of cache only when its a new first time request
            String nextPageId = PreferencesManager.getString(getActivity(), Constants
                    .APP_PREFERENCES, Constants.SharedPreferenceKeys.FACEBOOK_AFTER_PAGE_ID);
            if (TextUtils.isEmpty(nextPageId)) {
                checkCache(ids);
            }

            getActivity().getContentResolver().bulkInsert(DBProvider.URI_FACEBOOK, values);

            // Storing the next paging id in Preferences
            JSONObject pagingObject = JSONUtils.optJSONObject(responseObject, Constants.ApiKeys
                    .PAGING.key);
            if (pagingObject != null) {
                JSONObject cursorsObject = JSONUtils.optJSONObject(responseObject, Constants.ApiKeys
                        .CURSORS.key);
                if (cursorsObject != null) {
                    String afterPageId = JSONUtils.optString(pagingObject, Constants.ApiKeys.NEXT
                            .key);
                    PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                            .SharedPreferenceKeys.FACEBOOK_AFTER_PAGE_ID, afterPageId);
                }
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
        Cursor cursor = getActivity().getContentResolver().query(DBProvider.URI_FACEBOOK, new
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
            Logger.e(TAG, "Clearing the Facebook cache");
            getActivity().getContentResolver().delete(DBProvider.URI_FACEBOOK, null, null);
            PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                    .SharedPreferenceKeys.FACEBOOK_AFTER_PAGE_ID, null);
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

    /**
     * Check if the user is already logged in
     *
     * @return true if logged in, false otherwise
     */
    private boolean isFacebookLoggedIn() {
        boolean isLoggedIn = false;
        mAccessToken = AccessToken.getCurrentAccessToken();
        if (mAccessToken == null) {
            Logger.d(TAG, ">>>" + "Signed Out");
        } else {
            isLoggedIn = true;
            Logger.d(TAG, ">>>" + "Signed In");
        }
        return isLoggedIn;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getActivity() == null) {
            return null;
        }
        // Load with the latest feed on the top
        return new CursorLoader(getActivity(), DBProvider.URI_FACEBOOK, null, null, null,
                DBOpenHelper.COLUMN_CREATED_TIME + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (getActivity() != null) {
            // Update the cursor in the adapter
            mAdapter.setCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onRefresh() {
        requestFeed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Is called when clicked on tab the second time, when the user is already present in the tab
     */
    public void onTabClicked() {
        mRecyclerView.smoothScrollToPosition(0);
    }

}
