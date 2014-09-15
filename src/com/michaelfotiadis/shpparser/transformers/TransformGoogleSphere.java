package com.michaelfotiadis.shpparser.transformers;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class TransformGoogleSphere {

	/**
	 * 
	 * @param lon
	 * @param lat
	 * @return
	 */
	public static double[] WGS84toGoogleBing(double lon, double lat) {
		  double x = lon * 20037508.34 / 180;
		  double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
		  y = y * 20037508.34 / 180;
		  return new double[] {x, y};
		}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double[] GoogleBingtoWGS84Mercator (double x, double y) {
		  double lon = (x / 20037508.34) * 180;
		  double lat = (y / 20037508.34) * 180;

		  lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
		  return new double[] {lon, lat};
		}
	
	
}
