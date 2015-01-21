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

import com.esri.android.oauth.OAuthView;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        OAuthView oauthView = (OAuthView) findViewById(R.id.oauthView);
        oauthView.setCallbackListener(new CallbackListener<UserCredentials>() {
            
            public void onError(Throwable e) {
                // TODO Auto-generated method stub
                
            }
            
            public void onCallback(UserCredentials userCredentials) {
                Intent i = new Intent(getApplicationContext(), MapChooserActivity.class);
                i.putExtra(MapChooserActivity.EXTRA_USER_CREDENTIALS, userCredentials);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
