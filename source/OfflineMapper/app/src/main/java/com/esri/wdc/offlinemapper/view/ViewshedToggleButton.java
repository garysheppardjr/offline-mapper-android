package com.esri.wdc.offlinemapper.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Future;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.core.analysis.Viewshed;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FunctionRasterSource;
import com.esri.core.renderer.Colormap;
import com.esri.core.renderer.ColormapRenderer;
import com.esri.core.renderer.RasterRenderer;
import com.esri.core.table.FeatureTable;
import com.esri.core.tasks.query.QueryParameters;

/**
 * A button to toggle the viewshed. This class also computes and displays the viewshed.
 * TODO refactor this code to a controller.
 *
 */
public class ViewshedToggleButton extends ToggleButton {
    
    private static final String TAG = ViewshedToggleButton.class.getSimpleName();
    
//    private final HashMap<String, RasterLayer> viewshedLayers = new HashMap<String, RasterLayer>();
    private final HashSet<RasterLayer> viewshedLayers = new HashSet<RasterLayer>();
    private final ColormapRenderer viewshedColormapRenderer;
    
    private boolean addedViewshedLayers = false;

    public ViewshedToggleButton(Context context) {
        super(context);
        setupOnCheckedChangeListener();
        viewshedColormapRenderer = createViewshedColormapRenderer();
//        setupPrebuiltRasterLayers();
    }

    public ViewshedToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupOnCheckedChangeListener();
        viewshedColormapRenderer = createViewshedColormapRenderer();
//        setupPrebuiltRasterLayers();
    }

    public ViewshedToggleButton(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        setupOnCheckedChangeListener();
        viewshedColormapRenderer = createViewshedColormapRenderer();
//        setupPrebuiltRasterLayers();
    }
    
    private ColormapRenderer createViewshedColormapRenderer() {
        ArrayList<Colormap.UniqueValue> values = new ArrayList<Colormap.UniqueValue>();
        values.add(new Colormap.UniqueValue(0, Color.TRANSPARENT, "Not Visible"));
        values.add(new Colormap.UniqueValue(1, Color.RED, "Visible"));
        Colormap colormap = new Colormap(values);
        return new ColormapRenderer(colormap);
    }
    
