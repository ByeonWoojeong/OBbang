package app.gh.obbang;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.File;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";
    InputMethodManager methodManager;
    SharedPreferences preferences;
    String token, getUrl;
    WebView webView;
    WebView childView;

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );
        methodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        methodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        setContentView(R.layout.activity_main2);
        preferences = getSharedPreferences("pref", MODE_PRIVATE);

        webView = (WebView) findViewById(R.id.webview);

        getUrl = getIntent().getStringExtra("url");

        webView.getSettings().setDatabaseEnabled(true);
        File dir = getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        webView.getSettings().setAppCachePath(dir.getPath());
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            webView.getSettings().setTextZoom(100);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        } else {
            CookieSyncManager.createInstance(this);
        }
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.loadUrl(getUrl);  //원하는 사이트의 주소
    }

    class MyWebViewClient extends WebViewClient {
        public boolean doFallback(WebView view, Intent parsedIntent) {
            if (parsedIntent == null) {
                return false;
            }
            String fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url");
            if (fallbackUrl != null) {
                view.loadUrl(fallbackUrl);
                return true;
            }

            final String packageName = parsedIntent.getPackage();
            if (packageName != null) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                builder.setMessage("설치 후 사용하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, " 11111 " + url);
            if (url.startsWith("tel:")) {
                Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(dial);
                return true;
            } else if (url.startsWith("sms:")) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(i);
                return true;
            } else if (url.startsWith("intent:")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent intent = null;
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (webView.canGoBack()) {
                            webView.clearHistory();
                        }
                        return doFallback(view, intent);
                    }
                } else {
                    Intent intent = null;
                    try {
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        if (webView.canGoBack()) {
                            webView.clearHistory();
                        }
                        return doFallback(view, intent);
                    }
                }
                return true;
            } else if (url.endsWith(".mp4") || url.endsWith(".swf")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                view.getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                return false;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onCloseWindow(WebView w) {
            super.onCloseWindow(w);
            Log.i(TAG, "123");
            finish();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            Log.i(TAG, " Chrome 1");
            new AlertDialog.Builder(Main2Activity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            Log.i(TAG, " Chrome 2");
            new AlertDialog.Builder(Main2Activity.this)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            Log.i(TAG, " Chrome 3");
            childView = new WebView(view.getContext());
            childView.getSettings().setJavaScriptEnabled(true);
            childView.getSettings().setDomStorageEnabled(true);
            childView.getSettings().setAllowFileAccess(true);
            childView.getSettings().setAllowContentAccess(true);
            childView.getSettings().setLoadWithOverviewMode(true);
            childView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            childView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
            childView.getSettings().setUseWideViewPort(true);
            childView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            childView.setWebChromeClient(this);
            childView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                    Log.i(TAG, " 22222 " + url);
                    if (url.startsWith("tel:")) {
                        Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(dial);
                        return true;
                    } else if (url.startsWith("sms:")) {
                        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                        startActivity(i);
                        return true;
                    } else if (url.startsWith("market://details?id=")) {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                            childView.setVisibility(View.GONE);
                            childView.removeView(childView);
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(i);
                        }
                    } else if (url.startsWith("intent:")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            try {
                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                childView.setVisibility(View.GONE);
                                childView.removeView(childView);
                            }
                        } else {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                childView.setVisibility(View.GONE);
                                childView.removeView(childView);
                            }
                        }
                    } else if (url.endsWith(".mp4") || url.endsWith(".swf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(url), "video/*");
                        view.getContext().startActivity(intent);
                        return true;
                    } else if (url.startsWith("http://") || url.startsWith("https://")) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        startActivity(intent);

                        Intent intent = new Intent(Main2Activity.this, Main2Activity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);

                        return false;
                    } else {
//                        return false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            try {
                                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return true;
                }
            });

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }
    }


}
