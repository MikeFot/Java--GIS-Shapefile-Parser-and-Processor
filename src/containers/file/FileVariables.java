package containers.file;

import java.io.File;

import util.file.HelperOperations;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class FileVariables {

	private String mFilePath;

	public FileVariables(String filePath) {

		this.mFilePath = filePath;

	}

	public FileVariables() {
		this.mFilePath = null;
	}

	public void setStringPath(String mFilePath) {
		this.mFilePath = mFilePath;
	}

	public String getStringPath() {

		return mFilePath;

	}

	public File getPathAsFile() {
		File shpFile = HelperOperations.convertURLtoFile(mFilePath);
		return shpFile;
	}

}
