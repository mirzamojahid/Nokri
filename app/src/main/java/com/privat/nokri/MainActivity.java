package com.privat.nokri;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private ProgressBar progressBar;
    private Handler handler = new Handler();
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    WebView superWebView;
    TextView textView;
    SwipeRefreshLayout websitePullToRefresh;
    private RewardedVideoAd mRewardedVideoAd;

    @TargetApi(Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");


        ImageView btnnext, btnpre;
        textView = findViewById(R.id.textv);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnnext = (ImageView) findViewById(R.id.btnnext);
        btnpre = (ImageView) findViewById(R.id.btnprev);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        // Make sure that the MainActivity class implements the required interface:
        // RewardedVideoAdListener
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        prepaperAD();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mRewardedVideoAd.isLoaded()) {
                            mRewardedVideoAd.show();
                        }

                         else {

                          /*  if (mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            }

                           */
                            if (mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            }
                          //  Log.d("TAG", "INTERSTIAL AD NOT LOADED ");
                        }
                        prepaperAD();

                    }
                });
            }
        }, 30, 30, TimeUnit.SECONDS);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){

            NotificationChannel channel= new NotificationChannel("MyNotifications","MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }

        FirebaseMessaging.getInstance().subscribeToTopic("ge")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Successfully";
                        if (!task.isSuccessful()) {
                            msg = "failed";
                        }

                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });





        websitePullToRefresh = (SwipeRefreshLayout) findViewById(R.id.websitePullToRefresh);





        superWebView = findViewById(R.id.myWebView);





        superWebView.loadUrl("https://apps.privatenokri.com");
        superWebView.getSettings().setJavaScriptEnabled(true);
        superWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }


            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                try {
                    superWebView.stopLoading();
                } catch (Exception e) {
                }

                if (superWebView.canGoBack()) {
                    webView.goBack();
                }

                superWebView.loadUrl("about:blank");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Check your internet connection and try again.");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Try Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        startActivity(getIntent());
                    }
                });

                alertDialog.show();
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }
        });

        websitePullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                superWebView.loadUrl("https://apps.privatenokri.com");
                websitePullToRefresh.setRefreshing(false);
            }
        });



        superWebView.setWebChromeClient(new WebChromeClient() {
                                            @Override
                                            public void onProgressChanged(WebView view, int newProgress) {
                                                progressBar.setVisibility(View.VISIBLE);

                                                progressBar.setProgress(newProgress);
                                                if (newProgress == 100) {
                                                    progressBar.setVisibility(View.GONE);
                                                }
                                                super.onProgressChanged(view, newProgress);
                                            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
               // getSupportActionBar().setTitle(title);
                textView.setText(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }
                                        });

        btnpre.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (superWebView.canGoBack()) {
                   superWebView.goBack();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();

                    }
                }
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(superWebView.canGoForward()){
                    superWebView.goForward();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();

                    }
                }

            }
        });


        superWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri myUri = Uri.parse(url);
                Intent superIntent = new Intent(Intent.ACTION_VIEW);
                superIntent.setData(myUri);
                startActivity(superIntent);
            }
        });

    }






    private void GoForward() {
        if (superWebView.canGoForward()) {
            superWebView.goForward();
        } else {
            Toast.makeText(this, "Can't go further!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (superWebView.canGoBack()) {
            superWebView.goBack();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Exit App");
            dialog.setMessage("Private Noukri has nothing to go back, so what next?");
            dialog.setPositiveButton("EXIT ME", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
finish();
                }
            });
            dialog.setCancelable(false);
            dialog.setNegativeButton("STAY HERE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

        }
    }

    public void prepaperAD(){

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5981443664978235/3538233294");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-5981443664978235/8640839947",
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(this, "ad is coming", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();


}

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(this, "AdClosed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }


    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {
        Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }
}