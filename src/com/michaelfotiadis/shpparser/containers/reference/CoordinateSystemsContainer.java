package com.michaelfotiadis.shpparser.containers.reference;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class CoordinateSystemsContainer {

	private final String OSGB1936_2D_LAT_LON = "EPSG:4277";
	private final String OSGB1936_NATIONAL_GRID_EAST_NORTH = "EPSG:27700";
	private final String GOOGLE_WEB_MERCATOR_EAST_NORTH = "EPSG:3857";
	private final String WGS_84_2D_LAT_LON = "EPSG:4326";
	private final String ETRS_89_LAT_LON = "EPSG:4258";
	
	/**
	 * ETRS 89 Revised 01/03/2010
	 * Uses LAT and LON 2D (degree)
	 * @return EPSG String Code from the EPSG Geodetic Parameter Registry
	 */
	public String getETRS_89_LAT_LON() {
		return ETRS_89_LAT_LON;
	}


	/**
	 * OSGB 1936 Revised 06/01/2004
	 * Uses LAT and LON 2D (degree)
	 * @return EPSG String Code from the EPSG Geodetic Parameter Registry
	 */
	public String getOSGB1936_2D_LAT_LON() {
		return OSGB1936_2D_LAT_LON;
	}
	
	
	public String getWKT(String coordinateSystem) throws NoSuchAuthorityCodeException, FactoryException {
		CoordinateReferenceSystem crs = CRS.decode(coordinateSystem);
		String wkt = crs.toWKT();
		return wkt;
	}
	
	
	/**
	 * OSGB 1936 Projected Revised 07/05/2005#
	 * Uses EAST and NORTH 2D (metre)
	 * @return EPSG String Code from the EPSG Geodetic Parameter Registry
	 */
	public String getOSGB1936_NATIONAL_GRID_EAST_NORTH() {
		return OSGB1936_NATIONAL_GRID_EAST_NORTH;
	}
	
	/**
	 * Google Projected Web Mercator
	 * Uses EAST and NORTH 2D (metre)
	 * @return EPSG String Code from the EPSG Geodetic Parameter Registry
	 */
	public String getGOOGLE_WEB_MERCATOR_EAST_NORTH() {
		return GOOGLE_WEB_MERCATOR_EAST_NORTH;
	}
	/**
	 * WGS 84
	 * Uses LAT LON 2D (degree)
	 * @return EPSG String Code from the EPSG Geodetic Parameter Registry
	 */
	public String getWGS_84_2D_LAT_LON() {
		return WGS_84_2D_LAT_LON;
	}
	
}
