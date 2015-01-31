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
package com.esri.wdc.offlinemapper.controller;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;
import com.esri.wdc.offlinemapper.WebMapAdapter;

public class MapDownloadService extends Service {
    
    private static final String TAG = MapDownloadService.class.getSimpleName();
    
    public static final String EXTRA_USER_CREDENTIALS = "UserCredentials";
    public static final String EXTRA_PORTAL_URL = "PortalUrl";
    
    private boolean keepRunning = true;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void runServiceLoop(String portalUrl, UserCredentials userCredentials) {
        while (keepRunning) {
            final Portal portal = new Portal(portalUrl, userCredentials);
            final PortalQueryParams params = new PortalQueryParams();
            params.setQuery(PortalItemType.WEBMAP, null, "owner:" + userCredentials.getUserName() + " AND type:Web Map");
            params.setLimit(WebMapAdapter.LIMIT);
            PortalQueryResultSet<PortalItem> theResultSet = null;
            try {
                theResultSet = portal.findItems(params);
            } catch (Exception e) {
                Log.e(TAG, "Couldn't find portal items", e);
            }
            if (null != theResultSet) {
                List<PortalItem> items = theResultSet.getResults();
                for (PortalItem item : items) {
                    Log.d(TAG, "TODO store " + item.getType() + " item " + item.getTitle());
                }
            }
            if (keepRunning) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Couldn't sleep", e);
                }
            }
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        final UserCredentials userCredentials = (UserCredentials) extras.get(EXTRA_USER_CREDENTIALS); 
        final String portalUrl = extras.getString(EXTRA_PORTAL_URL);
        
        new Thread() {
            public void run() {
                runServiceLoop(portalUrl, userCredentials);
            };
        }.start();
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        keepRunning = false;
    }

}
