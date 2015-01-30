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

import java.util.Date;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;
import com.esri.wdc.offlinemapper.WebMapAdapter;

public class MapDownloadService extends IntentService {
    
    private static final String TAG = MapDownloadService.class.getSimpleName();
    
    public static final String EXTRA_USER_CREDENTIALS = "UserCredentials";
    public static final String EXTRA_PORTAL_URL = "PortalUrl";

    public MapDownloadService() {
        super(MapDownloadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Start MapDownloadService at " + new Date());
        while (true) {
            Bundle extras = intent.getExtras();
            UserCredentials userCredentials = (UserCredentials) extras.get(EXTRA_USER_CREDENTIALS); 
            String portalUrl = extras.getString(EXTRA_PORTAL_URL);
            Portal portal = new Portal(portalUrl, userCredentials);
            PortalQueryParams params = new PortalQueryParams();
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
            
            synchronized (this) {
                try {
                    wait(5000);
                } catch (Exception e) {
                }
            }
        }
    }

}
