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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryParams.PortalQuerySortOrder;
import com.esri.wdc.offlinemapper.R;
import com.esri.wdc.offlinemapper.model.DatabaseHelper;
import com.esri.wdc.offlinemapper.model.DatabaseListener;
import com.esri.wdc.offlinemapper.model.DbWebmap;

public class WebMapAdapter extends BaseAdapter {
    
    private static final String TAG = WebMapAdapter.class.getSimpleName();
    /**
     * TODO implement paging. For now, you get 100 and that's it.
     */
    public static final int LIMIT = 100;

    private final Activity activity;
    private final Portal portal;

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
        
        DatabaseHelper.getInstance(activity).addListener(new DatabaseListener() {
            
            public void onChangeRows() {
                WebMapAdapter.this.activity.runOnUiThread(new Runnable() {
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public int getCount() {
        DatabaseHelper db = DatabaseHelper.getInstance(activity);
        return db.getWebmapIds(db.getUserId(portal.getCredentials().getUserName(), portal.getUrl())).length;
    }

    public Object getItem(int position) {
        return getWebmap(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    private DbWebmap getWebmap(int position) {
        DatabaseHelper db = DatabaseHelper.getInstance(activity);
        long[] webmapIds = db.getWebmapIds(db.getUserId(portal.getCredentials().getUserName(), portal.getUrl()));
        if (position < webmapIds.length) {
            return db.getWebmap(webmapIds[position]);
        } else {
            return null;
        }
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
        
        DbWebmap webmap = getWebmap(position);
        if (null != webmap) {
            textView.setText(webmap.getItemId());
            Bitmap bmp;
            if (null != webmap.getThumbnail()) {
                bmp = BitmapFactory.decodeByteArray(webmap.getThumbnail(), 0, webmap.getThumbnail().length);
              
            } else {
                bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.desktopapp);
            }
            imageView.setImageBitmap(bmp);
        }
        return layout;
    }
    
    public Portal getPortal() {
        return portal;
    }

}
