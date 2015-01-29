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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;

public class WebMapAdapter extends BaseAdapter {
    
    private static final String TAG = WebMapAdapter.class.getSimpleName();

    private final Activity activity;
    private final Portal portal;
    private final Object resultSetLock = new Object();
    private HashMap<Integer, PortalItem> resultsByIndex = new HashMap<Integer, PortalItem>();
    private final HashMap<String, Bitmap> thumbnailsByItemId = new HashMap<String, Bitmap>();
    
    private int totalResultCount = 0;

    public WebMapAdapter(Activity activity, UserCredentials userCredentials) {
        this.activity = activity;
        portal = new Portal("https://www.arcgis.com", userCredentials);
        doInitialSearch();
    }
    
    private void doInitialSearch() {
        AsyncTask<Void, Void, PortalQueryResultSet<PortalItem>> task = new AsyncTask<Void, Void, PortalQueryResultSet<PortalItem>>() {

            @Override
            protected PortalQueryResultSet<PortalItem> doInBackground(Void... v) {
                try {
                    PortalQueryParams params = new PortalQueryParams("owner:" + portal.getCredentials().getUserName() + " AND type:Web Map");
                    return portal.findItems(params);
                } catch (Exception e) {
                    Log.e(TAG, "Error doing initial search", e);
                    return null;
                }
            }
            
            @Override
            protected void onPostExecute(PortalQueryResultSet<PortalItem> result) {
                //This gives us 1) the total number of results and 2) the first subset of results.
                synchronized (resultSetLock) {
                    totalResultCount = result.getTotalResults();
                    resultsByIndex.clear();
                    thumbnailsByItemId.clear();
                    List<PortalItem> results = result.getResults();
                    for (int i = 0; i < results.size(); i++) {
                        Log.d(TAG, "result " + i + " is " + results.get(i).getTitle());
                        resultsByIndex.put(i, results.get(i));
                    }
                }
                notifyDataSetChanged();
            }
            
        };
        task.execute(new Void[0]);
    }
    
    private Bitmap getThumbnail(PortalItem item) throws Exception {
        Bitmap thumbnail;
        synchronized (resultSetLock) {
            thumbnail = thumbnailsByItemId.get(item.getItemId());
        }
        if (null == thumbnail) {
            Log.d(TAG, "fetching thumbnail for " + item.getTitle());
            byte[] bytes = item.fetchThumbnail();
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            synchronized (resultSetLock) {
                thumbnailsByItemId.put(item.getItemId(), bmp);
            }
        }
        return thumbnail;
    }

    public int getCount() {
        synchronized (resultSetLock) {
            return totalResultCount;
        }
    }

    public Object getItem(int position) {
        synchronized (resultSetLock) {
            return resultsByIndex.get(position);
        }
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewGroup viewToReturn;
        
        if (convertView == null) {
            LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);
            
            ImageView imageView = new ImageView(activity) {
                @Override
                public void invalidate() {
                    Log.d(TAG, "Invalidating " + position);
                    super.invalidate();
                }
            };
            imageView.setLayoutParams(new GridView.LayoutParams(200, 133));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
            layout.addView(imageView);
            
            TextView textView = new TextView(activity);
            layout.addView(textView);
            
            viewToReturn = layout;
        } else {
            viewToReturn = (ViewGroup) convertView;
        }
        final ImageView imageView = (ImageView) viewToReturn.getChildAt(0);

        PortalItem item = null;
        synchronized (resultSetLock) {
            item = resultsByIndex.get(position);
        }
        if (null != item) {
            final TextView textView = (TextView) viewToReturn.getChildAt(1);
            final String itemName = item.getTitle();
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    textView.setText(itemName);
                }
            });
            
            new AsyncTask<PortalItem, Void, Bitmap>() {
                protected Bitmap doInBackground(PortalItem... params) {
                    try {
                        return getThumbnail(params[0]);
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't get thumbnail for item " + params[0].getItemId(), e);
                        return null;
                    }
                };
                
                protected void onPostExecute(final Bitmap result) {
                    activity.runOnUiThread(new Runnable() {
                        
                        public void run() {
                            imageView.setImageBitmap(result);
                        }
                    });
                };                
            }.execute(new PortalItem[] { item });
        }
        return viewToReturn;
    }

}
