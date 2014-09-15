package com.michaelfotiadis.shpparser.containers.ergo.geometry;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.michaelfotiadis.shpparser.containers.reference.CoordinateSystemsContainer;
import com.michaelfotiadis.shpparser.transformers.TransformReferenceSystem;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class ErgoVertex {
	private static final String POINT_DESCRIPTOR = "**&&&POINT&&&***";

	private static final int DEFAULT_DECIMALS = 15;

	private final String GEOGRAPHIC = "geographic 2D";
	private final BigDecimal x;
	private final BigDecimal y;
	private final String mLabel;
	private String colour;
	private String referenceSystem;

	public ErgoVertex(BigDecimal X, BigDecimal Y) {
		this.x = X;
		this.y = Y;
		this.mLabel = POINT_DESCRIPTOR;
		this.colour = "";
	}

	/**
	 * 
	 * @param X : Planar Coordinate
	 * @param Y : Planar Coordinate
	 * @param annotLabel : Text String annotation label
	 * @param colour : Text String colour code (preferably hex)
	 */
	public ErgoVertex(BigDecimal X, BigDecimal Y, String annotLabel, String colour) {
		this.x = X;
		this.y = Y;
		this.mLabel = annotLabel;
		this.colour = colour;
	}

	public ErgoVertex(double X, double Y) {
		this.x = new BigDecimal(X);
		this.y = new BigDecimal(Y);
		this.mLabel = POINT_DESCRIPTOR;
		this.colour = "";
	}

	public ErgoVertex(double X, double Y, String annotLabel, String colour) {
		this.x = new BigDecimal(X);
		this.y = new BigDecimal(Y);
		this.mLabel = annotLabel;
		this.colour = colour; 
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErgoVertex other = (ErgoVertex) obj;
		//		if (label == null) {
		//			if (other.label != null)
		//				return false;
		//		} else if (!label.equals(other.label))
		//			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}


	public String getColour() {
		return colour;
	}


	public String getLabel() {
		return mLabel;
	}

	public String getReferenceSystem() {
		return referenceSystem;
	}

	// REPLACE WITH METHOD
	public ErgoCoordinateSet getTransformedCoordinates(ErgoReferenceSystem sourceCRS, ErgoReferenceSystem targetCRS) {

		ErgoCoordinateSet targetCoordinateSet = transformCoordinateSet(sourceCRS, targetCRS);

		return targetCoordinateSet;

	}

	public ErgoCoordinateSet getWGSCoordinates(ErgoReferenceSystem sourceCRS) {
		CoordinateSystemsContainer coordSystems = new CoordinateSystemsContainer();
		ErgoReferenceSystem targetCRS = new ErgoReferenceSystem(coordSystems.getWGS_84_2D_LAT_LON(), "geographic 2D");

		ErgoCoordinateSet targetCoordinateSet = transformCoordinateSet(sourceCRS, targetCRS);
		return targetCoordinateSet;

	}

	public BigDecimal getX() {
		return x;
	}

	public double getXasDouble() {
		return x.doubleValue();
	}

	public double getXasDouble(int decimalLimit) {
		BigDecimal bd = x.setScale(decimalLimit, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}

	public int getXasInt() {
		return x.intValue();
	}

	public BigDecimal getY() {
		return y;
	}

	public double getYasDouble() {
		return y.doubleValue();
	}

	public double getYasDouble(int decimalLimit) {
		BigDecimal bd = y.setScale(decimalLimit, RoundingMode.HALF_EVEN);
		return bd.doubleValue();
	}


	public int getYasInt() {
		return y.intValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mLabel == null) ? 0 : mLabel.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}


	public void setReferenceSystem(String mReferenceSystem) {
		this.referenceSystem = mReferenceSystem;
	}

	@Override
	public String toString() {
		return "Vertex [mX=" + getXasDouble(DEFAULT_DECIMALS) + ", mY="
				+ getYasDouble(DEFAULT_DECIMALS) + "]";
	}

	private ErgoCoordinateSet transformCoordinateSet(ErgoReferenceSystem sourceCRS, ErgoReferenceSystem targetCRS) {
		double C1;
		double C2;

		if (sourceCRS.getType().equalsIgnoreCase(GEOGRAPHIC)) {
			C1 = y.doubleValue();
			C2 = x.doubleValue();
		} else {
			C1 = x.doubleValue();
			C2 = y.doubleValue();
		}

		Coordinate coordinateSet = new Coordinate(C1,C2);
		ErgoCoordinateSet targetCoordinateSet = new ErgoCoordinateSet(0, 0, 0, null);
		ErgoCoordinateSet sourceCoordinateSet = new ErgoCoordinateSet(C1, C2, 0, sourceCRS.getSystem());
		boolean leniency = true;
		if (sourceCRS != targetCRS) {
			try {
				targetCoordinateSet = TransformReferenceSystem.TransformCoordinates(coordinateSet, sourceCRS.getSystem(), targetCRS.getSystem(), leniency);

			} catch (MismatchedDimensionException | FactoryException | TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return targetCoordinateSet;
		} else {
			return sourceCoordinateSet;
		}
	}


}