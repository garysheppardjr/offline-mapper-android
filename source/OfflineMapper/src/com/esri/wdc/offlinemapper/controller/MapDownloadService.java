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

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.portal.BaseMap;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;
import com.esri.core.portal.WebMap;
import com.esri.core.portal.WebMapLayer;
import com.esri.core.tasks.tilecache.ExportTileCacheParameters;
import com.esri.core.tasks.tilecache.ExportTileCacheStatus;
import com.esri.core.tasks.tilecache.ExportTileCacheTask;
import com.esri.wdc.offlinemapper.WebMapAdapter;

public class MapDownloadService extends Service {

    private static final String TAG = MapDownloadService.class.getSimpleName();
    private static final String PATH = "OfflineMapperDownloads";

    public static final String EXTRA_USER_CREDENTIALS = "UserCredentials";
    public static final String EXTRA_PORTAL_URL = "PortalUrl";

    private boolean keepRunning = true;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void runServiceLoop(String portalUrl,
            UserCredentials userCredentials) {
        while (keepRunning) {
            final Portal portal = new Portal(portalUrl, userCredentials);
            final PortalQueryParams params = new PortalQueryParams();
            params.setQuery(PortalItemType.WEBMAP, null, WebMapAdapter.getWebMapQuery(userCredentials.getUserName()));
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
                    try {
                        WebMap webmap = WebMap.newInstance(item);
                        BaseMap basemap = webmap.getBaseMap();
                        Log.d(TAG, "basemap is called " + basemap.getTitle());
                        List<WebMapLayer> basemapLayers = basemap.getBaseMapLayers();
//                        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                        for (int i = 0; i < basemapLayers.size(); i++) {
                            WebMapLayer layer = basemapLayers.get(i);
                            Log.d(TAG, "layer is " + layer.getTitle() + " " + layer.getUrl());
                            ExportTileCacheTask exportTask = new ExportTileCacheTask(layer.getUrl(), userCredentials);
                            SpatialReference sr = (null != layer.getSpatialRefs() && 0 < layer.getSpatialRefs().size()) ? layer.getSpatialRefs().get(0) : SpatialReference.create(SpatialReference.WKID_WGS84_WEB_MERCATOR_AUXILIARY_SPHERE_10);
                            final Object continueLock = new Object();
                            ExportTileCacheParameters exportParams = new ExportTileCacheParameters(false, 0, 0, webmap.getInitExtent(), sr);
                            exportTask.generateTileCache(exportParams,
                                    new CallbackListener<ExportTileCacheStatus>() {
                                        
                                        public void onError(Throwable e) {
                                            Log.e(TAG, "Couldn't do status", e);
                                        }
                                        
                                        public void onCallback(ExportTileCacheStatus objs) {
                                            Log.d(TAG, objs.getStatus() + ": " + objs.getTotalBytesDownloaded() + "/" + objs.getDownloadSize());
                                        }
                                    },
                                    new CallbackListener<String>() {
                                        
                                        private boolean errored = false;
        
                                        public void onError(Throwable e) {
                                            errored = true;
                                            Log.e(TAG, "Couldn't generate tile cache", e);
                                            continueLock.notify();
                                        }
        
                                        public void onCallback(String path) {
                                            if (!errored) {
                                                Log.d(TAG, "Returned without error: " + path);
                                            }
                                            continueLock.notify();
                                        }
                                    },
                                    new File(getExternalCacheDir(), PATH).getAbsolutePath());
                            continueLock.wait();
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
