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

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.core.io.UserCredentials;
import com.esri.wdc.offlinemapper.R;

public class MapActivity extends Activity {
    
    public static final String EXTRA_PORTAL_URL = "portalUrl";
    public static final String EXTRA_WEB_MAP_ID = "webMapId";
    public static final String EXTRA_USER_CREDENTIALS = "userCredentials";

    private MapView mMapView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        String portalUrl = extras.getString(EXTRA_PORTAL_URL);
        String webMapId = extras.getString(EXTRA_WEB_MAP_ID);
        UserCredentials userCredentials = (UserCredentials) extras.get(EXTRA_USER_CREDENTIALS);
        
        String webmapUrl = String.format("%s/home/item.html?id=%s", portalUrl, webMapId);
        mMapView = new MapView(this, webmapUrl, userCredentials, null, null);
        setContentView(mMapView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }


}