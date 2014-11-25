package com.michaelfotiadis.shpparser.helpers;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;

/**
 * Class containing helper operations for features generated from shapefiles
 * @author Michael Fotiadis
 *
 */
public class FeatureOperations {

	/**
	 * Puts non-geometric properties in a HashMap. Planned only for Integer, Double and String.
	 * @param featurePolyline : MyPolyline target feature
	 * @param propertyType : Non-case sensitive Type of the property i.e. "double"
	 * @param propertyValue :  Actual toString value of the property i.e. "1.53357"
	 * @param propertyName : String descriptor of the property i.e. 2Radius"
	 * @return The toString value of the property as read from the HashMap
	 */
	public String createHashMapEntry(ErgoPolyline featurePolyline, String propertyType, String propertyValue, String propertyName) {

		String returnValue;  

		// Planned only for Integer, Double and String
		if (propertyType.equalsIgnoreCase("integer")) {
			featurePolyline.putInteger(propertyName, Integer.parseInt(propertyValue));
			Integer valueFromHashmap = featurePolyline.getInteger(propertyName);
			returnValue = valueFromHashmap.toString();
		} else if (propertyType.equalsIgnoreCase("double")) {
			featurePolyline.putDouble(propertyName, Double.parseDouble(propertyValue.toString()));
			Double valueFromHashmap = featurePolyline.getDouble(propertyName);
			returnValue = valueFromHashmap.toString();
		} else if (propertyType.equalsIgnoreCase("string")) {
			featurePolyline.putString(propertyName, String.valueOf(propertyValue.toString()));
			String valueFromHashmap = featurePolyline.getString(propertyName);
			returnValue = valueFromHashmap.toString();
		} else {
			returnValue = null; 
		} 

		return returnValue;

	}

	/**
	 * 
	 * @param geomType : Type of the geometry i.e "POINT"
	 * @param geomValue
	 * @return
	 */
	public static String[] splitGeometryString(String geomType, String geomValue) {
		String geomString;

		if  (geomValue.startsWith("POINT", 0)) {
			geomString = geomValue.replaceAll("POINT", "");
		} else if (geomValue.startsWith("MULTIPOLYGON",0)) {
			geomString = geomValue.replaceAll("MULTIPOLYGON", "");
		} else if (geomValue.startsWith("MULTILINESTRING",0)) {
			geomString = geomValue.replaceAll("MULTILINESTRING", "");
		}
		else {
			geomString = "";
			System.err.println("Warning: Unidentified Geometry Type.");
		}

		geomString = geomString.replace("(", ""); // make "(" disappear
		geomString = geomString.replace(")", ""); // make ")" disappear
		geomString = geomString.trim(); // trim white spaces

		String[] finalGeomString = geomString.split(","); // split by commas
		
		return finalGeomString;

	}



}
