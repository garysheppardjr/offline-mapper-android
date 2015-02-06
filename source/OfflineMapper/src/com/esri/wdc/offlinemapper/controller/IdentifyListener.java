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
package com.esri.wdc.offlinemapper.controller;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.Graphic;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.wdc.offlinemapper.R;

public class IdentifyListener implements OnSingleTapListener {

    public static final int TOLERANCE = 20;

    private final MapView mapView;
    private final UserCredentials userCredentials;
    private final IdentifyParameters params = new IdentifyParameters();
    private final LayoutInflater inflater;

    public IdentifyListener(MapView mapView, UserCredentials userCredentials) {
        this.mapView = mapView;
        this.userCredentials = userCredentials;

        params.setTolerance(TOLERANCE);
        params.setDPI(96);
        params.setLayerMode(IdentifyParameters.ALL_LAYERS);// TODO maybe top-most is better?
        params.setReturnGeometry(false);
        
        inflater = (LayoutInflater) mapView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void onSingleTap(float x, float y) {
        Point identifyPoint = mapView.toMapPoint(x, y);

        params.setGeometry(identifyPoint);
        params.setSpatialReference(mapView.getSpatialReference());
        params.setMapHeight(mapView.getHeight());
        params.setMapWidth(mapView.getWidth());

        // add the area of extent to identify parameters
        Envelope env = new Envelope();
        mapView.getExtent().queryEnvelope(env);
        params.setMapExtent(env);

        Graphic result = null;
        Layer[] layers = mapView.getLayers();
        for (int i = layers.length - 1; i >= 0; i--) {
            Layer layer = layers[i];
            if (layer instanceof GraphicsLayer) {
                GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
                int graphicIds[] = graphicsLayer.getGraphicIDs(x, y, TOLERANCE, 1);
                if (0 < graphicIds.length) {
                    result = graphicsLayer.getGraphic(graphicIds[0]);
                    break;
                }
            }
            //TODO add other layer types
        }
        
        Callout callout = mapView.getCallout();
        if (null != result) {
            Object symbolName = result.getAttributeValue("SymbolName");
            if (null == symbolName) {
                symbolName = "";
            }
            Object id = result.getAttributeValue("OBJECTID");
            if (null == id) {
                id = "";
            }
            callout.show(
                    Geometry.Type.POINT.equals(result.getGeometry().getType()) ? (Point) result.getGeometry() : identifyPoint,
                    loadView(result, symbolName.toString() + " " + id.toString(), new String[] { }));
        } else {
            callout.hide();
        }
    }
    
    private View loadView(Graphic result, String title, String[] fieldsToShow) {
        GridLayout grid = (GridLayout) inflater.inflate(R.layout.identify, null);
        String[] keys = result.getAttributeNames();
        grid.setRowCount(keys.length + (null == title ? 0 : 1));
        if (null != title) {
            TextView text = new TextView(grid.getContext());
            text.setText(title);
            text.setTextColor(Color.BLACK);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setTextSize(24);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1);
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 2);
            text.setLayoutParams(layoutParams);
            grid.addView(text);
        }
        for (String key : fieldsToShow) {
            TextView text = new TextView(grid.getContext());
            text.setText(key);
            grid.addView(text);
            text = new TextView(grid.getContext());
            text.setTypeface(Typeface.DEFAULT_BOLD);
            Object value = result.getAttributeValue(key);
            text.setText(null == value ? "" : value.toString());
            grid.addView(text);
        }
        return grid;
    }

}
