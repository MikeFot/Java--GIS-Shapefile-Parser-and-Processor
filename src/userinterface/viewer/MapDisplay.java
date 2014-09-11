package userinterface.viewer;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenuBar;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

/**
 * 
 * @author Michael Fotiadis
 * 
 */
public class MapDisplay {
	/**
	 * GeoTools Map Display window
	 * @param shapefile : The input ShapeFile in FILE format
	 * @throws IOException
	 */
	public static void shapefileDisplay(File shapefile) throws IOException {

		JMapFrame frame;
		MapContent map = new MapContent();

		FileDataStore dataStore = FileDataStoreFinder.getDataStore(shapefile);
		SimpleFeatureSource shapefileSource = dataStore.getFeatureSource();
		Style shpStyle = SLD.createPolygonStyle(Color.RED, null, 0.0f);
		Layer shpLayer = new FeatureLayer(shapefileSource, shpStyle);
		map.addLayer(shpLayer);

		frame = new JMapFrame(map);
		frame.enableLayerTable(true);
		frame.setSize(1000, 800);
		frame.enableStatusBar(true);
		frame.enableToolBar(true);
		frame.setTitle("Map Viewer (courtesy of GeoTools");
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		frame.setVisible(true);

		frame.setDefaultCloseOperation(JMapFrame.HIDE_ON_CLOSE);

	}

}
