package com.michaelfotiadis.shpparser.parsers.shapefile;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

import com.michaelfotiadis.shpparser.constants.AppConstants;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoShapefileGeometryType;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoVertex;
import com.michaelfotiadis.shpparser.containers.file.ShapefileContainer;
import com.michaelfotiadis.shpparser.containers.interfaces.ParserInterface;
import com.michaelfotiadis.shpparser.helpers.FeatureOperations;
import com.michaelfotiadis.shpparser.util.system.Log;

/**
 * Class for parsing shapefiles. Implements java.lang.Runnable.
 * @author Michael Fotiadis
 *
 */
public class ProcessorShpParser implements ParserInterface, Runnable {

	private ShapefileContainer shapefileContainer;
	private URL shapeURL;

	
	
	public ProcessorShpParser(final URL shapeURL) {
		this.shapeURL = shapeURL;
	}

	/**
	 * Parses a shapefile to memory
	 * Prompts user for input and proceeds to second stage parsing if true
	 * @param shapeURL Location of the shapefile in URL format
	 * @return ShapefileContainer custom object containing geometry and metadata. 
	 * Returns null if operation failed.
	 */
	//	@SuppressWarnings("rawtypes")
	//	public ShapefileContainer parseURLshapefile(final URL shapeURL) {
	//	
	//		
	//	}

	@Override
	public void run() {
		// initialise a container for the objects
		setShapefileContainer(new ShapefileContainer());

		String shpReferenceSystem;

		final Map<String, URL> map = new HashMap<String, URL>();
		map.put( "url", shapeURL );
		final DataStore shpDataStore;
		final String typeName;
		final FeatureSource<?, ?> featureSource;

		try {
			shpDataStore = DataStoreFinder.getDataStore(map);
			typeName = shpDataStore.getTypeNames()[0];
			featureSource = shpDataStore.getFeatureSource(typeName);
		} catch (IOException e) {
			Log.Exception(e, 0);
			setShapefileContainer(null);
			notify();
			return;
		} 

		Log.Out("Feature Source Hashcode = " + featureSource.hashCode(), 2, false);
		final FeatureType spatialFeatureType = featureSource.getSchema();

		if (spatialFeatureType.getCoordinateReferenceSystem()!= null) {
			shpReferenceSystem = spatialFeatureType.getCoordinateReferenceSystem().getName().toString();
			getShapefileContainer().setVerboseCRS(spatialFeatureType.getCoordinateReferenceSystem().toString());
			if (shpReferenceSystem.equals(com.michaelfotiadis.shpparser.constants.AppConstants.USER_PREFERRED_CRS)) {
				getShapefileContainer().setEpsgCode(AppConstants.USER_PREFERRED_EPSG);
			} else {
				if (spatialFeatureType.getCoordinateReferenceSystem() != null) {
					try {
						// try to find the EPSG code of the reference system
						getShapefileContainer().setEpsgCode(
								CRS.lookupEpsgCode(spatialFeatureType.getCoordinateReferenceSystem(), true));
					} catch (FactoryException eFactory) {
						Log.Exception(eFactory, 0);
						shpReferenceSystem = "Not Defined";
						getShapefileContainer().setVerboseCRS("Not Defined");
						getShapefileContainer().setEpsgCode(0);
					} catch (NullPointerException e) {
						Log.Exception(e, 0);
						shpReferenceSystem = "Not Defined";
						getShapefileContainer().setVerboseCRS("Not Defined");
						getShapefileContainer().setEpsgCode(0);
					}
				} else {
					shpReferenceSystem = "Not Defined";
					getShapefileContainer().setVerboseCRS("Not Defined");
				}
			}
		} else {
			shpReferenceSystem = "Not Defined";
			getShapefileContainer().setVerboseCRS("Not Defined");
		}

		// e.g. prints "Points"
		// Log.Out("Geometry Type : " + spatialFeatureType.getGeometryDescriptor().getName() , 1, false); 
		FeatureCollection<?, ?> fsShape;
		try {
			fsShape = featureSource.getFeatures();
		} catch (IOException e) {
			Log.Exception(e, 0);
			setShapefileContainer(null);
			return;
		} 

		final int fsSize = fsShape.size();

		String geometryType;
		try {
			geometryType = featureSource.getFeatures().features().next().getDefaultGeometryProperty().getType().getName().toString();
		} catch (NoSuchElementException | IOException e) {
			Log.Exception(e, 0);
			setShapefileContainer(null);
			return;
		}

		// replace geometry types
		if (geometryType.equals(ErgoShapefileGeometryType.POINT.toString())) {
			getShapefileContainer().setGeometryType(geometryType);
		} else if (geometryType.equals(ErgoShapefileGeometryType.MULTI_LINE_STRING.toString())) {
			getShapefileContainer().setGeometryType(ErgoShapefileGeometryType.POLYLINE.toString());
		} else if (geometryType.equals(ErgoShapefileGeometryType.MULTI_POLYGON.toString())) {
			getShapefileContainer().setGeometryType(ErgoShapefileGeometryType.POLYGON.toString());
		} else {
			getShapefileContainer().setGeometryType(geometryType);
		}

		Log.Out("Size of the Feature Collection : " + fsSize, 2, true);
		// latitude min max , longitude min max
		Log.Out(" Feature Class Boundaries" + fsShape.getBounds(), 2, false); 

		Log.Out("Waiting for user input...", 2, true);

		getShapefileContainer().setGeometryCollection(secondStageParsing(featureSource, shpReferenceSystem));

		shpDataStore.dispose();

		Log.Out("Data Parsing Complete", 0, true);
	}

