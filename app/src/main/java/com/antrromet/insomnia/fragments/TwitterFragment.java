package com.antrromet.insomnia.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.antrromet.insomnia.Constants;
import com.antrromet.insomnia.R;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.List;

public class TwitterFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int TWEET_COUNT = 20;
    private TwitterLoginButton mLoginButton;
    private TwitterApiClient mTwitterApiClient;
    private boolean mInitialFetch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_twitter, null);

        mLoginButton = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button);
        mLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                fetchTweets(false, 0, 0);
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

        mTwitterApiClient = TwitterCore.getInstance().getApiClient();
        if (mTwitterApiClient == null) {
            mLoginButton.setVisibility(View.VISIBLE);
        } else {
            mLoginButton.setVisibility(View.GONE);
            getActivity().getSupportLoaderManager().restartLoader(Constants.Loaders.TWITTER_FEEDS
                    .id, null, this);
            mInitialFetch = true;
        }


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Is called when clicked on tab the second time, when the user is already present in the tab
     */
    public void onTabClicked() {
    }

    private void fetchTweets(boolean isLoadMore, long maxId, long sinceId) {
        StatusesService statusesService = mTwitterApiClient.getStatusesService();
        statusesService.homeTimeline(TWEET_COUNT, sinceId, maxId, false, true, true, true, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {

            }

            @Override
            public void failure(TwitterException e) {

            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getActivity() == null) {
            return null;
        }
        // Load with the latest feed on the top
        return new CursorLoader(getActivity(), DBProvider.URI_INSTAGRAM, null, null, null,
                DBOpenHelper.COLUMN_ID + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
