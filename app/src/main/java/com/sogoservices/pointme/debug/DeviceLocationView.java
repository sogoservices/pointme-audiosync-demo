package com.sogoservices.pointme.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.Space;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sogoservices.pointme.R;

public class DeviceLocationView extends ConstraintLayout {

    private TextView deviceLocationTextView;
    private DeviceLocationFieldView deviceLocationFieldView;
    private Space xSpace;
    private Space ySpace;

    private final int maxWidthCentimeters = 1000;
    private final int maxHeightCentimeters = 1000;
    private final int minWidthCentimeters = 0;
    private final int minHeightCentimeters = 0;

    public DeviceLocationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DeviceLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeviceLocationView(Context context) {
        super(context);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.view_device_location, this);
        deviceLocationTextView = view.findViewById(R.id.deviceLocationTextView);
        deviceLocationFieldView = view.findViewById(R.id.deviceLocationFieldView);
        xSpace = view.findViewById(R.id.xSpace);
        ySpace = view.findViewById(R.id.ySpace);
    }

    public void setup(float x, float y) {
        deviceLocationTextView.setText(getResources().getString(R.string.device_location_info, String.valueOf(x), String.valueOf(y)));
        Pair<Integer, Integer> spacesXY = calculateSpacesXY(x, y);
        changeConstraints(spacesXY.first, spacesXY.second);
    }

    private Pair<Integer, Integer> calculateSpacesXY(float x, float y) {
        int widthPixels = deviceLocationFieldView.getWidth();
        int heightPixels = deviceLocationFieldView.getHeight();
        int xCentimeters;
        int yCentimeters;

        if (x < minWidthCentimeters) {
            xCentimeters = minWidthCentimeters;
        } else if (x > maxWidthCentimeters) {
            xCentimeters = maxWidthCentimeters;
        } else {
            xCentimeters = (int) x;
        }
        if (-y < minHeightCentimeters) {
            yCentimeters = minHeightCentimeters;
        } else if (-y > maxHeightCentimeters) {
            yCentimeters = maxHeightCentimeters;
        } else {
            yCentimeters = (int) -y;
        }

        int xPixels = xCentimeters * widthPixels / maxWidthCentimeters;
        int yPixels = yCentimeters * heightPixels / maxHeightCentimeters;
        return new Pair<>(xPixels, yPixels);
    }

    private void changeConstraints(int xPixels, int yPixels) {
        LayoutParams xLayoutParamsChanged = (LayoutParams) xSpace.getLayoutParams();
        xLayoutParamsChanged.width = xPixels;
        xLayoutParamsChanged.startToStart = R.id.deviceLocationFieldView;
        xLayoutParamsChanged.topToTop = R.id.deviceLocationFieldView;
        xLayoutParamsChanged.bottomToBottom = R.id.deviceLocationFieldView;

        LayoutParams yLayoutParamsChanged = (LayoutParams) ySpace.getLayoutParams();
        yLayoutParamsChanged.height = yPixels;
        yLayoutParamsChanged.startToStart = R.id.deviceLocationFieldView;
        yLayoutParamsChanged.endToEnd = R.id.deviceLocationFieldView;
        yLayoutParamsChanged.topToTop = R.id.deviceLocationFieldView;

        xSpace.setLayoutParams(xLayoutParamsChanged);
        ySpace.setLayoutParams(yLayoutParamsChanged);
    }

}
