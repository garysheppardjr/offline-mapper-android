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
package com.esri.wdc.offlinemapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.PortalItem;

public class MapChooserActivity extends Activity {
    
    public static final String EXTRA_USER_CREDENTIALS = "userCredentials";
    public static final String EXTRA_PORTAL_URL = "portalUrl";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_chooser);
        
        Object credsObject = getIntent().getExtras().get(EXTRA_USER_CREDENTIALS);
        if (credsObject instanceof UserCredentials) {
            final UserCredentials userCredentials = (UserCredentials) credsObject;
            String portalUrl = getIntent().getExtras().getString(EXTRA_PORTAL_URL);
            GridView gridview = (GridView) findViewById(R.id.gridview);
            final WebMapAdapter adapter = new WebMapAdapter(this, portalUrl, userCredentials);
            gridview.setAdapter(adapter);

            gridview.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    PortalItem item = (PortalItem) adapter.getItem(position);
                    Intent intent = new Intent(MapChooserActivity.this.getApplicationContext(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_PORTAL_URL, adapter.getPortal().getUrl());
                    intent.putExtra(MapActivity.EXTRA_WEB_MAP_ID, item.getItemId());
                    intent.putExtra(MapActivity.EXTRA_USER_CREDENTIALS, userCredentials);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
