package com.michaelfotiadis.shpparser.containers.ergo.geometry;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class ErgoCoordinateSet {

	private final double c1;
	private final double c2;
	private final double c3;
	private String referenceSystem;

	/**
	 * 
	 * @param X : Planar Coordinate
	 * @param Y : Planar Coordinate
	 * @param annotLabel : Text String annotation label
	 * @param colour : Text String colour code (preferably hex)
	 */
	public ErgoCoordinateSet(double C1, double C2, double C3, String ReferenceSystem) {
		this.c1 = C1;
		this.c2 = C2;
		this.c3 = C2;
		this.referenceSystem = ReferenceSystem;
	}

	public String getmReferenceSystem() {
		return referenceSystem;
	}

	public void setmReferenceSystem(String inputCRS) {
		this.referenceSystem = inputCRS;
	}

	public double getC1() {
		return c1;
	}

	public double getC2() {
		return c2;
	}

	public double getC3() {
		return c3;
	}


}
