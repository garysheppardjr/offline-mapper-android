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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.io.UserCredentials;
import com.esri.wdc.offlinemapper.R;
import com.esri.wdc.offlinemapper.controller.IdentifyListener;
import com.esri.wdc.offlinemapper.model.DbWebmap;
import com.esri.wdc.offlinemapper.model.NetworkModel;

public class MapActivity extends Activity {
    
    private static final String TAG = MapActivity.class.getSimpleName();
    
    public static final String EXTRA_PORTAL_URL = "portalUrl";
    public static final String EXTRA_USER_CREDENTIALS = "userCredentials";
    public static final String EXTRA_WEB_MAP = "webMap";

    private MapView mMapView;
    private LocationDisplayManager ldm = null;
    private IdentifyListener identifyListener = null;
    private UserCredentials userCredentials = null;
    
    private void runAfterMapInitialized(DbWebmap webmap) {
        ldm = mMapView.getLocationDisplayManager();
        ldm.start();
        
        identifyListener = new IdentifyListener(mMapView, userCredentials);
        mMapView.setOnSingleTapListener(identifyListener);
        
        mMapView.setOnStatusChangedListener(null);
        
        if (null != webmap && null != webmap.getInitExtent()) {
            mMapView.setExtent(GeometryEngine.project(
                    webmap.getInitExtent(),
                    SpatialReference.create(SpatialReference.WKID_WGS84),
                    mMapView.getSpatialReference()));
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
        String portalUrl = extras.getString(EXTRA_PORTAL_URL);
        final DbWebmap webmap = (DbWebmap) extras.get(EXTRA_WEB_MAP);
        final UserCredentials userCredentials = (UserCredentials) extras.get(EXTRA_USER_CREDENTIALS);
        this.userCredentials = userCredentials;
        
        if (NetworkModel.isConnected(this)) {
            String webmapUrl = String.format("%s/home/item.html?id=%s", portalUrl, webmap.getItemId());
            mMapView = new MapView(this, webmapUrl, userCredentials, null, null);
            mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
                public void onStatusChanged(Object source, STATUS status) {
                    if (STATUS.INITIALIZED.equals(status)) {
                        runAfterMapInitialized(webmap);
                    }
                }
            });
        } else {
            File theDataDir = new File(Environment.getExternalStorageDirectory(), "data");
            if (!theDataDir.exists()) {
                theDataDir = new File("/storage/extSdCard", "data");
            }
            final File dataDir = theDataDir;
            
            mMapView = new MapView(this);
            mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
                
                public void onStatusChanged(Object source, STATUS status) {
                    if (STATUS.INITIALIZED.equals(status)) {
                        runAfterMapInitialized(webmap);
                        
                        try {
                            Geodatabase gdb = new Geodatabase(new File(dataDir, "plan.geodatabase").getAbsolutePath(), true);
                            if (null != gdb) {
                                List<GeodatabaseFeatureTable> tables = gdb.getGeodatabaseTables();
                                for (GeodatabaseFeatureTable table : tables) {
                                    if (table.hasGeometry()) {
                                        mMapView.addLayer(new FeatureLayer(table));
                                    }
                                }
                            }
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "Couldn't load local Runtime geodatabase", e);
                        }
                    }
                }
            });
            
            ArcGISLocalTiledLayer basemapLayer = new ArcGISLocalTiledLayer(new File(dataDir, "basemap.tpk").getAbsolutePath());
            mMapView.addLayer(basemapLayer);
        }
        setContentView(mMapView);
    }
    
    public MapView getMapView() {
        return mMapView;
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
        switch (item.getItemId()) {
        case R.id.action_1:
            Point pt = GeometryEngine.project(-64.479, 10.165, mMapView.getSpatialReference());
            mMapView.zoomToScale(pt, 72224);
            break;
            
        case R.id.action_2:
            pt = GeometryEngine.project(-64.57, 10.148, mMapView.getSpatialReference());
            mMapView.zoomToScale(pt, 144448);
            break;
            
        case R.id.action_3:
            pt = GeometryEngine.project(-64.637, 10.153, mMapView.getSpatialReference());
            mMapView.zoomToScale(pt, 72224);
            break;            
        }
        return super.onOptionsItemSelected(item);
    }

}