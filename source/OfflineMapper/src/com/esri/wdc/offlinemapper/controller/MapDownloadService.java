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
import com.esri.core.portal.BaseMap;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;
import com.esri.core.portal.WebMap;
import com.esri.core.portal.WebMapLayer;
import com.esri.wdc.offlinemapper.model.DatabaseHelper;
import com.esri.wdc.offlinemapper.view.WebMapAdapter;

public class MapDownloadService extends Service {

    private static final String TAG = MapDownloadService.class.getSimpleName();
    private static final String PATH = "OfflineMapperDownloads";

    public static final String EXTRA_USER_CREDENTIALS = "UserCredentials";
    public static final String EXTRA_PORTAL_URL = "PortalUrl";

    private boolean keepRunning = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runServiceLoop(String portalUrl,
            UserCredentials userCredentials) {
        while (keepRunning) {
            final Portal portal = new Portal(portalUrl, userCredentials);
            
            DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
            long userId = db.insertUser(userCredentials.getUserName(), portalUrl);
            if (0 > userId) {
                userId = db.getUserId(userCredentials.getUserName(), portalUrl);
            }
            
            final PortalQueryParams params = WebMapAdapter.getWebMapQueryParams(userCredentials.getUserName());
            PortalQueryResultSet<PortalItem> theResultSet = null;
            try {
                theResultSet = portal.findItems(params);
            } catch (Exception e) {
                Log.e(TAG, "Couldn't find portal items", e);
            }
            if (null != theResultSet) {
                List<PortalItem> items = theResultSet.getResults();
                for (PortalItem item : items) {
                    try {
                        WebMap webmap = WebMap.newInstance(item);
                        byte[] thumbnailBytes = item.fetchThumbnail();
                        long webmapId = db.insertWebmap(item.getItemId(), userId, thumbnailBytes, item.getTitle());
                        if (0 > webmapId) {
                            webmapId = db.getWebmapId(item.getItemId());
                        }
                        BaseMap basemap = webmap.getBaseMap();
                        Log.d(TAG, "basemap is called " + basemap.getTitle());
                        List<WebMapLayer> basemapLayers = basemap.getBaseMapLayers();
                        for (int i = 0; i < basemapLayers.size(); i++) {
                            WebMapLayer layer = basemapLayers.get(i);
                            long basemapLayerId = db.insertBasemapLayer(layer.getUrl());
                            if (0 > basemapLayerId) {
                                basemapLayerId = db.getBasemapLayerId(layer.getUrl());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't read returned web map", e);
                    }
                }
            }
            if (keepRunning) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Couldn't sleep", e);
                }
            }
            
            //TODO make this loop to update maps; for now, set to false to do a one-time download and stop
            keepRunning = false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        final UserCredentials userCredentials = (UserCredentials) extras.get(EXTRA_USER_CREDENTIALS);
        final String portalUrl = extras.getString(EXTRA_PORTAL_URL);

        new Thread() {
            public void run() {
                keepRunning = true;
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
