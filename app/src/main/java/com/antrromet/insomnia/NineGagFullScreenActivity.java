package com.antrromet.insomnia;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.antrromet.insomnia.adapters.NineGagPagerAdapter;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.widgets.HackyViewPager;

public class NineGagFullScreenActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener, GestureDetector
        .OnDoubleTapListener, View.OnLongClickListener, View.OnClickListener {

    private static final String NINE_GAG_PKG_NAME = "com.ninegag.android.app";

    private NineGagPagerAdapter mAdapter;
    private HackyViewPager mViewPager;
    private ShareActionProvider mShareActionProvider;
    private Toolbar mToolBar;
    private boolean mIsContentVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nine_gag_full_screen);

        // Setting up toolbar
        mToolBar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.bringToFront();

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mAdapter = new NineGagPagerAdapter(this);
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

        getSupportLoaderManager().restartLoader(Constants.Loaders.NINE_GAG_FEEDS.id, null,
                this);

        showActionBar();
        mHandler.postDelayed(mHideRunnable, 3000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu_nine_gag, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        return true;
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
        return new CursorLoader(this, DBProvider.URI_NINE_GAG, null, null, null,
                DBOpenHelper.COLUMN_INSERTION_TIME + " asc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.setCursor(data);
        int pos = getIntent().getIntExtra("position", 0);
        mViewPager.setCurrentItem(pos, false);
        setShareIntent(pos);
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

    private void setShareIntent(int pos) {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mAdapter.getShareText(pos));
            shareIntent.setType("text/plain");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setShareIntent(position);
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
        //Launch the app or the webview activity
//        http://9gag.com/gag/aD3EWjB?ref=android
        String link = "http://9gag" + ".com/gag/" + v.getTag(R.id.key_id) + "?ref=android";
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(NINE_GAG_PKG_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            startActivity(new Intent(this, WebViewActivity.class).putExtra("link", link)
                    .putExtra("title", getString(R.string.nine_gag_post)));
        } finally {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        showHideContent();
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
}
