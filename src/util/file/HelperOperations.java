package util.file;

import java.io.File;

public class HelperOperations {

	/**
	 * 
	 * @param pathURL : The URL filename
	 * @return FILE object of the input filename
	 */
	public static File convertURLtoFile(String pathURL) {
		
		String[] stringURL = pathURL.toString().split("file:/");
		
		System.out.println(stringURL[1]);
		
		File shpFile = new File(stringURL[1]);
		
		
		return shpFile;
		
	}
	
	
}
