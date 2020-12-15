package com.sogoservices.pointme.debug;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sogoservices.pointme.R;
import com.sogoservices.pointme.debug.tablerssi.BeaconDataViewItem;
import com.sogoservices.pointme.sdk.blepackage.data.PNMBleResultBase;
import com.sogoservices.pointme.sdk.data.PNMBeacon;

import java.util.List;
import java.util.Locale;

public class BeaconsAdapter extends RecyclerView.Adapter<BeaconsAdapter.ViewHolder> {

    private List<PNMBeacon> beacons;
    private List<PNMBeacon> matchedBeacons;

    public void setBeacons(List<PNMBeacon> beacons) {
        this.beacons = beacons;
    }

    public void setMatchedBeacons(List<PNMBeacon> beacons) {
        this.matchedBeacons = beacons;
    }

    public void setBlePackage(PNMBleResultBase pnmBleResultBase) {
        for (int i = 0; i < beacons.size(); i++) {
            if (beacons.get(i).uuid == pnmBleResultBase.uuid) {
                for (int j = 0; j < beacons.get(i).beamAndAvgRssiList.length; j++) {
                    if (beacons.get(i).beamAndAvgRssiList[j].beamAngle == pnmBleResultBase.beamAngle) {
                        beacons.get(i).beamAndAvgRssiList[j].latestRssi = pnmBleResultBase.rssi;
                    }
                }
            }
        }
    }

    private boolean isMatched(PNMBeacon beacon) {
        if (matchedBeacons == null) {
            return false;
        }

        for (PNMBeacon matchedBeacon: matchedBeacons) {
            if (beacon.uuid == matchedBeacon.uuid) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.debug_item_poi, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PNMBeacon beacon = beacons.get(position);

        holder.beaconInformationTextView.setText(String.format(Locale.US, "uuid: %d, rangeFrom: %d, rangeTo: %d", beacon.uuid, beacon.from, beacon.to));
        if (isMatched(beacon)) {
            holder.beaconInformationTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.beaconInformationTextView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else {
            holder.beaconInformationTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.beaconInformationTextView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }

        holder.beaconDataContainer.removeAllViews();
        if (beacon.beamAndAvgRssiList != null && beacon.beamAndAvgRssiList.length > 0) {
            BeaconDataViewItem beaconDataViewItemHeader = new BeaconDataViewItem(holder.itemView.getContext());
            beaconDataViewItemHeader.setupHeader();
            holder.beaconDataContainer.addView(beaconDataViewItemHeader);
            for (int i = 0; i < beacon.beamAndAvgRssiList.length; i++) {
                BeaconDataViewItem beaconDataViewItem = new BeaconDataViewItem(holder.itemView.getContext());
                beaconDataViewItem.setup(beacon.beamAndAvgRssiList[i]);
                holder.beaconDataContainer.addView(beaconDataViewItem);
            }
        }
    }

    @Override
    public int getItemCount() {
        return beacons == null ? 0 : beacons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView beaconInformationTextView;
        LinearLayout beaconDataContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            beaconInformationTextView = itemView.findViewById(R.id.beaconInformationTextView);
            beaconDataContainer = itemView.findViewById(R.id.beaconDataContainer);
        }
    }

}
