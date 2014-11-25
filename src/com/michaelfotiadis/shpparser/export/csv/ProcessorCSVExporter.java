package com.michaelfotiadis.shpparser.export.csv;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.eclipse.swt.widgets.Display;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoReferenceSystem;
import com.michaelfotiadis.shpparser.containers.file.ShapefileContainer;
import com.michaelfotiadis.shpparser.containers.interfaces.ExporterInterface;
import com.michaelfotiadis.shpparser.util.file.FileOperations;
import com.michaelfotiadis.shpparser.util.system.Log;

/**
 * Class handling export to CSV
 * @author Michael Fotiadis
 *
 */
public class ProcessorCSVExporter implements ExporterInterface, Runnable {

	private final char DELIMITER = ',';
	private ErgoReferenceSystem targetCRS;
	private ErgoReferenceSystem sourceCRS;
	private ShapefileContainer shapefileContainer;
	private File saveFile;

	/**
	 * Generates export contents and writes them to a user selected CSV
	 * @param saveFile File to be stored
	 * @param shapefileContainer ShapefileContainer containing data to be exported
	 * @param sourceCRS Source ErgoReferenceSystem
	 * @param targetCRS Target ErgoReferenceSystem (null if no transformation)
	 */
	public ProcessorCSVExporter(File saveFile, final ShapefileContainer shapefileContainer,
			final ErgoReferenceSystem sourceCRS, final ErgoReferenceSystem targetCRS) {
		this.saveFile = saveFile;
		this.shapefileContainer = shapefileContainer;
		this.sourceCRS = sourceCRS;
		this.targetCRS = targetCRS;
	}

	@Override
	public void run() {
		boolean doTransformation = true;

		if (targetCRS == null || sourceCRS.getSystem().equals(targetCRS)) {
			doTransformation = false;
		}

		int count = 0;
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
			count ++;
			Log.Out("Exported Item " + count + " of " + shapefileContainer.getGeometryCollection().size(), 2, true);
		} // end iteration


		// start a SWT UI thread to handle file opening
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
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
			}
		});

	}

}
