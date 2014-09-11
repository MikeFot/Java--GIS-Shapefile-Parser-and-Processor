package containers.ergo.widgets;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Mike
 *
 */
public class ErgoButton {

	public Button ergoButton;
	private int ergoID;

	/**
	 * 
	 * @param shell
	 * @param style
	 * @param ErgoID
	 */
	public ErgoButton (Shell shell, int style, int ErgoID) { // constructor with ID

		this.ergoButton = new Button(shell, style);
		this.ergoID = ErgoID;

	}

	/**
	 * 
	 * @param shell
	 * @param style
	 */
	public ErgoButton (Shell shell, int style) { // constructor with ID

		this.ergoButton = new Button(shell, style);
		this.ergoID = 0;

	}


	public Button getErgoButton() {
		return ergoButton;
	}

	public void setErgoButton(Button mErgoButton) {
		this.ergoButton = mErgoButton;
	}

	public int getErgoID() {
		return ergoID;
	}

	public void setErgoID(int mErgoID) {
		this.ergoID = mErgoID;
	}



}
