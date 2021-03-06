package com.ljkj.qxn.wisdomsitepro.ui.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ljkj.qxn.wisdomsitepro.R;
import com.ljkj.qxn.wisdomsitepro.Utils.widget.WisdomWebView;

import butterknife.BindView;
import butterknife.OnClick;
import cdsp.android.logging.Logger;
import cdsp.android.ui.base.BaseActivity;

/**
 * 类描述：WebViewActivity
 * 创建人：lxx
 * 创建时间：2018/5/22 15:45
 * 修改人：
 * 修改时间：
 * 修改备注：
 */
public class WebViewActivity extends BaseActivity {
    private static final String EXTRA_KEY_TITLE = "extra_key_title";
    private static final String EXTRA_KEY_URL = "extra_key_url";
    private static final String TAG = WebViewActivity.class.getSimpleName();

    @BindView(R.id.tv_title)
    TextView titleText;

    @BindView(R.id.web_view)
    WisdomWebView webView;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private boolean isAnimStart = false;
    private String url;

    public static void startActivity(Context context, @NonNull String title, @NonNull String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(EXTRA_KEY_TITLE, title);
        intent.putExtra(EXTRA_KEY_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
    }

    @Override
    protected void initUI() {
        String title = getIntent().getStringExtra(EXTRA_KEY_TITLE);
        this.url = getIntent().getStringExtra(EXTRA_KEY_URL);
        titleText.setText(title);
    }

    @Override
    protected void initData() {
        configWebView();
        Logger.i(TAG, "WebView加载的url=" + url);
        webView.loadUrl(url);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface"})
    private void configWebView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        WebSettings webSettings = webView.getSettings();
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new NativeInterface(this), "AndroidNative");

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true);  //是否支持viewport属性，默认值 false
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小,当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度

        webSettings.setDomStorageEnabled(true);
        webSettings.setSavePassword(false);
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheEnabled(false);
        webSettings.setAllowFileAccess(true);  //设置可以访问文件
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setTextZoom(100); //关键属性，避免系统字体改变时，导致html页面显示铺不满手机屏幕
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setUseWideViewPort(true);

        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); //支持内容重新布局

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }

        webView.setWebViewClient(new SimpleWebViewClient());
        webView.setWebChromeClient(new SimpleWebChromeClient());
    }

    private class SimpleWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setAlpha(1.0f);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Logger.e("WebView", "onReceivedError--->" + error.getDescription());
            } else {
                Logger.e("WebView", "onReceivedError--->" + view.getUrl());
            }
        }

    }

    private class SimpleWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (progressBar == null) {
                return;
            }
            int currentProgress = progressBar.getProgress();
            if (newProgress >= 100 && !isAnimStart) {
                isAnimStart = true; // 防止调用多次动画
                startDismissAnimation(currentProgress);// 开启属性动画让进度条平滑消失

            } else {
                startProgressAnimation(currentProgress, newProgress);// 开启属性动画让进度条平滑递增
            }

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (titleText != null && TextUtils.isEmpty(titleText.getText())) {
                titleText.setText(title);
            }
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

    }

    //递增动画
    private void startProgressAnimation(int from, int to) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", from, to);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    //消失动画
    private void startDismissAnimation(final int progress) {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(progressBar, "alpha", 1.0f, 0.0f);
        ObjectAnimator anim2 = ObjectAnimator.ofInt(progressBar, "progress", progress, 100);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(anim1, anim2);
        animatorSet.setDuration(1000);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (progressBar == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                isAnimStart = false;
            }
        });
        animatorSet.start();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyWebView();
    }

    private void destroyWebView() {
        if (webView == null) {
            return;
        }
        webView.setWebViewClient(null);
        webView.setWebChromeClient(null);
        webView.clearHistory();
        ((ViewGroup) webView.getParent()).removeView(webView);
        webView.destroy();
        webView = null;
    }

    @OnClick(R.id.tv_back)
    public void onViewClicked() {
        onBackPressed();
    }

    private class NativeInterface {
        private Context context;

        NativeInterface(Context context) {
            this.context = context;
        }


    }

}
