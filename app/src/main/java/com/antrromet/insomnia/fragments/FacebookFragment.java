package com.antrromet.insomnia.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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
import com.antrromet.insomnia.WebViewActivity;
import com.antrromet.insomnia.adapters.FacebookRecyclerAdapter;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.utils.JSONUtils;
import com.antrromet.insomnia.utils.Logger;
import com.antrromet.insomnia.utils.PreferencesManager;
import com.antrromet.insomnia.widgets.EndlessRecyclerOnScrollListener;
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
        .LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, FacebookRecyclerAdapter
        .OnItemClickListener, FacebookRecyclerAdapter.OnItemLongClickListener {

    private static final String TAG = FacebookFragment.class.getSimpleName();
    private static final String FACEBOOK_PKG_NAME = "com.facebook.katana";
    private FacebookRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken;
    private LinearLayoutManager mLayoutManager;
    private boolean mFetchDataAgain;
    private boolean mIsManualRefresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        View view = View.inflate(getActivity(), R.layout.fragment_facebook, null);

        // Setting up the Refresh Layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_grey, R.color
                        .swipe_refresh_grey, R.color.swipe_refresh_grey,
                R.color.swipe_refresh_grey);

        // Setting up the Recycler View
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FacebookRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                requestFeed(true);
            }
        });
        mAdapter.setOnItemLongClickListener(this);
        mAdapter.setOnItemClickListener(this);

        // Setup facebook login
        final LoginButton mLoginButton = (LoginButton) view.findViewById(R.id.login_button);
        mCallbackManager = CallbackManager.Factory.create();
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
                requestFeed(false);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                showToast(R.string.facebook_failed_login);
            }
        });
        if (isFacebookLoggedIn()) {
            mLoginButton.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            // Loading data from the cache
            getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders.FACEBOOK_FEEDS.id,
                    null, this);
            requestFeed(false);
        } else {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            mLoginButton.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * Request the required info from the FB graph API
     */
    private void requestFeed(final boolean isNextPage) {
        if (isNetworkAvailable()) {
            mSwipeRefreshLayout.setEnabled(false);
            mSwipeRefreshLayout.setRefreshing(true);
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,from,to,message,full_picture,link,type,status_type");
            parameters.putString("date_format", "U");
            if (isNextPage) {
                String afterPageId = PreferencesManager.getString(getActivity(), Constants
                        .APP_PREFERENCES, Constants.SharedPreferenceKeys.FACEBOOK_AFTER_PAGE_ID);
                if (TextUtils.isEmpty(afterPageId)) {
                    // When the next page id is null simply return, there is nothing more to load
                    mSwipeRefreshLayout.setEnabled(false);
                    mSwipeRefreshLayout.setRefreshing(true);
                    showToast(getString(R.string.no_load_more));
                    return;
                } else {
                    parameters.putString("after", afterPageId);
                }
            }
            new GraphRequest(
                    mAccessToken,
                    "/me/home",
                    parameters,
                    HttpMethod.GET, new GraphRequest.Callback() {

                @Override
                public void onCompleted(GraphResponse graphResponse) {
                    mIsManualRefresh = false;
                    mSwipeRefreshLayout.setEnabled(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (getActivity() != null) {
                        Logger.d(TAG, graphResponse.getRawResponse());
                        if (graphResponse.getError() != null) {
                            Logger.e(TAG, graphResponse.getError().toString());
                            String error = graphResponse.getError().getErrorUserTitle() +
                                    graphResponse.getError().getErrorUserMessage();
                            if (TextUtils.isEmpty(error)) {
                                error = graphResponse
                                        .getError().getErrorMessage();
                            }
                            showToast(error);
                        } else {
                            JSONObject responseObject = graphResponse.getJSONObject();
                            if (responseObject != null) {
                                insertFeedsIntoDb(responseObject, isNextPage);
                                getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders
                                        .FACEBOOK_FEEDS.id, null, FacebookFragment.this);
                            }
                        }
                    }
                }
            }).executeAsync();
        }
    }

    /**
     * Insert the Facebook feeds in the Database
     */
    private void insertFeedsIntoDb(JSONObject responseObject, boolean isLoadMore) {
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

            JSONObject pagingObject = JSONUtils.optJSONObject(responseObject, Constants.ApiKeys
                    .PAGING.key);
            if (pagingObject != null) {
                JSONObject cursorsObject = JSONUtils.optJSONObject(pagingObject, Constants.ApiKeys
                        .CURSORS.key);
                if (cursorsObject != null) {
                    String newAfterPageId = JSONUtils.optString(cursorsObject, Constants.ApiKeys
                            .AFTER
                            .key);
                    String oldAfterPageId = PreferencesManager.getString(getActivity(), Constants
                            .APP_PREFERENCES, Constants.SharedPreferenceKeys.FACEBOOK_AFTER_PAGE_ID);
                    checkCache(ids, oldAfterPageId, newAfterPageId, isLoadMore);
                    // Insert in the cache
                    getActivity().getContentResolver().bulkInsert(DBProvider.URI_FACEBOOK, values);
                    PreferencesManager.set(getActivity(), Constants.APP_PREFERENCES, Constants
                            .SharedPreferenceKeys.FACEBOOK_AFTER_PAGE_ID, newAfterPageId);
                    if (mFetchDataAgain) {
                        Logger.e(TAG, "Auto fetching next set of data!");
                        requestFeed(true);
                    }
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
        if (isNetworkAvailable()) {
            mIsManualRefresh = true;
            requestFeed(false);
        }
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
        mLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    @Override
    public void onItemClick(View view, int position, String id) {
        Logger.d(TAG, "pos = " + position + " and id = " + id);
    }

    @Override
    public void onItemLongClick(View view, int position, String id) {
        // Launch the Facebook app or the webview activity
        String link = "https://www.facebook.com/" + id;
        PackageManager pm = getActivity().getPackageManager();
        try {
            pm.getPackageInfo(FACEBOOK_PKG_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            startActivity(new Intent(getActivity(), WebViewActivity.class).putExtra("link", link)
                    .putExtra("title", getString(R.string.facebook_post)));
        } finally {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        }
    }

}
