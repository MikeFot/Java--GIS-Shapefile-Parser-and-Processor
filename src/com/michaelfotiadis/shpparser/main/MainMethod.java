package com.michaelfotiadis.shpparser.main;

import com.michaelfotiadis.shpparser.constants.AppConstants;
import com.michaelfotiadis.shpparser.userinterface.layouts.MainParserLayout;
import com.michaelfotiadis.shpparser.util.file.FileOperations;

/**
 * Main method
 * @author Michael Fotiadis
 *
 */
public class MainMethod {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// clear the log
		new FileOperations().writeToFile(AppConstants.LOG_FILENAME, "", false, false);
		// initialise the UI
		new MainParserLayout().initUI();
	}

}
