package com.michaelfotiadis.shpparser.containers.file;

import java.io.File;

import com.michaelfotiadis.shpparser.util.file.FileOperations;

/**
 * Object storing information about a parsed file
 * @author Michael Fotiadis
 *
 */
public class FileContainer {

	private String mFilePath;

	public FileContainer(String filePath) {

		this.mFilePath = filePath;

	}

	public FileContainer() {
		this.mFilePath = null;
	}

	public void setStringPath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	public String getStringPath() {

		return mFilePath;

	}

	public File getPathAsFile() {
		File shpFile = new FileOperations().convertURLtoFile(mFilePath);
		return shpFile;
	}

}
