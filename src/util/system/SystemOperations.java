package util.system;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class SystemOperations implements ClipboardOwner {

	/**
	 * 
	 * @param infoMessage : Message to be displayed (String)
	 * @param location : Location of the error (String)
	 */
	public static void infoBox(String infoMessage, String location)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "Error: " + location, JOptionPane.ERROR_MESSAGE);
    }

	/**
	 * Method which returns the String contents of the Windows clipboard.
	 * @return Clipboard contents as String
	 */
	 public static String getClipboardContents() {
		    String result = "";
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    //odd: the Object parameter of getContents is not currently used
		    Transferable contents = clipboard.getContents(null);
		    boolean hasTransferableText =
		      (contents != null) &&
		      contents.isDataFlavorSupported(DataFlavor.stringFlavor)
		    ;
		    if (hasTransferableText) {
		      try {
		        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
		      }
		      catch (UnsupportedFlavorException | IOException ex){
		        System.out.println(ex);
		        ex.printStackTrace();
		      }
		    }
		    return result;
		  }

	 /**
	  * Method which sets the contents of the clipboard equal to the input String
	  * @param aString : String to be stored in the clipboard (String)
	  */
	 public static void setClipboardContents(String aString){
		    StringSelection stringSelection = new StringSelection(aString);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(stringSelection, null);
		  }

	 @Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// TODO Auto-generated method stub
		
	}
	 
}
