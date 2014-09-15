package com.michaelfotiadis.shpparser.containers.ergo.widgets;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class ErgoCombo {

	public Combo ergoCombo;
	private int ergoID;

	/**
	 * 
	 * @param shell
	 * @param style
	 * @param ErgoID
	 */
	public ErgoCombo (Shell shell, int style, int ErgoID) { // constructor with ID

		this.ergoCombo = new Combo(shell, style);
		this.ergoID = ErgoID;

	}

	/**
	 * 
	 * @param shell
	 * @param style
	 */
	public ErgoCombo (Shell shell, int style) { // constructor with ID

		this.ergoCombo = new Combo(shell, style);
		this.ergoID = 0;

	}

	public Combo getErgoCombo() {
		return ergoCombo;
	}

	public void setErgoButton(Combo ergoCombo) {
		this.ergoCombo = ergoCombo;
	}

	public int getErgoID() {
		return ergoID;
	}

	public void setErgoID(int mErgoID) {
		this.ergoID = mErgoID;
	}

}
