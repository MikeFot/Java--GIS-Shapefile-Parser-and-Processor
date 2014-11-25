package com.michaelfotiadis.shpparser.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.michaelfotiadis.shpparser.util.system.Log;

/**
 * Helper class for file operations
 * @author Michael Fotiadis
 *
 */
public class FileOperations {

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
