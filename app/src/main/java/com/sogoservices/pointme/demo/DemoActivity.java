package com.sogoservices.pointme.demo;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sogoservices.pointme.R;
import com.sogoservices.pointme.debug.DebugActivity;
import com.sogoservices.pointme.sdk.blepackage.data.PNMBleResultAudioSync;
import com.sogoservices.pointme.sdk.blepackage.data.PNMBleResultDeviceLocation;
import com.sogoservices.pointme.sdk.blepackage.parser.PNMBlePackageManagerAudioSync;
import com.sogoservices.pointme.sdk.blepackage.parser.PNMBlePackageManagerDeviceLocation;
import com.sogoservices.pointme.sdk.data.PNMPublicPoint;
import com.sogoservices.pointme.sdk.scanner.PNMScanner;
import com.sogoservices.pointme.sdk.scanner.PNMScannerConfiguration;
import com.sogoservices.pointme.sdk.scanner.PNMScannerDelegate;
import com.sogoservices.pointme.sdk.scanner.PNMScannerError;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends AppCompatActivity {

    private final int REQUEST_CODE_PERMISSIONS = 1;

    private final String STATE_SEARCH_MODE = "STATE_SEARCH_MODE";

    private enum SearchMode { STOPPED, STARTED, PAUSED, FOUND }

    private SearchMode searchMode = SearchMode.STOPPED;

    private ViewPager viewPager;
    private CardAdapter cardAdapter;

    private View searchViewGroup;
    private View searchStoppedView;
    private View searchStartedView;
    private View searchFoundView;
    private AnimatorSet searchFoundAnimation;

    private List<SearchResult> searchResults = new ArrayList<>();

    private WebView webView;

    private PNMScanner scanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity);

        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });

        findViewById(R.id.switchToDebugView).setOnClickListener(v -> {
            SearchHistory.getInstance().clear();
            Intent intent = new Intent(this, DebugActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        viewPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager);

        cardAdapter = new CardAdapter(getSupportFragmentManager());
        viewPager.setAdapter(cardAdapter);

        searchViewGroup = findViewById(R.id.searchViewGroup);
        searchStoppedView = findViewById(R.id.searchStoppedView);
        searchStartedView = findViewById(R.id.searchStartedView);
        searchFoundView = findViewById(R.id.searchFoundView);

        searchFoundAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.match_found);
        searchFoundAnimation.setTarget(searchFoundView);

        searchViewGroup.setOnClickListener(v -> {
            if (searchMode == SearchMode.FOUND) {
                onSearchButtonClick();
                cardAdapter.onSearchButtonPressed();
                vibrate();
            }
        });

        if (savedInstanceState != null) {
            searchMode = (SearchMode) savedInstanceState.getSerializable(STATE_SEARCH_MODE);
        }
        applyCurrentSearchMode();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int lastPosition = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    searchViewGroup.setVisibility(View.VISIBLE);
                } else {
                    searchViewGroup.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int currentPosition = viewPager.getCurrentItem();
                if (state == ViewPager.SCROLL_STATE_IDLE && currentPosition != lastPosition) {
                    if (currentPosition == 0) {
                        startSearch();
                    } else {
                        stopSearch();
                    }
                    lastPosition = currentPosition;
                }
            }
        });

        PNMScannerConfiguration scannerConfiguration = new PNMScannerConfiguration(
                PNMScannerConfiguration.SourceType.GPSAndBluetooth,
                "********");
        scanner = new PNMScanner<>(this, scannerConfiguration, delegate);
        scanner.subscribe(new PNMBlePackageManagerAudioSync() {
            @Override
            public void onReceived(PNMBleResultAudioSync pnmBleResultAudioSync) {
                // TODO: do something with pnmBleResultAudioSync
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_SEARCH_MODE, searchMode);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (viewPager.getCurrentItem() == 0) {
            startSearch();
        } else {
            onSearchStopped();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSearch();
        searchResults.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanner.startScanning();
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                    .setMessage(R.string.permissions_required)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNeutralButton(R.string.cancel, (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            } else {
                finish();
            }
        }
    }

    public void startSearch() {
        cardAdapter.setCurrentSearchResult(null);
        scanner.startScanning();
    }

    public void stopSearch() {
        scanner.stopScanning();
    }

    private void onSearchStarted() {
        searchMode = SearchMode.STARTED;
        applyCurrentSearchMode();
    }

    private void onSearchPaused() {
        searchMode = SearchMode.PAUSED;
        applyCurrentSearchMode();
    }

    private void onSearchStopped() {
        searchMode = SearchMode.STOPPED;
        applyCurrentSearchMode();
    }

    private List<String> loadedUrls = new ArrayList<>();

    private void onSearchFound(List<SearchResult> searchResults) {
        searchMode = searchResults.isEmpty() ? SearchMode.STARTED : SearchMode.FOUND;
        applyCurrentSearchMode();
        if (searchResults.size() > 0) {
            if (this.searchResults.isEmpty()) {
                vibrate();
            }

            String url = searchResults.get(0).url;
            if (!loadedUrls.contains(url)) {
                webView.loadUrl(url);
                loadedUrls.add(url);
            }
        }
        this.searchResults = searchResults;
        cardAdapter.setCurrentSearchResult(searchResults.isEmpty() ? null : searchResults.get(0));
    }

    public void onSearchButtonClick() {
        if (viewPager.getCurrentItem() == 0 && !searchResults.isEmpty()) {
            SearchResult firstResult = searchResults.get(0);
            if (SearchHistory.getInstance().addItem(firstResult)) {
                cardAdapter.addSearchResultToHistory(firstResult);
            }
        }
    }

    public void navigate(SearchResult searchResult) {
        PNMPublicPoint publicPoint = new PNMPublicPoint(0, searchResult.uuid, searchResult.type,
            searchResult.url, searchResult.description, searchResult.bitmap);
        scanner.navigate(publicPoint, false);
    }

    private void applyCurrentSearchMode() {
        switch (searchMode) {
            case STOPPED:
            case PAUSED:
                searchStoppedView.setVisibility(View.VISIBLE);
                searchStartedView.setVisibility(View.GONE);
                searchFoundView.setVisibility(View.GONE);
                searchFoundAnimation.cancel();
                break;
            case STARTED:
                searchStoppedView.setVisibility(View.GONE);
                searchStartedView.setVisibility(View.VISIBLE);
                searchFoundView.setVisibility(View.GONE);
                searchFoundAnimation.cancel();
                break;
            case FOUND:
                searchStoppedView.setVisibility(View.GONE);
                searchStartedView.setVisibility(View.GONE);
                searchFoundView.setVisibility(View.VISIBLE);
                searchFoundAnimation.start();
                break;
        }
    }

    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, 128));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    private PNMScannerDelegate delegate = new PNMScannerDelegate() {
        @Override
        public void didFindPoints(List<PNMPublicPoint> points) {
            List<SearchResult> searchResults = new ArrayList<>();
            for (PNMPublicPoint point: points) {
                searchResults.add(new SearchResult(point.getUuid(), point.getType(),
                    point.getUrlDescription(), point.getUrlIcon(), point.getUrl()));
            }
            onSearchFound(searchResults);
        }

        @Override
        public void didScanningProgressChange(boolean isScanning) {
            if (isScanning) {
                onSearchStarted();
            } else {
                onSearchPaused();
            }
        }

        @Override
        public void scannerError(PNMScannerError error) {
            if (isFinishing()) return;
            switch (error) {
                case BleNotAuthorized:
                case GPSNotAuthorized: {
                    ActivityCompat.requestPermissions(DemoActivity.this, new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
                    break;
                }
                case BleUnavailable: {
                    new AlertDialog.Builder(DemoActivity.this)
                        .setMessage("Please, turn on bluetooth")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                    break;
                }
                case BleMissing: {
                    new AlertDialog.Builder(DemoActivity.this)
                        .setMessage("The device doesn't have the bluetooth")
                        .setNeutralButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                    break;
                }
                case GPSUnavailable: {
                    new AlertDialog.Builder(DemoActivity.this)
                        .setMessage("Please, turn on \"Use location\" in the device settings")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                    break;
                }
                case NoInternetConnection: {
                    new AlertDialog.Builder(DemoActivity.this)
                        .setMessage("No internet connection")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                    break;
                }
                case ServerFetchFailed: {
                    new AlertDialog.Builder(DemoActivity.this)
                        .setMessage("Server fetch failed")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                    break;
                }
                case OrientationUnavailable: {
                    new AlertDialog.Builder(DemoActivity.this)
                        .setMessage("Orientation is unavailable")
                        .setNeutralButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                    break;
                }
            }
        }

        @Override
        public void scannerDidStart() {
            onSearchStarted();
        }

        @Override
        public void scannerDidStop() {
            onSearchStopped();
        }
    };
}
