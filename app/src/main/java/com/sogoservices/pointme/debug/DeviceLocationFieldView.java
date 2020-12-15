package com.sogoservices.pointme.debug;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sogoservices.pointme.R;

public class DeviceLocationFieldView extends ConstraintLayout {

    public DeviceLocationFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public DeviceLocationFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DeviceLocationFieldView(Context context) {
        super(context);
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int side = getMeasuredWidth();
        super.onMeasure(MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY));
    }

    private void initView() {
        View.inflate(getContext(), R.layout.view_device_location_field, this);
    }

}