//    private void setupPrebuiltRasterLayers() {
//        new Thread() {
//            @Override
//            public void run() {
//                final MapView mapView = ((MapActivity) getContext()).getMapView();
//                File dataDir = new File(Environment.getExternalStorageDirectory(), "data");
//                if (!dataDir.exists()) {
//                    dataDir = new File("/storage/extSdCard", "data");
//                }
//                File viewshedDir = new File(dataDir, "viewsheds");
//                String[] tifs = viewshedDir.list(new FilenameFilter() {
//                    
//                    public boolean accept(File dir, String filename) {
//                        return filename.toLowerCase().endsWith(".tif");
//                    }
//                });
//                if (null != tifs) {
//                    for (String tif : tifs) {                  
//                        RasterLayer viewshedLayer = viewshedLayers.get(tif);
//                        if (null == viewshedLayer) {
//                            File viewshedFile = new File(viewshedDir, tif);
//                            if (viewshedFile.exists()) {
//                                try {
//                                    FileRasterSource source = new FileRasterSource(viewshedFile.getAbsolutePath());
//                                    source.project(mapView.getSpatialReference());//Required if source has a different SR than mapView
//                                    viewshedLayer = new RasterLayer(source);
//                                    viewshedLayer.setName(tif);
//                                    viewshedLayer.setOpacity(0.5f);
//                                    viewshedLayer.setRenderer(viewshedColormapRenderer);
//                                    viewshedLayer.setVisible(false);
//                                    viewshedLayers.put(tif, viewshedLayer);
//                                } catch (FileNotFoundException e) {
//                                    Log.d(TAG, "Couldn't find raster file", e);
//                                }
//                            }
//                        }              
//                    }
//                }
//            }
//        }.start();
//    }
    
    private void setupOnCheckedChangeListener() {
//        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            
//            //Workaround: display pre-built rasters
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                final MapView mapView = addedViewshedLayers ? null : ((MapActivity) getContext()).getMapView();
//                Iterator<String> layerNameIterator = viewshedLayers.keySet().iterator();
//                while (layerNameIterator.hasNext()) {
//                    String layerName = layerNameIterator.next();
//                    RasterLayer layer = viewshedLayers.get(layerName);
//                    layer.setVisible(isChecked());
//                    if (!addedViewshedLayers) {
//                        Layer[] layers = mapView.getLayers();
//                        int index = -1;
//                        while (layers.length > ++index) {
//                            if ((layers[index] instanceof FeatureLayer) || (layers[index] instanceof GraphicsLayer)) {
//                                break;
//                            }
//                        }
//                        mapView.addLayer(layer, index);
//                    }
//                }
//                addedViewshedLayers = true;
//            }
//        });
            
        //TODO when it works: calculate viewsheds (this code needs a lot of work)
        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final MapView mapView = ((MapActivity) getContext()).getMapView();                
                if (isChecked()) {
                    //Compute and show viewshed
                    try {
                        String rasterName = "n10_w065_1arc_v3_webmercator.tif";
                        File dataDir = new File(Environment.getExternalStorageDirectory(), "data");
                        if (!dataDir.exists()) {
                            dataDir = new File("/storage/extSdCard", "data");
                        }
                        final String elevationFilename = new File(dataDir, rasterName).getAbsolutePath();
                        
                        Layer[] layers = mapView.getLayers();
                        for (Layer layer : layers) {
                            if (null != layer && null != layer.getName() && layer.getName().toLowerCase().contains("hostile")) {
                                if (layer instanceof FeatureLayer) {
                                    FeatureLayer flayer = (FeatureLayer) layer;
                                    final GraphicsLayer dummyGraphicsLayer = new GraphicsLayer();
                                    FeatureTable table = flayer.getFeatureTable();
                                    QueryParameters params = new QueryParameters();
                                    params.setWhere("0 = 0");
                                    Future<FeatureResult> future = table.queryFeatures(params, new CallbackListener<FeatureResult>() {
                                        
                                        public void onError(Throwable e) {
                                            // TODO Auto-generated method stub
                                            
                                        }
                                        
                                        public void onCallback(FeatureResult objs) {
                                            Iterator<Object> iter = objs.iterator();
                                            while (iter.hasNext()) {
                                                Object obj = iter.next();
                                                Feature f = (Feature) obj;
                                                Graphic g = new Graphic(f.getGeometry(), null);
                                                dummyGraphicsLayer.addGraphic(g);
                                            }
                                            doViewsheds(dummyGraphicsLayer, elevationFilename, mapView);
                                        }
                                    });
                                } else if (layer instanceof GraphicsLayer) {
                                    GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
                                    doViewsheds(graphicsLayer, elevationFilename, mapView);
                                }
                            }
                        }
                        
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (RuntimeException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
//                    //Hide viewshed
//                    if (null != viewshedLayer) {
//                        mapView.removeLayer(viewshedLayer);
//                    }
                    for (RasterLayer viewshedLayer : viewshedLayers) {
                        try {
                            mapView.removeLayer(viewshedLayer);
                        } catch (RuntimeException e) {
                            //meh
                            Log.w(TAG, "Couldn't remove layer", e);
                        }
                    }
                }
            }
        });
        
    }
    
    private void doViewsheds(GraphicsLayer graphicsLayer, String elevationFilename, MapView mapView) {
        int[] graphicIds = graphicsLayer.getGraphicIDs();
        for (int graphicId : graphicIds) {
            try {
                Viewshed viewshed = new Viewshed(elevationFilename);
                FunctionRasterSource source = viewshed.getOutputFunctionRasterSource();
                RasterLayer viewshedLayer = new RasterLayer(source);
                viewshedLayers.add(viewshedLayer);
                RasterRenderer rend = viewshedLayer.getRenderer();
                SpatialReference sr = viewshedLayer.getSpatialReference();
                viewshedLayer.setVisible(true);
                mapView.addLayer(viewshedLayer);
                viewshed.setObserverZOffset(200);
                Point pt = (Point) graphicsLayer.getGraphic(graphicId).getGeometry();
//                pt = (Point) GeometryEngine.project(pt, mapView.getSpatialReference(), SpatialReference.create(32720));
                viewshed.setObserver((Point) graphicsLayer.getGraphic(graphicId).getGeometry());
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (RuntimeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Throwable t) {
                Log.d(TAG, "Wow, it's bad", t);
            }
            break;//TODO
        }
    }

}
