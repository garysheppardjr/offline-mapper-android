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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database_helper_db";
    
    private static DatabaseHelper instance = null;
    
    private final ArrayList<DatabaseListener> listeners = new ArrayList<DatabaseListener>();
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static DatabaseHelper getInstance(Context context) {
        if (null == instance) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }
    
    public void addListener(DatabaseListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }
    
    public void removeListener(DatabaseListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireOnChangeRows() {
        synchronized (listeners) {
            for (final DatabaseListener listener : listeners) {
                new Thread() {
                    public void run() {
                        listener.onChangeRows();
                    };
                }.start();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 1; i <= DATABASE_VERSION; i++) {
            createVersion(db, i);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            createVersion(db, i);
        }
    }
    
    private void createVersion(SQLiteDatabase db, int version) {
        StringBuilder sbCreate;
        switch (version) {
        
        case 1:
            sbCreate = new StringBuilder("CREATE TABLE user (")
            .append("username TEXT")
            .append(",portal_url TEXT")
            .append(", UNIQUE (username, portal_url)")
            .append(")");
            db.execSQL(sbCreate.toString());
            
            sbCreate = new StringBuilder("CREATE TABLE basemap_layer (")
            .append("url TEXT PRIMARY KEY")
            .append(")");
            db.execSQL(sbCreate.toString());

            sbCreate = new StringBuilder("CREATE TABLE webmap (")
            .append("item_id TEXT PRIMARY KEY")
            .append(",user_id INTEGER REFERENCES user (rowid)")
            .append(",thumbnail BLOB")
            .append(")");
            db.execSQL(sbCreate.toString());
            
            sbCreate = new StringBuilder("CREATE TABLE webmap_basemap_layer_xref (")
            .append("webmap_rowid INTEGER REFERENCES webmap (rowid)")
            .append(",basemap_layer_rowid INTEGER REFERENCES basemap_layer (rowid)")
            .append(",layer_index INTEGER")
            .append(")");
            db.execSQL(sbCreate.toString());
            
            break;
        }
    }
    
    public long[] getWebmapIds(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("webmap", new String[] { "rowid" }, "user_id = ?", new String[] { Long.toString(userId) }, null, null, null);
        long[] ids = new long[cursor.getCount()];
        int index = 0;
        while (cursor.moveToNext()) {
            ids[index++] = cursor.getLong(0);
        }
        return ids; 
    }
    
    public int deleteUser(long userId) {
        int rows = 0;
        SQLiteDatabase db = getWritableDatabase();
        long[] webmapIds = getWebmapIds(userId);
        for (long webmapId : webmapIds) {
            rows += deleteWebmap(webmapId);
        }
        rows += db.delete("user", "rowid = ?", new String[] { Long.toString(userId) });
        fireOnChangeRows();
        return rows;
    }
    
    public int deleteWebmap(long webmapId) {
        int rows = 0;
        SQLiteDatabase db = getWritableDatabase();
        rows += db.delete("webmap_basemap_layer_xref", "webmap_rowid = ?", new String[] { Long.toString(webmapId) });
        rows += db.delete("webmap", "rowid = ?", new String[] { Long.toString(webmapId) });
        fireOnChangeRows();
        return rows;
    }
    
    public long insertBasemapLayer(String url) {
        ContentValues values = new ContentValues();
        values.put("url", url);
        SQLiteDatabase db = getWritableDatabase();
        try {
            long rowid = db.insert("basemap_layer", null, values);
            fireOnChangeRows();
            return rowid;
        } catch (Throwable t) {
            Log.d(TAG, "Couldn't insert (maybe it already exists): " + t.getMessage());
            return -1;
        }
    }
    
    public long getBasemapLayerId(String url) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("basemap_layer", new String[] { "rowid" }, "url = ?", new String[] { url }, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        } else {
            return -1;
        }
    }
    
    public long insertUser(String username, String portalUrl) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("portal_url", portalUrl);
        SQLiteDatabase db = getWritableDatabase();
        try {
            long rowid = db.insert("user", null, values);
            fireOnChangeRows();
            return rowid;
        } catch (Throwable t) {
            Log.d(TAG, "Couldn't insert (maybe it already exists): " + t.getMessage());
            return -1;
        }
    }
    
    public long getUserId(String username, String portalUrl) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("user", new String[] { "rowid" }, "username = ? AND portal_url = ?", new String[] { username, portalUrl }, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        } else {
            return -1;
        }
    }

    public long insertWebmap(String itemId, long userId, byte[] thumbnail) {
        ContentValues values = new ContentValues();
        values.put("item_id", itemId);
        values.put("user_id", userId);
        values.put("thumbnail", thumbnail);
        SQLiteDatabase db = getWritableDatabase();
        try {
            long rowid = db.insert("webmap", null, values);
            fireOnChangeRows();
            return rowid;
        } catch (Throwable t) {
            Log.d(TAG, "Couldn't insert (maybe it already exists): " + t.getMessage());
            return -1;
        }
    }
    
    public long getWebmapId(String itemId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("webmap", new String[] { "rowid" }, "item_id = ?", new String[] { itemId }, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getLong(0);
        } else {
            return -1;
        }
    }
    
    public DbWebmap getWebmap(long webmapId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("webmap", new String[] { "item_id", "user_id", "thumbnail" }, "rowid = ?", new String[] { Long.toString(webmapId) }, null, null, null);
        if (cursor.moveToFirst()) {
            DbWebmap webmap = new DbWebmap();
            webmap.setRowId(webmapId);
            webmap.setItemId(cursor.getString(0));
            webmap.setUserId(cursor.getLong(1));
            webmap.setThumbnail(cursor.getBlob(2));
            return webmap;
        } else {
            return null;
        }
    }

}
