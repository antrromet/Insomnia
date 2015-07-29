package com.antrromet.insomnia;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.antrromet.insomnia.adapters.MainViewPagerAdapter;
import com.antrromet.insomnia.fragments.BaseFragment;
import com.antrromet.insomnia.fragments.InstagramFragment;
import com.antrromet.insomnia.fragments.NineGagFragment;
import com.antrromet.insomnia.fragments.TwitterFragment;
import com.antrromet.insomnia.interfaces.OnTabClickListener;
import com.antrromet.insomnia.widgets.SlidingTabLayout;
import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements OnTabClickListener {

    private MainViewPagerAdapter mViewPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tabbed_toolbar);
        setSupportActionBar(toolbar);

        // Setup the viewpager for the different tabs
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        // Setup the sliding tab layout
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.custom_tab, 0);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setOnTabClickListener(this);
        slidingTabLayout.setViewPager(mViewPager);

        TwitterAuthConfig authConfig =
                new TwitterAuthConfig("KHtayz7R82sBjJ2zXHu5IiFex",
                        "TDMHLT5L50dEGBni5b03EkDVFma5QbREYulHFTrkUDlbLd9jf0");
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
    }


    @Override
    public void OnTabClicked(int pos) {
        // The viewpager will be moved to the tab that the user clicks
        if (pos == mViewPager.getCurrentItem()) {
            // If the user is already present on the tab then move the content to the first
            // position, handled in onTabClicked()
            BaseFragment fragment = mViewPagerAdapter.getFragmentAt(pos);
            if (fragment != null) {
                switch (pos) {
                    case 0:
                        ((NineGagFragment) fragment).onTabClicked();
                        break;
                    case 1:
//                        ((ListFragment) fragment).onTabClicked();
                        break;
                    case 3:
                        ((InstagramFragment) fragment).onTabClicked();
                        break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the fragment, which will
        // then pass the result to the login button.
        TwitterFragment fragment = (TwitterFragment) mViewPagerAdapter.getFragmentAt(1);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
