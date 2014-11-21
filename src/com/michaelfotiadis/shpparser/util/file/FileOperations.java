package com.michaelfotiadis.shpparser.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.graph.util.SimpleFileFilter;

import com.michaelfotiadis.shpparser.util.system.Log;

/**
 * Helper class for file operations
 * @author Michael Fotiadis
 *
 */
public class FileOperations {

	private final String SHAPEFILE_DESCRIPTION = "ShapeFile";
	private final String SHAPEFILE_EXTENSION = ".shp";


	public File saveSpecificFile(String extension) {

		// Create a file chooser
		JFileChooser fc = new JFileChooser();

		if (extension.length() > 0) {

			// Add a File Filter for DXF files only
			FileFilter type = new ExtensionFilter(extension, extension);
			fc.addChoosableFileFilter(type);
			fc.setFileFilter(type);
		}
		int returnVal = fc.showSaveDialog(fc);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return new File(fc.getSelectedFile().getAbsolutePath() + extension);
		} else {
			return null;
		}
	}

	/**
	 * Opens a browser for shapefiles
	 * @return Location of the user selected file in FILE format
	 */
	public File browseAndReturnFile() {

		// Create a file chooser
		JFileChooser fc = new JFileChooser();

		// Add a File Filter for DXF files only
		FileFilter type = new ExtensionFilter(SHAPEFILE_DESCRIPTION, SHAPEFILE_EXTENSION);
		fc.addChoosableFileFilter(type);
		fc.setFileFilter(type);

		int returnVal = fc.showOpenDialog(fc);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

	/**
	 * Converts a String URL to a File object
	 * @param pathURL : The URL filename
	 * @return FILE object of the input filename
	 */
	public File convertURLtoFile(String pathURL) {
		final String urlSeparator = "file:/";
		
		if (pathURL.contains(urlSeparator)) {

			String[] stringURL = pathURL.toString().split(urlSeparator);

			System.out.println(stringURL[1]);

			File shpFile = new File(stringURL[1]);


			return shpFile;
		} else {
			return null;
		}

	}

	/**
	 * Opens a browser for shapefiles and returns a URL
	 * @return Location of the user selected shapefile in URL format 
	 */
	@SuppressWarnings("deprecation")
	public URL browseAndReturnURL() {

		URL shapeURL = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new SimpleFileFilter("shp", SHAPEFILE_DESCRIPTION)); // browse for a Shapefile
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();

			try {
				shapeURL = file.toURL();
			} catch (MalformedURLException e) {
				Log.Exception(e, 0);
			}

		} else {
			Log.Err("FileOperations Report: File error.", 1, true);
		}
		return shapeURL;
	}


	/**
	 * Checks if a directory is empty
	 * @param dataDir Directory path
	 * @return
	 */
	public static boolean isDirectoryEmpty(String dataDir) {
		File targetDirectory = new File(dataDir);
		if (targetDirectory.isDirectory()) {
			String[] fileList = targetDirectory.list();
			if (fileList.length > 0) {

				Log.Out("Directory " + targetDirectory.getPath() + "/ is not empty.",1 , false);
				return false;
			} else {
				Log.Out("Directory is empty.", 1, false);
				return true;
			}
		} else { 
			Log.Out("This is not a directory.", 1, false);
			return true;
		}
	}

	/**
	 * 
	 * @param dataDir
	 * @return
	 */
	public static String[] listFilesInDir(String dataDir){

		File targetDirectory = new File(dataDir);
		String[] fileList = targetDirectory.list();

		return fileList;

	}

	/**
	 * Code for extending the File Chooser
	 */
	private class ExtensionFilter extends FileFilter {
		private String extensions[];

		private String description;

		private ExtensionFilter(String description, String extension) {
			this(description, new String[] { extension });
		}

		private ExtensionFilter(String description, String extensions[]) {
			this.description = description;
			this.extensions = (String[]) extensions.clone();
		}

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			}
			int count = extensions.length;
			String path = file.getAbsolutePath();
			for (int i = 0; i < count; i++) {
				String ext = extensions[i];
				if (path.endsWith(ext)
						&& (path.charAt(path.length() - ext.length()) == '.')) {
					return true;
				}
			}
			return false;
		}

		public String getDescription() {
			return (description == null ? extensions[0] : description);
		}
	}

	/**
	 * Write to file
	 * @param file the filename
	 * @param text the text to write to the file.
	 * @param append the append
	 * @param newline whether to append a newline at the end to the string.
	 */
	public void writeToFile(String file, String text, Boolean append, Boolean newline){
		try{

			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(text);
			if (newline)
				bw.newLine();
			bw.close();
		}catch (Exception e) {
			Log.Exception(e, 1);
		}
	}
}
