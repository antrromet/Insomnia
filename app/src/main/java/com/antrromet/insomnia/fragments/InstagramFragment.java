package com.antrromet.insomnia.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.antrromet.insomnia.Constants;
import com.antrromet.insomnia.R;
import com.antrromet.insomnia.WebViewActivity;
import com.antrromet.insomnia.interfaces.OnVolleyResponseListener;
import com.antrromet.insomnia.utils.PreferencesManager;

public class InstagramFragment extends BaseFragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, OnVolleyResponseListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Button mLoginButton;

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
        String accessToken = PreferencesManager.getString(getActivity(), Constants.APP_PREFERENCES,
                Constants.SharedPreferenceKeys.INSTAGRAM_ACCESS_TOKEN);
        if (TextUtils.isEmpty(accessToken)) {
            mLoginButton.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setVisibility(View.GONE);
        } else {
            mLoginButton.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            requestFeeds(accessToken);
        }

        setVolleyListener(this);
        return view;
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
            String accessToken = PreferencesManager.getString(getActivity(), Constants
                    .APP_PREFERENCES, Constants.SharedPreferenceKeys.INSTAGRAM_ACCESS_TOKEN);
            if (TextUtils.isEmpty(accessToken)) {
                showToast(getString(R.string.instagram_failed_login));
            } else {
                mLoginButton.setVisibility(View.GONE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                requestFeeds(accessToken);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Fetches the feed for the logged in user
     */
    private void requestFeeds(String accessToken) {
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setRefreshing(true);
        requestVolley(Constants.VolleyTags.INSTAGRAM_FEEDS, Request.Method.GET, String
                .format(Constants
                        .Urls.INSTAGRAM_FEEDS.link, accessToken), null, null);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void OnSuccess(Constants.VolleyTags tag, Object responseObject) {
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void OnError(Constants.VolleyTags tag, VolleyError error) {
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
