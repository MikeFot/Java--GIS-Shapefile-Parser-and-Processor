package parsers.shapefile;

import helpers.FeatureOperations;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import userinterface.layouts.MainLayout;
import util.system.Log;
import containers.ergo.geometry.ErgoPolyline;
import containers.ergo.geometry.ErgoVertex;

public class ParseShapefile {

	private static CoordinateReferenceSystem VERBOSE_CRS;
	private static String GEOMETRY_TYPE;
	private static int EPSG_CODE;

	@SuppressWarnings("rawtypes")
	public static Collection<ErgoPolyline> parseURLshapefile(final URL shapeURL) throws IOException, NoSuchAuthorityCodeException {

		Collection<ErgoPolyline> GEOMETRY_COLLECTION = new ArrayList<ErgoPolyline>(); // don't make static, we need it to reset
		String shpReferenceSystem;

		Map<String, URL> map = new HashMap<String, URL>();
		map.put( "url", shapeURL );
		final DataStore shpDataStore = DataStoreFinder.getDataStore(map); // look for existing datastore
		
		final String name = shpDataStore.getTypeNames()[0];
		final FeatureSource featureSource = shpDataStore.getFeatureSource(name);
		Log.Out("Feature Source Hashcode = " + featureSource.hashCode(), 2, false);
		final FeatureType spatialFeatureType = featureSource.getSchema();

		VERBOSE_CRS = spatialFeatureType.getCoordinateReferenceSystem(); // store the WKT description

		if (VERBOSE_CRS!= null) {
			shpReferenceSystem = VERBOSE_CRS.getName().toString();
			if (VERBOSE_CRS.getName().toString().equals("British_National_Grid")) {
				EPSG_CODE = 27700;
			} else {

				try {
					EPSG_CODE = CRS.lookupEpsgCode(VERBOSE_CRS, true);
				} catch (FactoryException eFactory) {
					Log.Exception(eFactory, 0);
					eFactory.printStackTrace();
				} // store the EPSG code
			}
		} else {
			shpReferenceSystem = "Not Defined";
		}

		Log.Out("Geometry Type : " + spatialFeatureType.getGeometryDescriptor().getName() , 1, false); // e.g. prints "Points"
		final FeatureCollection fsShape = featureSource.getFeatures(); 

		final String shpPrintout = shpReferenceSystem.replace('_', ' ');

		final int fsSize = fsShape.size();

		final String geom = featureSource.getFeatures().features().next().getDefaultGeometryProperty().getType().getName().toString();

		switch (geom) {
		case "Point" : GEOMETRY_TYPE = "Point"; break;
		case "MultiLineString" : GEOMETRY_TYPE = "Polyline"; break;
		case "MultiPolygon" : GEOMETRY_TYPE = "Polygon";break;
		default : GEOMETRY_TYPE = geom; break;
		}

		Log.Out("Waiting for user input...", 2, true);

		// Wait for user input before continuing
		final int dialogButton = JOptionPane.YES_NO_OPTION;
		final int dialogResult = JOptionPane.showConfirmDialog (null, "Shapefile contains " + fsSize + 
				" items. \nThe geometry type is " + GEOMETRY_TYPE +
				". \nThe reference system is " + shpPrintout + 
				".\n\nContinue parsing?","Valid Shapefile Detected",dialogButton);
		if (dialogResult == JOptionPane.NO_OPTION){
			return null;
		}

		Log.Out("Size of the Feature Collection : " + fsSize, 2, true);
		Log.Out(" Feature Class Boundaries" + fsShape.getBounds(), 2, false); // latitude min max , longitude min max

		final FeatureIterator featureCollectionA = featureSource.getFeatures().features();

		while (featureCollectionA.hasNext()) {
			Feature featureA = featureCollectionA.next();
			Log.Out("Now Parsing Feature with ID : " + featureA.getIdentifier().toString().substring(featureA.getIdentifier().toString().indexOf('.')+1) , 2, true);
			MainLayout.getSTATUS_LIST().ergoList.redraw();
			String geomType = featureA.getDefaultGeometryProperty().getType().getName().toString();
			String geomValue = featureA.getDefaultGeometryProperty().getValue().toString();

			Log.Out("GEOMETRY TYPE " + geomType , 3 , false);
			Log.Out("GEOMETRY VALUE " + geomValue , 3, false);

			String[] splitGeomString = FeatureOperations.SplitGeometryString(geomType, geomValue);

			List<ErgoVertex> pointCollection = new ArrayList<ErgoVertex>(); // Collection of Vertices (MyVertex)

			for (String coordinateSet : splitGeomString) {

				String[] coordinateSetSplit = coordinateSet.split(" "); // Split Coordinates by Space Character
				List<Double> doubleCoordinateList = new ArrayList<Double>(); // Initialise ArrayList to store Coordinates - Should expand for height

				for (String singleCoordinate : coordinateSetSplit) {
					if (!singleCoordinate.isEmpty()) { // check because parsing may have failed
						doubleCoordinateList.add(Double.parseDouble(singleCoordinate));
					} else {
						//    				   System.err.println("Warning: Empty Coordinate Detected.");
					}
				}

				if (doubleCoordinateList.size() > 1) { // table is valid if it has 2 entries e.g. X Y

					double raw_X_Coordinate = doubleCoordinateList.get(0);
					double raw_Y_Coordinate = doubleCoordinateList.get(1);
					Log.Out(String.valueOf(raw_X_Coordinate) , 3, false);
					Log.Out(String.valueOf(raw_Y_Coordinate) , 3, false);

					ErgoVertex vertex2D = new ErgoVertex(raw_X_Coordinate, raw_Y_Coordinate);
					vertex2D.setReferenceSystem(shpReferenceSystem);
					pointCollection.add(vertex2D);

				} else {
					Log.Err("Warning: Generated Coordinate Array with less than 2 entries.", 0, false);
				}

			} // finish iterating coordinates

			String featureID = featureA.getIdentifier().toString().substring(featureA.getIdentifier().toString().indexOf('.')+1);

			ErgoPolyline featurePolyline = new ErgoPolyline(pointCollection, featureID, geomType);

			// HASHMAP FROM HERE ON NOW
			for (Property singleProperty : featureA.getProperties()) {
				String propertyName = singleProperty.getName().toString();
				String propertyValue = singleProperty.getValue().toString();
				String propertyType = singleProperty.getType().getBinding().getSimpleName().toString();

				/**
				 *  MAJOR OVERRIDE
				 */
				propertyType = "String"; // OVERRIDE
				// TAKE CARE!

				// add exception for DATES and convert them to String
				if (!propertyType.equalsIgnoreCase("String") && !propertyType.equalsIgnoreCase("Double") && !propertyType.equalsIgnoreCase("Integer")  ) {
					propertyType = "String";
				}

				if (!propertyName.equalsIgnoreCase("the_geom")) { // skip the_geom
					// Write to HashMap and get recorded value
					String returnFromHashmap = helpers.FeatureOperations.CreateHashMapEntry(featurePolyline, propertyType, propertyValue, propertyName);

					if (returnFromHashmap != null) {
						//				Log.Out(" Wrote " + propertyType + " value \"" + returnFromHashmap + "\" for field \"" + propertyName + "\"");
						featurePolyline.setHashMapSize();
					} else {
						Log.Err(" Property " + propertyName + " of type " + propertyType + " has not been added.", 1, false);
					}
					featurePolyline.setReferenceSystem(shpReferenceSystem);
				}
			} // end iterate property list

			GEOMETRY_COLLECTION.add(featurePolyline); // add the new feature to the collection
		} // end while feature has next
		
		
		featureCollectionA.close(); // close the iterator to lift lock

		Log.Out("Exiting Shapefile Parser." , 1 , true);
		return GEOMETRY_COLLECTION;

	}

	public static int getEPSG_CODE() {
		return EPSG_CODE;
	}

	public static String getGEOMETRY_TYPE() {
		return GEOMETRY_TYPE;
	}

	public static String getVerboseCRS() {
		if (VERBOSE_CRS.toString() != null) {
			
			return VERBOSE_CRS.toString();
			
		} else {
			return "";
		}
	}

}
