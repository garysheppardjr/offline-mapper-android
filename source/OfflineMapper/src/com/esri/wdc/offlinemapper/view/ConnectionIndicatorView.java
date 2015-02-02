/*******************************************************************************
 * Copyright 2015 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.wdc.offlinemapper.view;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.esri.wdc.offlinemapper.R;
import com.esri.wdc.offlinemapper.model.NetworkModel;

/**
 * A View that indicates whether the app can connect to the network or not.
 */
public class ConnectionIndicatorView extends ImageView {
    
    private final Timer timer = new Timer(true);
    private TimerTask timerTask = null;
    boolean updating = false;
    
    public ConnectionIndicatorView(Context context) {
        super(context);
        startUpdating();
    }

    public ConnectionIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        startUpdating();
    }
    
    public ConnectionIndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        startUpdating();
    }
    
    private void updateIcon(Activity activity) {
        final NetworkInfo networkInfo = NetworkModel.getActiveNetworkInfo(activity);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if (null != networkInfo && networkInfo.isConnected()) {
                    switch (networkInfo.getType()) {
                    case ConnectivityManager.TYPE_MOBILE:
                        setImageResource(R.drawable.ic_action_network_cell);
                        break;
                        
                    case ConnectivityManager.TYPE_WIFI:
                        setImageResource(R.drawable.ic_action_network_wifi);
                        break;
                        
                    default:
                        setImageResource(R.drawable.ic_action_accept);
                    }
                }
                else {
                    setImageResource(R.drawable.ic_action_error_red);
                }
            }
        });
    }
    
    private void startUpdating() {
        if (!updating) {
            stopUpdating();
            timerTask = new TimerTask() {
                
                @Override
                public void run() {
                    if (getContext() instanceof Activity) {
                        updateIcon((Activity) getContext());
                    }
                }
                
            };
            timer.schedule(timerTask, 0, 500);
            updating = true;
        }
    }
    
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (View.VISIBLE == visibility) {
            startUpdating();
        } else {
            stopUpdating();
        }
    }

    private void stopUpdating() {
        if (updating) {
            if (null != timerTask) {
                timerTask.cancel();
            }
            updating = false;
        }
    }

}
