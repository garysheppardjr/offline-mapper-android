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
                //TODO pass credentials
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
