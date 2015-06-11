package com.antrromet.insomnia;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antrromet.insomnia.utils.PreferencesManager;

public class WebViewActivity extends BaseActivity {

    private ProgressBar mWebViewProgressBar;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String title = getIntent().getStringExtra("title");
        // Setting up toolbar
        Toolbar toolBar = (Toolbar) findViewById(R.id.action_bar);
        if (!TextUtils.isEmpty(title)) {
            toolBar.setTitle(title);
        }
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mWebViewProgressBar = (ProgressBar) findViewById(R.id.webViewProgress);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings();
        mWebView.getSettings().setJavaScriptEnabled(true);

        // Enable default zoom in Android webview
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDisplayZoomControls(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setUseWideViewPort(true);

        // Show the error (if any) while loading the web page
        mWebView.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebViewActivity.this, description, Toast.LENGTH_SHORT).show();
            }
        });

        // Show the progress of the page
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mWebViewProgressBar.setProgress(newProgress);
                mWebViewProgressBar.bringToFront();
                if (newProgress == 100) {
                    mWebViewProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
                                      @Override
                                      public boolean shouldOverrideUrlLoading(WebView view,
                                                                              String url) {
                                          if (url.startsWith(getString(R.string
                                                  .instagram_redirect_uri))) {
                                              String parts[] = url.split("=");
                                              String accessToken = parts[1];
                                              showToast(R.string.successfully_logged_in);
                                              PreferencesManager.set(WebViewActivity.this,
                                                      Constants.APP_PREFERENCES,
                                                      Constants
                                                              .SharedPreferenceKeys
                                                              .INSTAGRAM_ACCESS_TOKEN, accessToken);
                                              setResult(Activity.RESULT_FIRST_USER);
                                              WebViewActivity.this.finish();
                                              return true;
                                          }
                                          return false;
                                      }
                                  }
        );

        String url = getIntent().getStringExtra("link");
        mWebView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