	/**
	 * Second stage of shapefile parsing
	 * @param featureSource Source of items to be parsed
	 * @param shpReferenceSystem Reference System of the parsed shapefile
	 * @return Collection of <ErgoPolyline> containing parsed objects
	 */
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
			//			Log.Out("Now Parsing Feature with ID : " + featureA.getIdentifier().toString().substring(featureA.getIdentifier().toString().indexOf('.')+1) , 2, true);

			String geomType = featureA.getDefaultGeometryProperty().getType().getName().toString();
			String geomValue = featureA.getDefaultGeometryProperty().getValue().toString();

			//			Log.Out("GEOMETRY TYPE " + geomType , 3 , false);
			//			Log.Out("GEOMETRY VALUE " + geomValue , 3, false);

			String[] splitGeomString = FeatureOperations.splitGeometryString(geomType, geomValue);

			// Collection of Vertices (MyVertex)
			Collection<ErgoVertex> pointCollection = new ArrayList<ErgoVertex>(); 

			for (String coordinateSet : splitGeomString) {

				// Split Coordinates by Space Character
				String[] coordinateSetSplit = coordinateSet.split(" "); 
				// Initialise ArrayList to store Coordinates - Should expand for height
				List<Double> doubleCoordinateList = new ArrayList<Double>(); 

				for (String singleCoordinate : coordinateSetSplit) {
					if (!singleCoordinate.isEmpty()) { // check because parsing may have failed
						doubleCoordinateList.add(Double.parseDouble(singleCoordinate));
					} else {
						//						Log.Err("Warning: Empty Coordinate Detected.", 1, false);
					}
				}

				if (doubleCoordinateList.size() > 1) { // table is valid if it has 2 entries e.g. X Y

					double raw_X_Coordinate = doubleCoordinateList.get(0);
					double raw_Y_Coordinate = doubleCoordinateList.get(1);
					//					Log.Out(String.valueOf(raw_X_Coordinate) , 3, false);
					//					Log.Out(String.valueOf(raw_Y_Coordinate) , 3, false);

					ErgoVertex vertex2D = new ErgoVertex(raw_X_Coordinate, raw_Y_Coordinate);
					vertex2D.setReferenceSystem(shpReferenceSystem);
					pointCollection.add(vertex2D);

				} else {
					//					Log.Err("Warning: Generated Coordinate Array with less than 2 entries.", 0, false);
				}

			} // finish iterating coordinates

			String featureID = featureA.getIdentifier().toString().substring(featureA.getIdentifier().toString().indexOf('.')+1);
			//			Log.Out("Size of point collection: " + pointCollection.size(), 2, false);
			ErgoPolyline featurePolyline = new ErgoPolyline(pointCollection, featureID, geomType);

			// HASHMAP FROM HERE ON NOW
			for (Property singleProperty : featureA.getProperties()) {
				if (singleProperty != null) {
					String propertyName = singleProperty.getName().toString();
					String propertyValue;
					try {
						propertyValue = singleProperty.getValue().toString();
					} catch (NullPointerException e) {
						propertyValue = "";
					}
					String propertyType = singleProperty.getType().getBinding().getSimpleName().toString();

					// TODO handle this properly
					propertyType = "String"; // OVERRIDE

					// add exception for DATES and convert them to String
					if (!propertyType.equalsIgnoreCase("String") && !propertyType.equalsIgnoreCase("Double") && !propertyType.equalsIgnoreCase("Integer")  ) {
						propertyType = "String";
					}

					if (!propertyName.equalsIgnoreCase("the_geom")) { // skip the_geom
						// Write to HashMap and get recorded value
						String valueToMap = new FeatureOperations().createHashMapEntry(featurePolyline, propertyType, propertyValue, propertyName);

						if (valueToMap != null) {
							//Log.Out(" Wrote " + propertyType + " value \"" + returnFromHashmap + "\" for field \"" + propertyName + "\"");
							featurePolyline.setHashMapSize();
						} else {
							//Log.Err(" Property " + propertyName + " of type " + propertyType + " has not been added.", 1, false);
						}
						featurePolyline.setReferenceSystem(shpReferenceSystem);
					}
				}
			} // end iterate property list

			geometryCollection.add(featurePolyline); // add the new feature to the collection
		} // end while feature has next


		featureCollectionA.close(); // close the iterator to lift lock

		Log.Out("Exiting Shapefile Parser." , 1 , true);
		return geometryCollection;
	}

	private ShapefileContainer getShapefileContainer() {
		return shapefileContainer;
	}

	private void setShapefileContainer(ShapefileContainer shapefileContainer) {
		this.shapefileContainer = shapefileContainer;
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return shapefileContainer;
	}

}
