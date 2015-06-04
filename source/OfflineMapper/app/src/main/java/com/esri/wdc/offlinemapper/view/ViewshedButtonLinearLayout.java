package com.esri.wdc.offlinemapper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.esri.wdc.offlinemapper.R;

public class ViewshedButtonLinearLayout extends LinearLayout {

    public ViewshedButtonLinearLayout(Context context) {
        super(context);
        setupLayout();
    }

    public ViewshedButtonLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupLayout();
    }

    public ViewshedButtonLinearLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        setupLayout();
    }
    
    private void setupLayout() {
        inflate(getContext(), R.layout.viewshed_button, this);
    }

}
