package com.antrromet.insomnia;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.antrromet.insomnia.adapters.MainViewPagerAdapter;
import com.antrromet.insomnia.fragments.BaseFragment;
import com.antrromet.insomnia.fragments.NineGagFragment;
import com.antrromet.insomnia.interfaces.OnTabClickListener;
import com.antrromet.insomnia.widgets.SlidingTabLayout;

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
                }
            }
        }
    }

}
