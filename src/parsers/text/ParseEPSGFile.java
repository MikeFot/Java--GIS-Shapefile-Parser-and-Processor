package parsers.text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ParseEPSGFile {
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public static String[][] readAndParseEPSGFile (String fileName) {

		String fullPath = "externaldata/" + fileName;
		File epsgFile = new File(fullPath);
		String epsgStringFile = epsgFile.toString();
		
		System.out.println("Parsing EPSG file.");

		int lineCount = 0;
		try {
			lineCount = ParseEPSGFile.count(epsgStringFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("The EPSG file contains " + lineCount + " rows.");
		String epsgArray[][] = new String[3][lineCount+1];

		try {
			FileInputStream fstream = new FileInputStream(epsgStringFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String inputStream;
			int i = 0;
			while ((inputStream = br.readLine()) != null) {
				String[] tempText = inputStream.split(",");

				epsgArray[0][i] = tempText[0]; 
				epsgArray[1][i] = tempText[1];
				epsgArray[2][i] = tempText[2];
				i++;
			}
			br.close();

		} catch (Exception e) {
			System.err.println(e);
		}
		return epsgArray;
	}
	
	private static int count(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
}

