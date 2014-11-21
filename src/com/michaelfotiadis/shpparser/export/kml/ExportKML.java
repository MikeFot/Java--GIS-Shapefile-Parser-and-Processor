package com.michaelfotiadis.shpparser.export.kml;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.ArrayUtils;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoReferenceSystem;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoShapefileGeometryType;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoVertex;
import com.michaelfotiadis.shpparser.containers.file.ShapefileContainer;
import com.michaelfotiadis.shpparser.containers.reference.CoordinateSystemsContainer;
import com.michaelfotiadis.shpparser.util.file.FileOperations;
import com.michaelfotiadis.shpparser.util.system.Log;

import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Style;

/**
 * Class which handles exporting of shapefiles to KML
 * @author Michael Fotiadis
 *
 */
public class ExportKML {

	private boolean didOperationSucceed;
	private File kmlFile;
	private Kml mKML;

	/**
	 * Exports geometry collection to KML
	 * 
	 * @param GEOMETRY_COLLECTION
	 *            : The input ErgoPolyline collection
	 * @param sourceCRS
	 *            : The original coordinate system of the input collection
	 */
	public boolean createKML(final ShapefileContainer shapefileContainer,
			final ErgoReferenceSystem sourceCRS) {

		Log.Out("Starting KML export..." , 0 , true);
		mKML = new Kml();
		final String geometry = shapefileContainer.getGeometryType();

		Document document;

		if (geometry.equals(ErgoShapefileGeometryType.POINT.toString())) {
			document = mKML.createAndSetDocument().withName("Point_Export");
		} else if (geometry.equals(ErgoShapefileGeometryType.POLYLINE.toString())) {
			document = mKML.createAndSetDocument().withName("Polyline_Export");
		}  else if (geometry.equals(ErgoShapefileGeometryType.POLYGON.toString())) {
			document = mKML.createAndSetDocument().withName("Polygon_Export");
		} else {
			document = mKML.createAndSetDocument().withName("Shp_Export");
		}
		Log.Out("Created the KML document in memory." , 1, false);

		final Style style = document.createAndAddStyle().withId("lStyle");

		final LineStyle lineStyle = style.createAndSetLineStyle()
				.withColorMode(ColorMode.RANDOM).withWidth(4.0d);
		final IconStyle iconstyle = style.createAndSetIconStyle()
				.withColorMode(ColorMode.NORMAL).withScale(1.1d);
		iconstyle.createAndSetIcon().withHref(
				"http://maps.google.com/mapfiles/kml/paddle/red-circle.png");
		final PolyStyle polyStyle = style.createAndSetPolyStyle()
				.withColorMode(ColorMode.RANDOM);

		Log.Out("Created the KML styles." , 1, false);
		Long totalCount = (long) shapefileContainer.getGeometryCollection().size();

		Log.Out("Source CRS is " + sourceCRS.getSystem(), 1, false);
		ErgoReferenceSystem targetCRS = new ErgoReferenceSystem(new CoordinateSystemsContainer().getWGS_84_2D_LAT_LON(), "geographic 2D");
		Log.Out("Target CRS is " + targetCRS.getSystem(), 1, false);

		boolean doTransformation = false;
		if (!targetCRS.getSystem().equals(sourceCRS.getSystem())) {
			Log.Out("Transformation needs to be performed", 1, false);
			doTransformation = true;
		}

		for (ErgoPolyline currentPolyline : shapefileContainer.getGeometryCollection()) {
			String vID;
			double vCoord1;
			double vCoord2;

			vID = currentPolyline.getID();

			Log.Out(" Exporting " + vID + " of " + totalCount, 2, true);

			Placemark mPlacemark = document.createAndAddPlacemark().withName(
					vID);
			ExtendedData mExtendedData = new ExtendedData();

			// sort the arrays as they appear in reverse order
			final String[] sortedStringArray = currentPolyline.getStringKeys();
			ArrayUtils.reverse(sortedStringArray);
			String[] sortedDoubleArray = currentPolyline.getDoubleKeys();
			ArrayUtils.reverse(sortedDoubleArray);
			String[] sortedIntegerArray = currentPolyline.getIntegerKeys();
			ArrayUtils.reverse(sortedIntegerArray);
			// end sort

			// add extended data
			addExtendedData(sortedStringArray, currentPolyline, mExtendedData);
			addExtendedData(sortedIntegerArray, currentPolyline, mExtendedData);
			addExtendedData(sortedDoubleArray, currentPolyline, mExtendedData);

			mPlacemark.setExtendedData(mExtendedData);



			if (geometry.equals(ErgoShapefileGeometryType.POLYLINE.toString())) {
				LineString mLine = mPlacemark.createAndSetLineString();

				for (ErgoVertex vertex : currentPolyline.getVertexList()) {
					if (doTransformation) {
						vCoord1 = vertex.getWGSCoordinates(sourceCRS).getC2();
						vCoord2 = vertex.getWGSCoordinates(sourceCRS).getC1();
						//						String export = " From : " + vertex.getXasDouble() + " " 
						//								+ vertex.getYasDouble() + " To : " + vCoord1 + " " + vCoord2;
						//						Log.Out(export , 2, true);
					} else {
						vCoord1 = vertex.getXasDouble();
						vCoord2 = vertex.getYasDouble();
					}
					mLine.addToCoordinates(vCoord1, vCoord2);
				}
				mPlacemark.createAndAddStyle().withLineStyle(lineStyle);
			}  else if (geometry.equals(ErgoShapefileGeometryType.POLYGON.toString())) {
				LinearRing mRing = mPlacemark.createAndSetPolygon()
						.createAndSetOuterBoundaryIs().createAndSetLinearRing();

				for (ErgoVertex vertex : currentPolyline.getVertexList()) {
					if (doTransformation) {
						vCoord1 = vertex.getWGSCoordinates(sourceCRS).getC2();
						vCoord2 = vertex.getWGSCoordinates(sourceCRS).getC1();
						//						String export = " From : " + vertex.getXasDouble() + " " 
						//								+ vertex.getYasDouble() + " To : " + vCoord1 + " " + vCoord2;
						//						Log.Out(export , 2, true);
					} else {
						vCoord1 = vertex.getXasDouble();
						vCoord2 = vertex.getYasDouble();
					}

					mRing.addToCoordinates(vCoord1, vCoord2);
				}
				mPlacemark.createAndAddStyle().withPolyStyle(polyStyle);
			} else {
				ErgoVertex vertex = currentPolyline.getVertex(0);
				if (doTransformation) {
					vCoord1 = vertex.getWGSCoordinates(sourceCRS).getC2();
					vCoord2 = vertex.getWGSCoordinates(sourceCRS).getC1();
					//					String export = " From : " + vertex.getXasDouble() + " " 
					//							+ vertex.getYasDouble() + " To : " + vCoord1 + " " + vCoord2;
					//					Log.Out(export , 2, true);
				} else {
					vCoord1 = vertex.getXasDouble();
					vCoord2 = vertex.getYasDouble();
				}

				mPlacemark.createAndSetPoint().createAndSetCoordinates()
				.add(new Coordinate(vCoord1, vCoord2));
				mPlacemark.createAndAddStyle().withIconStyle(iconstyle);
			}
		} // end iteration
		Log.Out("Preparing to export..." , 1, true);

		try {
			kmlFile = new FileOperations().saveSpecificFile(".kml");

			if (kmlFile == null) {
				Log.Out("User Aborted", 1, true);
				return false;
			}
			mKML.marshal(kmlFile);

			Log.Out("KML exported successfully at : " + kmlFile.getAbsolutePath() , 0, true);

			didOperationSucceed = true;

			int userChoice = JOptionPane.showConfirmDialog(
					null,
					"Open Exported File in Google Earth?",
					"Show Export",
					JOptionPane.YES_NO_OPTION);
			if (userChoice == JOptionPane.YES_OPTION) {
				// Open the file using default options
				Desktop desktop = Desktop.getDesktop();
				desktop.open(kmlFile); // remove if needed
			}
			
			document = null;
		} catch (FileNotFoundException eFileNotFound) {
			didOperationSucceed = false;
			Log.Exception(eFileNotFound, 0);
			eFileNotFound.printStackTrace();
		} catch (IOException eIO3) {
			didOperationSucceed = false;
			Log.Exception(eIO3, 0);
			eIO3.printStackTrace();
		} finally {
			if (kmlFile != null && mKML != null) {
				Kml.unmarshal(kmlFile);
				mKML = null;
				kmlFile = null;
			}
		}
		return didOperationSucceed;
	}

	/**
	 * Stores HashMap data from a MyPolyline object in an ExtendedData container
	 * to be copied into a KML
	 * 
	 * @param mArray
	 *            : The array containing the extended data
	 * @param mPolyline
	 *            : The MyPolyline object whose extended data will be extracted
	 * @param mData
	 *            : The ExtendedData container which will store the data
	 */
	private static void addExtendedData(final String[] mArray,
			final ErgoPolyline mPolyline, final ExtendedData mData) {
		for (String strKey : mArray) {
			String strValue = mPolyline.getString(strKey);
			mData.addToData(KmlFactory.createData(strValue).withName(strKey));
		}
	}

}
