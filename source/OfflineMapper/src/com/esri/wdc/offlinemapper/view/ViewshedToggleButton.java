package com.esri.wdc.offlinemapper.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.core.raster.FileRasterSource;

/**
 * A button to toggle the viewshed. This class also computes and displays the viewshed.
 * TODO refactor this code to a controller.
 *
 */
public class ViewshedToggleButton extends ToggleButton {
    
    private static final String TAG = ViewshedToggleButton.class.getSimpleName();
    
    private final HashMap<String, RasterLayer> viewshedLayers = new HashMap<String, RasterLayer>();
    
    private RasterLayer viewshedLayer = null; 

    public ViewshedToggleButton(Context context) {
        super(context);
        setupOnCheckedChangeListener();
    }

    public ViewshedToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupOnCheckedChangeListener();
    }

    public ViewshedToggleButton(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        setupOnCheckedChangeListener();
    }
    
    private void setupOnCheckedChangeListener() {
        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            //Workaround: display canned rasters
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final MapView mapView = ((MapActivity) getContext()).getMapView();
                File dataDir = new File(Environment.getExternalStorageDirectory(), "data");
                if (!dataDir.exists()) {
                    dataDir = new File("/storage/extSdCard", "data");
                }
                File viewshedDir = new File(dataDir, "viewsheds");
                String[] tifs = viewshedDir.list(new FilenameFilter() {
                    
                    public boolean accept(File dir, String filename) {
                        return filename.toLowerCase().endsWith(".tif");
                    }
                });
                for (String tif : tifs) {                  
                    RasterLayer viewshedLayer = viewshedLayers.get(tif);
                    if (null == viewshedLayer) {
                        File viewshedFile = new File(viewshedDir, tif);
                        if (viewshedFile.exists()) {
                            try {
                                FileRasterSource source = new FileRasterSource(viewshedFile.getAbsolutePath());
                                source.project(mapView.getSpatialReference());
                                viewshedLayer = new RasterLayer(source);
                                mapView.addLayer(viewshedLayer);
                                viewshedLayers.put(tif, viewshedLayer);
                            } catch (FileNotFoundException e) {
                                Log.d(TAG, "Couldn't find raster file", e);
                            }
                        }
                    }
                    if (null != viewshedLayer) {
                        viewshedLayer.setVisible(isChecked());
                    }                    
                }
            }
        });
            
        //TODO when it works: calculate viewsheds (this code needs a lot of work)
//        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                final MapView mapView = ((MapActivity) getContext()).getMapView();                
//                if (isChecked()) {
//                    //Compute and show viewshed
//                    try {
//                        String rasterName = "ASTGTM2_N10W065_dem_utm.tif";
//                        File dataDir = new File(Environment.getExternalStorageDirectory(), "data");
//                        if (!dataDir.exists()) {
//                            dataDir = new File("/storage/extSdCard", "data");
//                        }
//                        String elevationFilename = new File(dataDir, rasterName).getAbsolutePath();
//                        
//                        Layer[] layers = mapView.getLayers();
//                        for (Layer layer : layers) {
//                            if (null != layer && null != layer.getName() && layer.getName().toLowerCase().contains("hostile")) {
//                                if (layer instanceof GraphicsLayer) {
//                                    GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
//                                    int[] graphicIds = graphicsLayer.getGraphicIDs();
//                                    for (int graphicId : graphicIds) {
//                                        try {
//                                            Viewshed viewshed = new Viewshed(elevationFilename);
//                                            FunctionRasterSource source = viewshed.getOutputFunctionRasterSource();
//                                            viewshedLayer = new RasterLayer(source);
//                                            RasterRenderer rend = viewshedLayer.getRenderer();
//                                            SpatialReference sr = viewshedLayer.getSpatialReference();
//                                            viewshedLayer.setVisible(true);
//                                            mapView.addLayer(viewshedLayer);
//                                            viewshed.setObserverZOffset(200);
//                                            Point pt = (Point) graphicsLayer.getGraphic(graphicId).getGeometry();
////                                            pt = (Point) GeometryEngine.project(pt, mapView.getSpatialReference(), SpatialReference.create(32720));
//                                            viewshed.setObserver((Point) graphicsLayer.getGraphic(graphicId).getGeometry());
//                                        } catch (IllegalArgumentException e) {
//                                            // TODO Auto-generated catch block
//                                            e.printStackTrace();
//                                        } catch (FileNotFoundException e) {
//                                            // TODO Auto-generated catch block
//                                            e.printStackTrace();
//                                        } catch (RuntimeException e) {
//                                            // TODO Auto-generated catch block
//                                            e.printStackTrace();
//                                        } catch (Throwable t) {
//                                            Log.d(TAG, "Wow, it's bad", t);
//                                        }
//                                        break;//TODO
//                                    }
//
//                                }
//                            }
//                        }
//                        
//                    } catch (IllegalArgumentException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (RuntimeException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                } else {
//                    //Hide viewshed
//                    if (null != viewshedLayer) {
//                        mapView.removeLayer(viewshedLayer);
//                    }
//                }
//            }
//        });
        
    }

}
