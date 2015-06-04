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
package com.esri.wdc.offlinemapper.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkModel {
    
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return null != networkInfo && networkInfo.isConnected();
    }
    
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getActiveNetworkInfo();
    }

}
