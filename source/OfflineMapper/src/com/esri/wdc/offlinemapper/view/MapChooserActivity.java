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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.esri.core.io.UserCredentials;
import com.esri.wdc.offlinemapper.R;
import com.esri.wdc.offlinemapper.model.DatabaseHelper;
import com.esri.wdc.offlinemapper.model.DbWebmap;

public class MapChooserActivity extends Activity {
    
    private static final String TAG = MapChooserActivity.class.getSimpleName();
    
    public static final String EXTRA_USER_CREDENTIALS = "userCredentials";
    public static final String EXTRA_PORTAL_URL = "portalUrl";
    
    private UserCredentials userCredentials = null;
    private String portalUrl = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_chooser);
        
        Object credsObject = getIntent().getExtras().get(EXTRA_USER_CREDENTIALS);
        if (credsObject instanceof UserCredentials) {
            SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
            
            userCredentials = (UserCredentials) credsObject;
            portalUrl = getIntent().getExtras().getString(EXTRA_PORTAL_URL);
            GridView gridview = (GridView) findViewById(R.id.gridview);
            final WebMapAdapter adapter = new WebMapAdapter(this, portalUrl, userCredentials);
            gridview.setAdapter(adapter);

            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    DbWebmap webmap = (DbWebmap) adapter.getItem(position);
                    Intent intent = new Intent(MapChooserActivity.this.getApplicationContext(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_PORTAL_URL, adapter.getPortal().getUrl());
                    intent.putExtra(MapActivity.EXTRA_USER_CREDENTIALS, userCredentials);
                    intent.putExtra(MapActivity.EXTRA_WEB_MAP, webmap);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_clearCache == id) {
            if (null != userCredentials) {
                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                long userId = db.getUserId(userCredentials.getUserName(), portalUrl);
                int rows = db.deleteUser(userId);
                Log.d(TAG, "clearCache deleted " + rows + " rows");
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
}
