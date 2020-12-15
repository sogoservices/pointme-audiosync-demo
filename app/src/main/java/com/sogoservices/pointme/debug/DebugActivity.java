package com.sogoservices.pointme.debug;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.sogoservices.pointme.R;
import com.sogoservices.pointme.Utils;
import com.sogoservices.pointme.demo.DemoActivity;
import com.sogoservices.pointme.sdk.blepackage.data.PNMBleResultAudioSync;
import com.sogoservices.pointme.sdk.blepackage.data.PNMBleResultDeviceLocation;
import com.sogoservices.pointme.sdk.blepackage.parser.PNMBlePackageManagerAudioSync;
import com.sogoservices.pointme.sdk.blepackage.parser.PNMBlePackageManagerDeviceLocation;
import com.sogoservices.pointme.sdk.data.PNMPublicPoint;
import com.sogoservices.pointme.sdk.scanner.PNMScanner;
import com.sogoservices.pointme.sdk.scanner.PNMScannerConfiguration;
import com.sogoservices.pointme.sdk.scanner.PNMScannerDelegate;
import com.sogoservices.pointme.sdk.scanner.PNMScannerError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebugActivity extends AppCompatActivity implements PNMScannerDelegate {

    private final int REQUEST_CODE_PERMISSIONS = 1;

    private PNMScanner scanner;

    private TextView azimuthView;
    private TextView lastAzimuthView;
    private TextView pitchView;
    private TextView rollView;
    private TextView latitudeView;
    private TextView longitudeView;
    private TextView posChangedView;

    private TextView configPositionThresholdView;
    private TextView configPitchThresholdView;
    private TextView configAzimuthThresholdView;
    private TextView configAzimuthDeviationView;

    private TextView poiTitleView;
    private TextView beaconTitleView;
    private View progressView;

    private DeviceLocationView deviceLocationView;

    private ViewGroup actionsContainerViewGroup;

    private LinearLayout audioSyncContentLayout;
    private LinearLayout deviceLocationContentLayout;

    private POIsAdapter poisAdapter = new POIsAdapter();
    private BeaconsAdapter beaconsAdapter = new BeaconsAdapter();

    private boolean isStartScanningOnStart = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_activity);

        findViewById(R.id.switchToDemoView).setOnClickListener(v -> {
            Intent intent = new Intent(this, DemoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        lastAzimuthView = findViewById(R.id.lastAzimuthView);
        azimuthView = findViewById(R.id.azimuthView);
        pitchView = findViewById(R.id.pitchView);
        rollView = findViewById(R.id.rollView);
        latitudeView = findViewById(R.id.latitudeView);
        longitudeView = findViewById(R.id.longitudeView);
        posChangedView = findViewById(R.id.positionChangedView);

        configPositionThresholdView = findViewById(R.id.configPositionChangeThresholdView);
        configPitchThresholdView = findViewById(R.id.configPitchRotationThresholdView);
        configAzimuthThresholdView = findViewById(R.id.configAzimuthRotationThresholdView);
        configAzimuthDeviationView = findViewById(R.id.configAzimuthDeviationView);

        poiTitleView = findViewById(R.id.poiTitleView);
        beaconTitleView = findViewById(R.id.beaconsTitleView);
        progressView = findViewById(R.id.progress);

        audioSyncContentLayout = findViewById(R.id.audioSyncContentLayout);
        deviceLocationContentLayout = findViewById(R.id.deviceLocationContentLayout);

        actionsContainerViewGroup = findViewById(R.id.actionsContainer);

        PNMScannerConfiguration scannerConfiguration = new PNMScannerConfiguration(
                PNMScannerConfiguration.SourceType.GPSAndBluetooth,
                "********");
        scanner = new PNMScanner(this, scannerConfiguration, this);
        scanner.subscribe(new PNMBlePackageManagerAudioSync() {
            @Override
            public void onReceived(PNMBleResultAudioSync pnmBleResultAudioSync) {
                audioSyncContentLayout.setVisibility(View.VISIBLE);
                deviceLocationContentLayout.setVisibility(View.GONE);
                beaconsAdapter.setBlePackage(pnmBleResultAudioSync);
                beaconsAdapter.notifyDataSetChanged();
            }
        });

        RecyclerView poisView = findViewById(R.id.poisView);
        poisView.setAdapter(poisAdapter);
        poisView.setNestedScrollingEnabled(false);

        RecyclerView beaconsView = findViewById(R.id.beaconsView);
        beaconsView.setAdapter(beaconsAdapter);
        beaconsView.setNestedScrollingEnabled(false);

        deviceLocationView = findViewById(R.id.deviceLocationView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateOrientationUI();
        updateLocationUI();
        updateConfigurationUI();
        updatePointsUI();

        if (isStartScanningOnStart) {
            scanner.startScanning();
            isStartScanningOnStart = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStartScanningOnStart = scanner.isRunning();
        scanner.stopScanning();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_logs: {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, "PointMe logs");
                intent.setType("text/plain");

                ArrayList<Uri> files = new ArrayList<>();
                File dir = new File(getFilesDir(), "logs");
                File[] logFiles = dir.listFiles();
                if (logFiles != null) {
                    for (File file: logFiles) {
                        files.add(FileProvider.getUriForFile(this,
                            getString(R.string.file_provider_authority), file));
                    }
                }

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(intent);
                return true;
            }
            default:
                return false;
        }
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

    private void updateOrientationUI() {
        if (scanner.hasOrientation()) {
            switch (scanner.getOrientationAccuracy()) {
                case SensorManager.SENSOR_STATUS_UNRELIABLE: {
                    azimuthView.setText(getString(R.string.azimuth_pattern_unreliable, Math.round(scanner.getAzimuth())));
                    break;
                }
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW: {
                    azimuthView.setText(getString(R.string.azimuth_pattern_low, Math.round(scanner.getAzimuth())));
                    break;
                }
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM: {
                    azimuthView.setText(getString(R.string.azimuth_pattern_medium, Math.round(scanner.getAzimuth())));
                    break;
                }
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH: {
                    azimuthView.setText(getString(R.string.azimuth_pattern_high, Math.round(scanner.getAzimuth())));
                    break;
                }
                default:
                    azimuthView.setText(getString(R.string.azimuth_pattern, Math.round(scanner.getAzimuth())));
            }
            pitchView.setText(getString(R.string.pitch_pattern, Math.round(scanner.getPitch())));
            rollView.setText(getString(R.string.roll_pattern, Math.round(scanner.getRoll())));
        } else {
            azimuthView.setText(R.string.azimuth_none);
            pitchView.setText(R.string.pitch_none);
            rollView.setText(R.string.roll_none);
        }
    }

    private void updateLocationUI() {
        if (!scanner.isInStandby()) {
            poisAdapter.setCurrentLocation(scanner.getLocation());
            poisAdapter.notifyDataSetChanged();
        }
        if (scanner.getLocation() == null) {
            latitudeView.setText(R.string.latitude_none);
            longitudeView.setText(R.string.longitude_none);
        } else {
            latitudeView.setText(getString(R.string.latitude_pattern, scanner.getLocation().getLatitude()));
            longitudeView.setText(getString(R.string.longitude_pattern, scanner.getLocation().getLongitude()));
        }
        Float posChanged = scanner.getLocationChanged();
        if (posChanged == null)
            posChangedView.setText(getString(R.string.position_changed_none));
        else
            posChangedView.setText(getString(R.string.position_changed, Math.round(scanner.getLocationChanged())));
    }

    private void updateConfigurationUI() {
        if (scanner.getServerConfiguration() != null) {
            configPositionThresholdView.setText(getString(R.string.config_position_change_threshold, scanner.getServerConfiguration().getPositionChangeThreshold()));
            configPitchThresholdView.setText(getString(R.string.config_pitch_rotation_threshold, scanner.getServerConfiguration().getPitchRotationThreshold()));
            configAzimuthThresholdView.setText(getString(R.string.config_azimuth_rotation_threshold, scanner.getServerConfiguration().getAzimuthRotationThreshold()));
            configAzimuthDeviationView.setText(getString(R.string.config_azimuth_deviation, scanner.getServerConfiguration().getAzimuthDeviation()));
        } else {
            configPositionThresholdView.setText(R.string.config_position_change_threshold_none);
            configPitchThresholdView.setText(R.string.config_pitch_rotation_threshold_none);
            configAzimuthThresholdView.setText(R.string.config_azimuth_rotation_threshold_none);
            configAzimuthDeviationView.setText(R.string.config_azimuth_deviation_none);
        }
    }

    private void showMatchedPoints(List<PNMPublicPoint> matchedPOIs) {
        actionsContainerViewGroup.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(DebugActivity.this);
        for (final PNMPublicPoint point : matchedPOIs) {
            View actionView = inflater.inflate(R.layout.debug_layout_action, actionsContainerViewGroup, false);
            TextView actionButton = actionView.findViewById(R.id.button);

            if (point.getUrlIcon() == null) {
                actionButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_unicorn, 0, 0, 0);
            } else {
                Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                    point.getUrlIcon(), Utils.dpToPx(24), Utils.dpToPx(24), true));
                actionButton.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
            }

            switch (point.getType()) {
                case POI:
                    actionButton.setText(String.format(Locale.US, "type: %s, id: %s, %s",
                        point.getType(), point.getId(), point.getUrlDescription()));
                    break;
                case Beacon:
                    if (point.getUrlDescription() == null) {
                        actionButton.setText(String.format(Locale.US, "type: %s, uuid: %s",
                            point.getType(), point.getUuid()));
                    } else {
                        actionButton.setText(String.format(Locale.US, "type: %s, uuid: %s, %s",
                            point.getType(), point.getUuid(), point.getUrlDescription()));
                    }
                    break;
            }
            actionButton.setOnClickListener(v -> scanner.navigate(point, true));
            actionsContainerViewGroup.addView(actionView);
        }
    }

    private void updatePointsUI() {
        poisAdapter.setScanResult(scanner.getLastScanResult());
        poisAdapter.setMatchedPoints(scanner.getLastScanMatchedPoints());
        poisAdapter.notifyDataSetChanged();

        beaconsAdapter.setBeacons(scanner.getAllBeacons());
        beaconsAdapter.setMatchedBeacons(scanner.getLastScanMatchedBeacons());
        beaconsAdapter.notifyDataSetChanged();

        if (scanner.getLastScanAzimuth() != null) {
            lastAzimuthView.setText(getString(R.string.azimuth_last_pattern, Math.round(scanner.getLastScanAzimuth())));
        } else {
            lastAzimuthView.setText(getString(R.string.azimuth_last_none));
        }

        if (scanner.getAllPOIs() == null || scanner.getAllPOIs().isEmpty()) {
            poiTitleView.setText(getString(R.string.poi_count, 0));
        } else {
            poiTitleView.setText(getString(R.string.poi_count, scanner.getAllPOIs().size()));
        }

        if (scanner.getAllBeacons() == null || scanner.getAllBeacons().isEmpty()) {
            beaconTitleView.setText(getString(R.string.beacons_count, 0));
        } else {
            beaconTitleView.setText(getString(R.string.beacons_count, scanner.getAllBeacons().size()));
        }
    }

    @Override
    public void didConfigurationChanged() {
        updateConfigurationUI();
    }

    @Override
    public void didOrientationChanged() {
        updateOrientationUI();
    }

    @Override
    public void didLocationChanged() {
        updateLocationUI();
    }

    @Override
    public void didFindPoints(List<PNMPublicPoint> points) {
        updatePointsUI();
        if (!(scanner.isInStandby() && points.isEmpty())) {
            showMatchedPoints(points);
        }
    }

    @Override
    public void didScanningProgressChange(boolean isScanning) {
        if (isScanning) {
            progressView.setVisibility(View.VISIBLE);
        } else {
            progressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void scannerError(PNMScannerError error) {
        if (isFinishing()) return;
        switch (error) {
            case BleNotAuthorized:
            case GPSNotAuthorized: {
                ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
                break;
            }
            case BleUnavailable: {
                new AlertDialog.Builder(this)
                    .setMessage("Please, turn on bluetooth")
                    .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                    .setNeutralButton("Cancel", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
                break;
            }
            case BleMissing: {
                new AlertDialog.Builder(this)
                    .setMessage("The device doesn't have the bluetooth")
                    .setNeutralButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
                break;
            }
            case GPSUnavailable: {
                new AlertDialog.Builder(this)
                        .setMessage("Please, turn on \"Use location\" in the device settings")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                break;
            }
            case NoInternetConnection: {
                new AlertDialog.Builder(this)
                        .setMessage("No internet connection")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                break;
            }
            case ServerFetchFailed: {
                new AlertDialog.Builder(this)
                        .setMessage("Server fetch failed")
                        .setPositiveButton("Retry", (dialog, which) -> scanner.startScanning())
                        .setNeutralButton("Cancel", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                break;
            }
            case OrientationUnavailable: {
                new AlertDialog.Builder(this)
                        .setMessage("Orientation is unavailable")
                        .setNeutralButton("OK", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                break;
            }
        }
    }

}
