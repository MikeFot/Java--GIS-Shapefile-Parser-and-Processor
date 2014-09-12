package helpers;

import containers.ergo.geometry.ErgoPolyline;

/**
 * Class containing methods to iterate through the properties of a custom ErgoPolyline object and print them
 * @author Michael Fotiadis
 * 
 */
public class PrintFeatureProperties {

	/**
	 * Prints String properties from the ErgoPolyline HashMap
	 * @param featurePolyline : The ErgoPolyline object whose properties will be printed
	 */
	public static void PrintStringKeys (ErgoPolyline featurePolyline) {
		// String Keys
		String[] stringKeyCollection = featurePolyline.getStringKeys();
		System.out.print("\nString Keys : ");
		
		for (String  singleStringKey : stringKeyCollection) {
			System.out.print("\"" + singleStringKey + "\" ");
		}
	}

	/**
	 * Prints Integer properties from the ErgoPolyline HashMap
	 * @param featurePolyline : The ErgoPolyline object whose properties will be printed
	 */
	public static void PrintIntegerKeys (ErgoPolyline featurePolyline) {
		// String Keys
		String[] integerKeyCollection = featurePolyline.getIntegerKeys();
		System.out.print("\nInteger Keys : ");
		
		for (String  singleIntegerKey : integerKeyCollection) {
			System.out.print("\"" + singleIntegerKey + "\" ");
		}
	}

	/**
	 * Prints Double properties from the ErgoPolyline HashMap
	 * @param featurePolyline : The ErgoPolyline object whose properties will be printed
	 */
	public static void PrintDoubleKeys (ErgoPolyline featurePolyline) {
		// String Keys
		String[] doubleKeyCollection = featurePolyline.getDoubleKeys();
		System.out.print("\nDouble Keys : ");

		for (String  singleDoubleKey : doubleKeyCollection) {
			System.out.print("\"" + singleDoubleKey + "\" ");
		}
	}

}

