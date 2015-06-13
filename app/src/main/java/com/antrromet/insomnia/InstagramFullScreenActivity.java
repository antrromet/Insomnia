package com.antrromet.insomnia;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.antrromet.insomnia.adapters.InstagramPagerAdapter;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.widgets.HackyViewPager;

public class InstagramFullScreenActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener, GestureDetector
        .OnDoubleTapListener, View.OnLongClickListener, View.OnClickListener {

    private static final String INSTAGRAM_PKG_NAME = "com.instagram.android";

    private InstagramPagerAdapter mAdapter;
    private HackyViewPager mViewPager;
    private Toolbar mToolBar;
    private boolean mIsContentVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_full_screen);

        // Setting up toolbar
        mToolBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.bringToFront();

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mAdapter = new InstagramPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        Handler mHandler = new Handler();
        Runnable mHideRunnable = new Runnable() {
            @Override
            public void run() {
                hideActionBar();
                mViewPager.findViewWithTag(mViewPager.getCurrentItem()).findViewById(R.id.title_text_view)
                        .setVisibility(View.GONE);
            }
        };

        getSupportLoaderManager().restartLoader(Constants.Loaders.INSTAGRAM_FEEDS.id, null,
                this);

        showActionBar();
        mHandler.postDelayed(mHideRunnable, 3000);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This order should exactly be the same as the one in the Main Activity
        return new CursorLoader(this, DBProvider.URI_INSTAGRAM, null, null, null,
                DBOpenHelper.COLUMN_CREATED_TIME + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.setCursor(data);
        mViewPager.setCurrentItem(getIntent().getIntExtra("position", 0), false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("position", mViewPager.getCurrentItem());
        setResult(100, returnIntent);
        super.finish();
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (mViewPager.findViewWithTag(mViewPager.getCurrentItem()) != null) {
            if (mIsContentVisible) {
                mViewPager.findViewWithTag(mViewPager.getCurrentItem()).findViewById(R.id.title_text_view)
                        .setVisibility(View.VISIBLE);
            } else {
                mViewPager.findViewWithTag(mViewPager.getCurrentItem()).findViewById(R.id.title_text_view)
                        .setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * Shows the title bar on the top
     */
    private void showActionBar() {
        mIsContentVisible = true;
        mToolBar.animate().translationY(0).setInterpolator(new AccelerateInterpolator()).start();
//        mHandler.postDelayed(mHideRunnable, 3000);
        mAdapter.setTitleVisibility(mIsContentVisible);
    }

    private void hideActionBar() {
        mIsContentVisible = false;
        mToolBar.animate().translationY(-mToolBar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        mAdapter.setTitleVisibility(mIsContentVisible);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        showHideContent();
        return false;
    }

    private void showHideContent() {
        if (mIsContentVisible) {
            hideActionBar();
            mViewPager.findViewWithTag(mViewPager.getCurrentItem()).findViewById(R.id.title_text_view)
                    .setVisibility(View.GONE);
        } else {
            showActionBar();
            mViewPager.findViewWithTag(mViewPager.getCurrentItem()).findViewById(R.id.title_text_view)
                    .setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(INSTAGRAM_PKG_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            startActivity(new Intent(this, WebViewActivity.class).putExtra("link", v.getTag(R.id
                    .key_link).toString())
                    .putExtra("title", getString(R.string.instagram_post)));
        } finally {
            Intent intent = pm.getLaunchIntentForPackage(INSTAGRAM_PKG_NAME);
            intent.setComponent(new ComponentName(INSTAGRAM_PKG_NAME, "com.instagram.android" +
                    ".activity.UrlHandlerActivity"));
            intent.setData(Uri.parse(v.getTag(R.id.key_link).toString()));
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        showHideContent();
    }
}
