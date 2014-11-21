package com.michaelfotiadis.shpparser.export.csv;

import java.io.File;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoReferenceSystem;
import com.michaelfotiadis.shpparser.containers.file.ShapefileContainer;
import com.michaelfotiadis.shpparser.util.file.FileOperations;

/**
 * Class handling export to CSV
 * @author Michael Fotiadis
 *
 */
public class ExportCSV {

	private final String CSV_EXTENSION = ".csv";
	private final char DELIMITER = ',';

	/**
	 * Generates export contents and writes them to a user selected CSV
	 * @param saveFile 
	 * @param shapefileContainer
	 * @param sourceCRS
	 * @param targetCRS
	 */
	public void createAndExportCSV(File saveFile, final ShapefileContainer shapefileContainer,
			final ErgoReferenceSystem sourceCRS, final ErgoReferenceSystem targetCRS) {

		boolean doTransformation = true;

		if (targetCRS == null || sourceCRS.getSystem().equals(targetCRS)) {
			doTransformation = false;
		}

		for (ErgoPolyline currentPolyline : shapefileContainer.getGeometryCollection()) {

			StringBuilder builder = new StringBuilder();
			builder.append(currentPolyline.getID());

			builder.append(DELIMITER);

			if (doTransformation) {
				builder.append(currentPolyline.getVertexListAsTransformedString(sourceCRS, targetCRS));
			} else {
				builder.append(currentPolyline.getVertexListAsString());
			}

			for (String key : currentPolyline.getStringKeys()) {
				builder.append(DELIMITER);
				builder.append(currentPolyline.getString(key));
			}

			new FileOperations().writeToFile(saveFile.getAbsolutePath(), builder.toString(), true, true);
		} // end iteration

	}


}
