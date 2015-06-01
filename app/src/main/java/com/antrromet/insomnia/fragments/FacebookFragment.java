package com.antrromet.insomnia.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antrromet.insomnia.Constants;
import com.antrromet.insomnia.R;
import com.antrromet.insomnia.adapters.NineGagRecyclerAdapter;
import com.antrromet.insomnia.utils.Logger;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class FacebookFragment extends BaseFragment implements LoaderManager
        .LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FacebookFragment.class.getSimpleName();
    private NineGagRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken;
    private AccessTokenTracker mAccessTokenTracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

        View view = inflater.inflate(R.layout.fragment_facebook, null);

        mLoginButton = (LoginButton) view.findViewById(R.id.login_button);

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
        mAdapter = new NineGagRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        // Setup facebook login
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

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // App code
            }
        };

        return view;
    }

    private void requestFeed() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,message,picture,link");
        new GraphRequest(
                mAccessToken,
                "/me/home",
                parameters,
                HttpMethod.GET, new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse graphResponse) {
                showToast("Completed");
                Logger.d(TAG, graphResponse.getRawResponse());
            }
        }).executeAsync();
    }

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
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAccessTokenTracker.stopTracking();
    }
}
