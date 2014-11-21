package com.michaelfotiadis.shpparser.containers.ergo.geometry;

/**
 * Class for storing the properties of a Reference System
 * @author Michael Fotiadis
 *
 */
public class ErgoReferenceSystem {

	private String ergoSystem;
	private String ergoType;

	public ErgoReferenceSystem (String system, String type) {

		this.ergoSystem = system;
		this.ergoType = type;

	}

	public String getSystem() {
		return ergoSystem;
	}


	public void setSystem(String ergoSystem) {
		this.ergoSystem = ergoSystem;
	}

	public String getType() {
		return ergoType;
	}

	public void setType(String ergoType) {
		this.ergoType = ergoType;
	}


}
