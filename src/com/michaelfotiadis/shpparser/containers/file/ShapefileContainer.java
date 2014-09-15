package com.michaelfotiadis.shpparser.containers.file;

import java.util.ArrayList;
import java.util.Collection;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;

/**
 * Object storing information about a parsed shapefile along with geometric data
 * @author Michael Fotiadis
 *
 */
public class ShapefileContainer {

	
	private Collection<ErgoPolyline> geometryCollection;
	private String verboseCRS;
	private String geometryType;
	private int epsgCode;
	
	public ShapefileContainer() {
		this.setGeometryCollection(new ArrayList<>());
	}

	public int getEpsgCode() {
		return epsgCode;
	}
	public String getGeometryType() {
		return geometryType;
	}
	
	public String getVerboseCRS() {
		if (verboseCRS.toString() != null) {

			return verboseCRS.toString();

		} else {
			return "";
		}
	}

	public void setEpsgCode(int code) {
		epsgCode = code;
	}

	public void setGeometryType(String type) {
		geometryType = type;
	}
	
	public void setVerboseCRS(String crs) {
		verboseCRS = crs;
	}

	public Collection<ErgoPolyline> getGeometryCollection() {
		return geometryCollection;
	}

	public void setGeometryCollection(Collection<ErgoPolyline> geometryCollection) {
		this.geometryCollection = geometryCollection;
	}
	
}
