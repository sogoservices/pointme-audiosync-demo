package com.sogoservices.pointme.debug.tablerssi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sogoservices.pointme.R;
import com.sogoservices.pointme.sdk.data.PNMBeaconTableItem;

public class BeaconDataViewItem extends ConstraintLayout {

    private TextView beamAngleTextView;
    private TextView latestRssiTextView;
    private TextView averageRssiTextView;

    public BeaconDataViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public BeaconDataViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public BeaconDataViewItem(Context context) {
        super(context);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.view_item_beacon_data, this);
        beamAngleTextView = view.findViewById(R.id.beamAngleTextView);
        latestRssiTextView = view.findViewById(R.id.latestRssiTextView);
        averageRssiTextView = view.findViewById(R.id.averageRssiTextView);
    }

    public void setupHeader() {
        beamAngleTextView.setText(getResources().getString(R.string.beacon_data_item_beam_angle));
        latestRssiTextView.setText(getResources().getString(R.string.beacon_data_item_latest_rssi));
        averageRssiTextView.setText(getResources().getString(R.string.beacon_data_item_average_rssi));
    }

    public void setup(PNMBeaconTableItem pnmBeaconTableItem) {
        String beamAngle = String.valueOf(pnmBeaconTableItem.beamAngle);
        String latestRssi = String.valueOf(pnmBeaconTableItem.latestRssi);
        String averageRssi = String.valueOf(pnmBeaconTableItem.averageRssi);
        beamAngleTextView.setText(beamAngle);
        latestRssiTextView.setText(latestRssi);
        averageRssiTextView.setText(averageRssi);
    }

}
