package com.sogoservices.pointme.debug;

import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sogoservices.pointme.R;
import com.sogoservices.pointme.sdk.api.dto.PNMPoi;
import com.sogoservices.pointme.sdk.data.PNMPointScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class POIsAdapter extends RecyclerView.Adapter<POIsAdapter.ViewHolder> {

    private Location currentLocation;
    private List<PNMPoi> matchedPoints;
    private List<PNMPointScanResult> scanResult;

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    public void setMatchedPoints(List<PNMPoi> matchedPoints) {
        if (matchedPoints != null)
            this.matchedPoints = new ArrayList<>(matchedPoints);
        else
            this.matchedPoints = null;
    }

    public void setScanResult(List<PNMPointScanResult> scanResult) {
        if (scanResult != null)
            this.scanResult = new ArrayList<>(scanResult);
        else
            this.scanResult = null;
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
        PNMPointScanResult poiScanResult = scanResult.get(position);
        long id = poiScanResult.getPoi().getId();
        Location poiLocation = new Location(LocationManager.GPS_PROVIDER);
        poiLocation.setLatitude(poiScanResult.getPoi().getLocation().getLatitude());
        poiLocation.setLongitude(poiScanResult.getPoi().getLocation().getLongitude());
        int distance = currentLocation == null ? 0 : Math.round(currentLocation.distanceTo(poiLocation));
        int rangeFrom = poiScanResult.getPoi().getRangeFrom();
        int rangeTo = poiScanResult.getPoi().getRangeTo();
        int rangeFromDif = (int) Math.round(poiScanResult.getRangeFromDif());
        int rangeToDif = (int) Math.round(poiScanResult.getRangeToDif());
        int absoluteBearing = (int) Math.round(poiScanResult.getAbsoluteBearing());
        int relativeBearing = (int) Math.round(poiScanResult.getRelativeBearing());
        holder.textView.setText(String.format(Locale.US,
                "id: %d, distance: %dm, rangeFrom: %d°(%+d°), rangeTo: %d°(%+d°), bearing: %d°, relativeBearing: %+d°",
                id, distance, rangeFrom, rangeFromDif, rangeTo, rangeToDif, absoluteBearing, relativeBearing));
        if (matchedPoints.contains(poiScanResult.getPoi())) {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.textView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return scanResult == null ? 0 : scanResult.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
