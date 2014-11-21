package com.michaelfotiadis.shpparser.export.csv;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoReferenceSystem;
import com.michaelfotiadis.shpparser.containers.file.ShapefileContainer;
import com.michaelfotiadis.shpparser.util.file.FileOperations;
import com.michaelfotiadis.shpparser.util.system.Log;

/**
 * Class handling export to CSV
 * @author Michael Fotiadis
 *
 */
public class ExportCSV {

	private final char DELIMITER = ',';

	/**
	 * Generates export contents and writes them to a user selected CSV
	 * @param saveFile File to be stored
	 * @param shapefileContainer ShapefileContainer containing data to be exported
	 * @param sourceCRS Source ErgoReferenceSystem
	 * @param targetCRS Target ErgoReferenceSystem (null if no transformation)
	 */
	public boolean createAndExportCSV(File saveFile, final ShapefileContainer shapefileContainer,
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
		
		int userChoice = JOptionPane.showConfirmDialog(
				null,
				"Attempt to Open Exported File?",
				"Show Export",
				JOptionPane.YES_NO_OPTION);
		if (userChoice == JOptionPane.YES_OPTION) {
			// Open the file using default options
			try {
				Desktop.getDesktop().open(saveFile);
			} catch (IOException e) {
				Log.Exception(e, 0);
			} // remove if needed
		}
		
		return true;
	}

}
