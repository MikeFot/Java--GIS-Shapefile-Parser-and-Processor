package com.michaelfotiadis.shpparser.util.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.graph.util.SimpleFileFilter;

/**
 * 
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
	 * 
	 * @param pathURL : The URL filename
	 * @return FILE object of the input filename
	 */
	public File convertURLtoFile(String pathURL) {
		
		String[] stringURL = pathURL.toString().split("file:/");
		
		System.out.println(stringURL[1]);
		
		File shpFile = new File(stringURL[1]);
		
		
		return shpFile;
		
	}

	/**
	 * Opens a browser for shapefiles
	 * @return Location of the user selected shapefile in URL format 
	 */
	@SuppressWarnings("deprecation")
	public URL browseAndReturnURL() {

		URL shapeURL = null;
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new SimpleFileFilter("shp", SHAPEFILE_DESCRIPTION)); // browse for a Shapefile
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();

			try {
				shapeURL = f.toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		} else {
			System.err.println("FileOperations Report: File error.");
		}
		return shapeURL;
	}


	/**
	 * 
	 * @param dataDir
	 * @return
	 */
	public static boolean isDirectoryEmpty(String dataDir) {
		File targetDirectory = new File(dataDir);
		if (targetDirectory.isDirectory()) {
			String[] fileList = targetDirectory.list();
			if (fileList.length > 0) {

				System.out.println("Directory " + targetDirectory.getPath() + "/ is not empty.");
				return false;
			} else {
				System.out.println("Directory is empty.");
				return true;
			}
		} else { 
			System.out.println("This is not a directory.");
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
}
