package parsers.shapefile;

import helpers.FeatureOperations;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;

import org.eclipse.swt.widgets.Display;
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

import util.system.Log;
import constants.AppConstants;
import containers.ergo.geometry.ErgoPolyline;
import containers.ergo.geometry.ErgoVertex;
import containers.file.ShapefileContainer;

public class ParseShapefile {

	private static boolean userChoice;
	private ShapefileContainer shapefileContainer;


	private boolean makeUserDialog(int fsSize, String shpPrintout) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				// Wait for user input before continuing
				final int dialogButton = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog (null, "Shapefile contains " + fsSize + 
						" items. \nThe geometry type is " + shapefileContainer.getGeometryType() +
						". \nThe reference system is " + shpPrintout + 
						".\n\nContinue parsing?","Valid Shapefile Detected",dialogButton);

				if (dialogResult == JOptionPane.NO_OPTION){
					userChoice = false;
				} else {
					userChoice = true;
				}
			}
		});
		return userChoice;
	}

	@SuppressWarnings("rawtypes")
	public ShapefileContainer parseURLshapefile(final URL shapeURL) {

		shapefileContainer = new ShapefileContainer();

		final String shpReferenceSystem;

		final Map<String, URL> map = new HashMap<String, URL>();
		map.put( "url", shapeURL );
		final DataStore shpDataStore;
		final String typeName;
		final FeatureSource featureSource;

		try {
			shpDataStore = DataStoreFinder.getDataStore(map);
			typeName = shpDataStore.getTypeNames()[0];
			featureSource = shpDataStore.getFeatureSource(typeName);
		} catch (IOException e) {
			Log.Exception(e, 0);
			return null;
		} 

		Log.Out("Feature Source Hashcode = " + featureSource.hashCode(), 2, false);
		final FeatureType spatialFeatureType = featureSource.getSchema();

		if (spatialFeatureType.getCoordinateReferenceSystem()!= null) {
			shpReferenceSystem = spatialFeatureType.getCoordinateReferenceSystem().getName().toString();
			if (shpReferenceSystem.equals(AppConstants.USER_PREFERRED_CRS)) {
				shapefileContainer.setEpsgCode(AppConstants.USER_PREFERRED_EPSG);
			} else {
				try {
					shapefileContainer.setEpsgCode(
							CRS.lookupEpsgCode(spatialFeatureType.getCoordinateReferenceSystem(), true));
				} catch (FactoryException eFactory) {
					Log.Exception(eFactory, 0);
					eFactory.printStackTrace();
					shapefileContainer.setEpsgCode(0);
				} // store the EPSG code
			}
		} else {
			shpReferenceSystem = "Not Defined";
		}

		shapefileContainer.setVerboseCRS(spatialFeatureType.getCoordinateReferenceSystem().toString());

		// e.g. prints "Points"
		Log.Out("Geometry Type : " + spatialFeatureType.getGeometryDescriptor().getName() , 1, false); 
		FeatureCollection fsShape;
		try {
			fsShape = featureSource.getFeatures();
		} catch (IOException e) {
			Log.Exception(e, 0);
			return null;
		} 

		final String shpPrintout = shpReferenceSystem.replace('_', ' ');

		final int fsSize = fsShape.size();

		String geom;
		try {
			geom = featureSource.getFeatures().features().next().getDefaultGeometryProperty().getType().getName().toString();
		} catch (NoSuchElementException | IOException e) {
			Log.Exception(e, 0);
			return null;
		}

		switch (geom) {
		case "Point" : shapefileContainer.setGeometryType("Point"); break;
		case "MultiLineString" :  shapefileContainer.setGeometryType("Polyline"); break;
		case "MultiPolygon" :  shapefileContainer.setGeometryType("Polygon");break;
		default :  shapefileContainer.setGeometryType(geom); break;
		}

		Log.Out("Size of the Feature Collection : " + fsSize, 2, true);
		// latitude min max , longitude min max
		Log.Out(" Feature Class Boundaries" + fsShape.getBounds(), 2, false); 

		Log.Out("Waiting for user input...", 2, true);

		boolean continueParsing = makeUserDialog(fsSize, shpPrintout);

		if (!continueParsing) {
			return null;
		}

		shapefileContainer.setGeometryCollection(secondStageParsing(featureSource, shpReferenceSystem));

		shpDataStore.dispose();

		Log.Out("Data Parsing Complete", 0, true);
		return shapefileContainer;
	}

	@SuppressWarnings("rawtypes")
	private static Collection<ErgoPolyline> secondStageParsing(FeatureSource featureSource, 
			String shpReferenceSystem) {

		Collection<ErgoPolyline> geometryCollection = new ArrayList<ErgoPolyline>(); 

		FeatureIterator featureCollectionA;
		try {
			featureCollectionA = featureSource.getFeatures().features();
		} catch (IOException e) {
			Log.Exception(e, 0);
			return null;
		}

		while (featureCollectionA.hasNext()) {
			Feature featureA = featureCollectionA.next();
			Log.Out("Now Parsing Feature with ID : " + featureA.getIdentifier().toString().substring(featureA.getIdentifier().toString().indexOf('.')+1) , 2, true);

			String geomType = featureA.getDefaultGeometryProperty().getType().getName().toString();
			String geomValue = featureA.getDefaultGeometryProperty().getValue().toString();

			Log.Out("GEOMETRY TYPE " + geomType , 3 , false);
			Log.Out("GEOMETRY VALUE " + geomValue , 3, false);

			String[] splitGeomString = FeatureOperations.SplitGeometryString(geomType, geomValue);

			// Collection of Vertices (MyVertex)
			List<ErgoVertex> pointCollection = new ArrayList<ErgoVertex>(); 

			for (String coordinateSet : splitGeomString) {

				// Split Coordinates by Space Character
				String[] coordinateSetSplit = coordinateSet.split(" "); 
				// Initialise ArrayList to store Coordinates - Should expand for height
				List<Double> doubleCoordinateList = new ArrayList<Double>(); 

				for (String singleCoordinate : coordinateSetSplit) {
					if (!singleCoordinate.isEmpty()) { // check because parsing may have failed
						doubleCoordinateList.add(Double.parseDouble(singleCoordinate));
					} else {
						Log.Err("Warning: Empty Coordinate Detected.", 1, false);
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

				// TODO handle this properly
				propertyType = "String"; // OVERRIDE

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

			geometryCollection.add(featurePolyline); // add the new feature to the collection
		} // end while feature has next


		featureCollectionA.close(); // close the iterator to lift lock

		Log.Out("Exiting Shapefile Parser." , 1 , true);
		return geometryCollection;
	}

}
