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

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;

public class WebMapAdapter extends BaseAdapter {
    
    private static final String TAG = WebMapAdapter.class.getSimpleName();
    /**
     * TODO implement paging. For now, you get 100 and that's it.
     */
    private static final int LIMIT = 100;

    private final Context mContext;
    private final Portal portal;
    private final Object resultSetLock = new Object();
    private final HashMap<PortalItem, Bitmap> thumbnails = new HashMap<PortalItem, Bitmap>();
    
    private PortalQueryResultSet<PortalItem> resultSet = null;

    public WebMapAdapter(Context c, UserCredentials userCredentials) {
        mContext = c;
        portal = new Portal("https://www.arcgis.com", userCredentials);
        synchronized (resultSetLock) {
            refreshItems();
        }
    }
    
    public void refreshItems() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... v) {
                PortalQueryParams params = new PortalQueryParams();
                params.setQuery(PortalItemType.WEBMAP, null, "owner:" + portal.getCredentials().getUserName() + " AND type:Web Map");
                params.setLimit(LIMIT);
                PortalQueryResultSet<PortalItem> theResultSet = null;
                try {
                    theResultSet = portal.findItems(params);
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't find portal items", e);
                }
                synchronized (resultSetLock) {
                    if (null != theResultSet) {
                        resultSet = theResultSet;
                        thumbnails.clear();
                        List<PortalItem> items = resultSet.getResults();
                        for (PortalItem item : items) {
                            try {
                                byte[] bytes = item.fetchThumbnail();
                                Bitmap bmp;
                                if (null != bytes) {
                                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    
                                } else {
                                    bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.desktopapp);
                                }
                                thumbnails.put(item, bmp);
                            } catch (Exception e) {
                                Log.e(TAG, "Couldn't get thumbnail", e);
                            }                            
                        }
                    }
                }
                return null;
            }
            
            @Override
            protected void onPostExecute(Void result) {
                notifyDataSetChanged();
            }
            
        };
        task.execute(new Void[0]);
    }

    public int getCount() {
        synchronized (resultSetLock) {
            if (null != resultSet) {
                int totalResults = resultSet.getTotalResults();
                return (totalResults > LIMIT) ? LIMIT : totalResults;
            } else {
                return 0;
            }
        }
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 133));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        PortalItem item = null;
        synchronized (resultSetLock) {
            if (null != resultSet) {
                List<PortalItem> results = resultSet.getResults();
                if (position < results.size()) {
                    item = results.get(position);
                }
            }
        }
        if (null != item) {
            Bitmap bmp = thumbnails.get(item);
            if (null != bmp) {
                imageView.setImageBitmap(bmp);
            }
        }
        return imageView;
    }

}
