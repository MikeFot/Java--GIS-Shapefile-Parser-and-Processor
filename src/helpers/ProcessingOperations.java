package helpers;

import parsers.text.ParseEPSGFile;
import util.system.Log;
import containers.ergo.geometry.ErgoPolyline;
import containers.ergo.geometry.ErgoReferenceSystem;
import containers.ergo.geometry.ErgoVertex;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class ProcessingOperations {

	/**
	 * Converts an ErgoPolyline#s ErgoVertex list to a String array
	 * @param polyline ErgoPolyline object to be processed
	 * @return String array containing coordinates
	 */
	public String[] buildCoordinateList(ErgoPolyline polyline) {

		String[] coordinateTable = new String[polyline.getVertexList().size()];
		int iter_CoordinateCount = 0;
		for (ErgoVertex vertex :  polyline.getVertexList()) {

			StringBuilder sb = new StringBuilder();

			sb.append(String.valueOf(vertex.getXasDouble(6)));
			sb.append(" , ");
			sb.append(String.valueOf(vertex.getYasDouble(6)));

			coordinateTable[iter_CoordinateCount] = sb.toString();
			iter_CoordinateCount ++;
		}
		return coordinateTable;
	}
	

	/**
	 * 
	 * @param stringkeys
	 * @param polyline
	 * @return
	 */
	public String[] buildAttributeList(String[] stringkeys, ErgoPolyline polyline) {
		
		int sizePline = polyline.getHashMapSize();
		
		String[] attributeTable = new String[sizePline];
		// iterate string k
		int iter_AttributeCount = 0;
		String attribute;
		
		for (String strKey :  stringkeys) {
			
			attribute = String.valueOf(polyline.getString(strKey));
			Log.Out(strKey + " " + attribute, 3, false);
			attributeTable[sizePline - iter_AttributeCount - 1] = (strKey.toString() + " = " + attribute.toString());

			iter_AttributeCount ++;

		}
		return attributeTable;
		
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public String[] createComboString(String fileName) {

		final String epsgTable[][] = ParseEPSGFile.readAndParseEPSGFile(fileName);

		String[] epsgColumn = new String[epsgTable[0].length];
		Log.Out("Populating combo boxes with " + epsgTable[0].length	+ " items." , 2, true);
		int i = 0;

//		epsgColumn[i] = "---Other Systems---";
//		i++;
		while (i < epsgTable[0].length) {

			epsgColumn[i] = epsgTable[1][i] + " (EPSG: " + epsgTable[0][i] + " , " + epsgTable[2][i] + ")";
			i++;
		}

		return epsgColumn;

	}
	
	/**
	 * Extracts the EPSG from a String
	 * @param input : The input String to be split
	 * @return String of the format "EPSG:#" 
	 */
	public ErgoReferenceSystem splitComboString(String input) {
		final String[] firstSplit = input.split("EPSG: ");
		final String[] secondSplit = firstSplit[1].split(" , ");
		
		final String selectedCRS = "EPSG:" + secondSplit[0].trim();
		final String selectedProj = secondSplit[1].replace(")", "");
		
		
		final ErgoReferenceSystem referenceSystem = new ErgoReferenceSystem(selectedCRS, selectedProj);
		
		return referenceSystem;
		
	}
	
}
