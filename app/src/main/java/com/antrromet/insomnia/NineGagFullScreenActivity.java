package com.antrromet.insomnia;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.antrromet.insomnia.adapters.NineGagPagerAdapter;
import com.antrromet.insomnia.provider.DBOpenHelper;
import com.antrromet.insomnia.provider.DBProvider;
import com.antrromet.insomnia.widgets.HackyViewPager;

public class NineGagFullScreenActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener{

    private NineGagPagerAdapter mAdapter;
    private HackyViewPager mViewPager;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nine_gag_full_screen);

        // Setting up toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        mAdapter = new NineGagPagerAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        getSupportLoaderManager().restartLoader(Constants.Loaders.NINE_GAG_FEEDS.id, null,
                this);
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
                DBOpenHelper.COLUMN_INSERTION_TIME + " desc");
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
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
