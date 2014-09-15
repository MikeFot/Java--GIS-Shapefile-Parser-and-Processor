package com.michaelfotiadis.shpparser.containers.ergo.widgets;

import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Michael Fotiadis
 *
 */
public class ErgoList {

	public List ergoList;
	private int ergoID;

	/**
	 * 
	 * @param shell
	 * @param style
	 * @param ErgoID
	 */
	public ErgoList (Shell shell, int style, int ErgoID) { // constructor with ID

		this.ergoList = new List(shell, style);
		this.ergoID = ErgoID;

	}

	/**
	 * 
	 * @param shell
	 * @param style
	 */
	public ErgoList (Shell shell, int style) { // constructor with ID

		this.ergoList = new List(shell, style);
		this.ergoID = 0;

	}


	public List getErgoList() {
		return ergoList;
	}

	public void setErgoList(List mErgoList) {
		this.ergoList = mErgoList;
	}

	public int getErgoID() {
		return ergoID;
	}

	public void setErgoID(int mErgoID) {
		this.ergoID = mErgoID;
	}



}
