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
import android.view.LayoutInflater;
import android.view.View;
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
            callout.show(Geometry.Type.POINT.equals(result.getGeometry().getType()) ? (Point) result.getGeometry() : identifyPoint, loadView(result));
        } else {
            callout.hide();
        }
    }
    
    private View loadView(Graphic result) {
        View view = inflater.inflate(R.layout.identify, null);
        StringBuffer sb = new StringBuffer();
        String sep = "";
        String[] keys = result.getAttributeNames();
        for (String key : keys) {
            sb.append(sep).append(key).append(": ").append(result.getAttributeValue(key));
            sep = "\n";
        }
        ((TextView) view.findViewById(R.id.textView_identifyText)).setText(sb.toString());
        return view;
    }

}
