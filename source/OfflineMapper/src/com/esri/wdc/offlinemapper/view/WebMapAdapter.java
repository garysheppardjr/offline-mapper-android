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

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryParams.PortalQuerySortOrder;
import com.esri.core.portal.PortalQueryResultSet;
import com.esri.wdc.offlinemapper.R;

public class WebMapAdapter extends BaseAdapter {
    
    private static final String TAG = WebMapAdapter.class.getSimpleName();
    /**
     * TODO implement paging. For now, you get 100 and that's it.
     */
    public static final int LIMIT = 100;

    private final Activity activity;
    private final Portal portal;
    private final Object resultSetLock = new Object();
    private final HashMap<PortalItem, Bitmap> thumbnails = new HashMap<PortalItem, Bitmap>();
    
    private PortalQueryResultSet<PortalItem> resultSet = null;

    public static final PortalQueryParams getWebMapQueryParams(String ownerUsername) {
        PortalQueryParams params = new PortalQueryParams();
        StringBuilder sb = new StringBuilder("type:\"Web Map\" AND tags:offline-mapper");
        if (null != ownerUsername) {
            sb.append(" AND owner:").append(ownerUsername);
        }
        params.setQuery(PortalItemType.WEBMAP, null, sb.toString());
        params.setLimit(LIMIT);
        params.setSortField("numComments");
        params.setSortOrder(PortalQuerySortOrder.DESCENDING);
        return params;
    }

    public WebMapAdapter(Activity activity, String portalUrl, UserCredentials userCredentials) {
        this.activity = activity;
        portal = new Portal(portalUrl, userCredentials);
        synchronized (resultSetLock) {
            refreshItems();
        }
    }
    
    public void refreshItems() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... v) {
                PortalQueryParams params = getWebMapQueryParams(portal.getCredentials().getUserName());
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
                                    bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.desktopapp);
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
        synchronized (resultSetLock) {
            return resultSet.getResults().get(position);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout;
        ImageView imageView;
        TextView textView;
        if (convertView == null) {
            layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            
            imageView = new ImageView(activity);
            imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
            imageView.setPadding(8, 8, 8, 8);
            layout.addView(imageView);
            
            textView = new TextView(activity);
            textView.setTextColor(0xffffffff);
            textView.setTextSize(24f);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            layout.addView(textView);
        } else {
            layout = (LinearLayout) convertView;
            imageView = (ImageView) layout.getChildAt(0);
            textView = (TextView) layout.getChildAt(1);
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
            textView.setText(item.getTitle());
            Bitmap bmp = thumbnails.get(item);
            if (null != bmp) {
                imageView.setImageBitmap(bmp);
            }
        }
        return layout;
    }
    
    public Portal getPortal() {
        return portal;
    }

}
