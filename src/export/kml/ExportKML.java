package export.kml;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import util.system.Log;
import containers.ergo.geometry.ErgoPolyline;
import containers.ergo.geometry.ErgoReferenceSystem;
import containers.ergo.geometry.ErgoVertex;
import containers.file.ShapefileContainer;
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

public class ExportKML {

	private boolean didOperationSucceed;
	
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
				final Kml mKML = new Kml();
				final String geometry = shapefileContainer.getGeometryType();

				Document document;

				switch (geometry) {
				case "Point":
					document = mKML.createAndSetDocument().withName("Point_Export");
					break;
				case "Polyline":
					document = mKML.createAndSetDocument().withName("Polyline_Export");
					break;
				case "Polygon":
					document = mKML.createAndSetDocument().withName("Polygon_Export");
					break;
				default:
					document = mKML.createAndSetDocument().withName("Shp_Export");
					break;
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

				for (ErgoPolyline currentPolyline : shapefileContainer.getGeometryCollection()) {

					String vID;
					double vCoord1;
					double vCoord2;
					// double vCoord3;

					vID = currentPolyline.getID();

					Placemark mPlacemark = document.createAndAddPlacemark().withName(
							vID);
					ExtendedData mExtendedData = new ExtendedData();

					// sort the arrays as they appear in reverse order
					String[] sortedStringArray = currentPolyline.getStringKeys();
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

					switch (geometry) {
					case "Polyline":
						LineString mLine = mPlacemark.createAndSetLineString();

						for (ErgoVertex vertex : currentPolyline.getVertexList()) {
							vCoord1 = vertex.getWGSCoordinates(sourceCRS).getC2();
							vCoord2 = vertex.getWGSCoordinates(sourceCRS).getC1();

							String export = " From : " + vertex.getXasDouble() + " " 
									+ vertex.getYasDouble() + " To : " + vCoord1 + " " + vCoord2;
							Log.Out(export , 2, true);
							mLine.addToCoordinates(vCoord1, vCoord2);
						}
						mPlacemark.createAndAddStyle().withLineStyle(lineStyle);
						break;
					case "Polygon":
						LinearRing mRing = mPlacemark.createAndSetPolygon()
						.createAndSetOuterBoundaryIs().createAndSetLinearRing();

						for (ErgoVertex vertex : currentPolyline.getVertexList()) {

							vCoord1 = vertex.getWGSCoordinates(sourceCRS).getC2();
							vCoord2 = vertex.getWGSCoordinates(sourceCRS).getC1();

							String export = " From : " + vertex.getXasDouble() + " " 
									+ vertex.getYasDouble() + " To : " + vCoord1 + " " + vCoord2;
							Log.Out(export , 2, true);
							mRing.addToCoordinates(vCoord1, vCoord2);
						}
						mPlacemark.createAndAddStyle().withPolyStyle(polyStyle);
						break;
					default:
						ErgoVertex vertex = currentPolyline.getVertex(0);


						vCoord1 = vertex.getWGSCoordinates(sourceCRS).getC2();
						vCoord2 = vertex.getWGSCoordinates(sourceCRS).getC1();

						String export = " From : " + vertex.getXasDouble() + " " + vertex.getYasDouble() + " To : " + vCoord1 + " " + vCoord2;
						Log.Out(export , 2, true);
						mPlacemark.createAndSetPoint().createAndSetCoordinates()
						.add(new Coordinate(vCoord1, vCoord2));
						mPlacemark.createAndAddStyle().withIconStyle(iconstyle);
						break;
					}

				} // end iteration

				final String exportName;
				switch (geometry) {
				case "Point":
					exportName = "Point_Export.kml";
					break;
				case "Polyline":
					exportName = "Polyline_Export.kml";
					break;
				case "Polygon":
					exportName = "Polygon_Export.kml";
					break;
				default:
					exportName = "Shapefile_Export.kml";
					break;
				}
				Log.Out("Preparing to export..." , 1, true);

				try {
					File kmlFile = new File("output/" + exportName);
					mKML.marshal(kmlFile);
					Log.Out("KML exported successfully at : " + exportName , 0, true);

					// Open the file using default options
					Desktop desktop = Desktop.getDesktop();
					desktop.open(kmlFile); // remove if needed
					didOperationSucceed = true;
				} catch (FileNotFoundException eFileNotFound) {
					didOperationSucceed = false;
					Log.Exception(eFileNotFound, 0);
					eFileNotFound.printStackTrace();
				} catch (IOException eIO3) {
					didOperationSucceed = false;
					Log.Exception(eIO3, 0);
					eIO3.printStackTrace();
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
