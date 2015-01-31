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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.esri.android.oauth.OAuthView;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.wdc.offlinemapper.controller.MapDownloadService;
import com.esri.wdc.offlinemapper.model.NetworkModel;

public class LoginActivity extends Activity {
    
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String USER_CREDENTIALS_KEY = "UserCredentials";
    private static final String PORTAL_URL_KEY = "PortalUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ArcGISRuntime.setClientId(getString(R.string.clientId));
        
        doLogin();
    }
    
    public void doLogin(View view) {
        doLogin();
    }
    
    private void doLogin() {
        final SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
        String userCredsString = prefs.getString(USER_CREDENTIALS_KEY, null);
        String portalUrl = prefs.getString(PORTAL_URL_KEY, null);
        if (null == portalUrl || null == userCredsString) {
            prefs.edit()
            .remove(PORTAL_URL_KEY)
            .remove(USER_CREDENTIALS_KEY)
            .commit();
            userCredsString = null;
            portalUrl = "https://www.arcgis.com";//TODO get from user when we support Portal
            final String portalUrlFinal = portalUrl;
            
            if (NetworkModel.isConnected(this)) {
                OAuthView oauthView = new OAuthView(this, portalUrl, getString(R.string.clientId), new CallbackListener<UserCredentials>() {
                    
                    public void onError(Throwable e) {
                        
                    }
                    
                    public void onCallback(final UserCredentials userCredentials) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                doCreatePin(portalUrlFinal, userCredentials, null);
                            }
                        });
                    }
                });
                setContentView(oauthView);
            } else {
                setContentView(R.layout.activity_login_disconnected);
            }
        } else {
            final String portalUrlFinal = portalUrl;
            final String userCredsStringFinal = userCredsString;
            runOnUiThread(new Runnable() {
                public void run() {
                    doEnterPin(portalUrlFinal, userCredsStringFinal, null);
                }
            });
        }
    }
    
    private void doCreatePin(final String portalUrl, final UserCredentials userCredentials, String error) {
        setContentView(R.layout.activity_login_pin);
        final EditText editText = (EditText) findViewById(R.id.editText_pin);
        if (null != error) {
            editText.setError(error);
        }
        ((TextView) findViewById(R.id.textView_pinLabel)).setText(getString(R.string.create_pin));
        final Button okButton = (Button) findViewById(R.id.button_ok);
        okButton.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                final String firstPin = editText.getText().toString().trim();
                editText.setText("");
                ((TextView) findViewById(R.id.textView_pinLabel)).setText(getString(R.string.confirm_pin));
                okButton.setOnClickListener(new OnClickListener() {
                    
                    public void onClick(View v) {
                        String secondPin = editText.getText().toString().trim();
                        if (0 < firstPin.length() && firstPin.equals(secondPin)) {
                            String enc = encryptIt(userCredentials, Integer.parseInt(firstPin));
                            SharedPreferences prefs = LoginActivity.this.getPreferences(MODE_PRIVATE);
                            prefs.edit()
                            .putString(PORTAL_URL_KEY, portalUrl)
                            .putString(USER_CREDENTIALS_KEY, enc)
                            .commit();
                            
                            startMapChooserActivity(portalUrl, userCredentials);
                        } else {
                            doCreatePin(portalUrl, userCredentials, getString(R.string.error_pin_mismatch));
                        }
                    }
                });
            }
        });
        editText.requestFocus();
    }
    
    private void doEnterPin(final String portalUrl, final String encryptedCredentials, String error) {
        setContentView(R.layout.activity_login_pin);
        final EditText editText = (EditText) findViewById(R.id.editText_pin);
        if (null != error) {
            editText.setError(error);
        }
        ((TextView) findViewById(R.id.textView_pinLabel)).setText(getString(R.string.enter_pin));
        ((Button) findViewById(R.id.button_ok)).setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                try {
                    UserCredentials userCredentials = (UserCredentials) decryptIt(encryptedCredentials, Integer.parseInt(editText.getText().toString()));
                    startMapChooserActivity(portalUrl, userCredentials);
                } catch (Throwable t) {
                    editText.setText("");
                    doEnterPin(portalUrl, encryptedCredentials, "Incorrect PIN");
                }
            }
        });
    }
    
    private void startMapChooserActivity(String portalUrl, UserCredentials userCredentials) {
        Intent mapDownloadServiceIntent = new Intent(this, MapDownloadService.class);
        mapDownloadServiceIntent.putExtra(MapDownloadService.EXTRA_USER_CREDENTIALS, userCredentials);
        mapDownloadServiceIntent.putExtra(MapDownloadService.EXTRA_PORTAL_URL, portalUrl);
        startService(mapDownloadServiceIntent);
        
        Intent i = new Intent(getApplicationContext(), MapChooserActivity.class);
        i.putExtra(MapChooserActivity.EXTRA_USER_CREDENTIALS, userCredentials);
        i.putExtra(MapChooserActivity.EXTRA_PORTAL_URL, portalUrl);
        startActivity(i);
    }
    
    public void logout(View view) {
        stopService(new Intent(getApplicationContext(), MapDownloadService.class));
        getPreferences(MODE_PRIVATE).edit()
        .remove(PORTAL_URL_KEY)
        .remove(USER_CREDENTIALS_KEY).commit();
        doLogin();
    }
    
    private static String fixSeed(int seed) {
        String seedString = Integer.toString(seed);
        while (8 > seedString.length()) {
            seedString += " ";
        }
        return seedString;
    }
    
    private static String encryptIt(Serializable object, int seed) {
        String seedString = fixSeed(seed);
        
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
            outstream.writeObject(object);
            outstream.close();

            DESKeySpec keySpec = new DESKeySpec(seedString.getBytes("UTF8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);
            
            // Cipher is not thread safe
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            String encrypedValue = Base64.encodeToString(cipher.doFinal(byteOut.toByteArray()), Base64.DEFAULT);
            return encrypedValue;
        } catch (Throwable t) {
            Log.e(TAG, "Couldn't encrypt", t);
            return null;
        }
    }
    
    private static Object decryptIt(String value, int seed)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException {
        String seedString = fixSeed(seed);

        DESKeySpec keySpec = new DESKeySpec(seedString.getBytes("UTF8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        byte[] encrypedPwdBytes = Base64.decode(value, Base64.DEFAULT);
        // cipher is not thread safe
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));
        
        ByteArrayInputStream byteIn = new ByteArrayInputStream(decrypedValueBytes);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        return objIn.readObject();
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
