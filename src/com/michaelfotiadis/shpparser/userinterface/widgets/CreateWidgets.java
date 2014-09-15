package com.michaelfotiadis.shpparser.userinterface.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.michaelfotiadis.shpparser.containers.ergo.widgets.ErgoButton;
import com.michaelfotiadis.shpparser.containers.ergo.widgets.ErgoCombo;
import com.michaelfotiadis.shpparser.containers.ergo.widgets.ErgoList;

/**
 * Class used to mass-produce widgets
 * @author Michael Fotiadis
 *
 */
public class CreateWidgets {

	/**
	 * 
	 * @param shell : the Shell object used
	 * @param gridData : the GridData object used
	 * @param labelText : the String to be set as Text
	 * @param width : the Integer width of the widget
	 * @param horizontalSpan : the Integer number of columns the widget occupies
	 * @return Label widget
	 */
	public Label createLabel(Shell shell, GridData gridData, String labelText, int width, int horizontalSpan) {
		Label myLabel = new Label(shell, SWT.NONE);

		gridData.widthHint = width;
		gridData.horizontalSpan = horizontalSpan;

		myLabel.setText(labelText);
		myLabel.setLayoutData(gridData);

		return myLabel;
	}

	/**
	 * 
	 * @param shell : the Shell object used
	 * @param gridData : the GridData object used
	 * @param labelText : the String to be set as Text
	 * @param width : the Integer width of the widget
	 * @param horizontalSpan : the Integer number of columns the widget occupies
	 * @return Text widget
	 */
	public Text createText(Shell shell, GridData gridData, String labelText, int width, int horizontalSpan) {
		Text myText = new Text(shell, SWT.MULTI | SWT.BORDER);

		gridData.widthHint = width;
		gridData.horizontalSpan = horizontalSpan;

		myText.setText(labelText);
		myText.setLayoutData(gridData);

		return myText;
	}

	/**
	 * 
	 * @param shell : the Shell object used
	 * @param gridData : the GridData object used
	 * @param labelText : the String to be set as Text
	 * @param horizontalSpan : the Integer number of columns the widget occupies
	 * @return Button widget
	 */
	public ErgoButton createButton(Shell shell, GridData gridData, String labelText, int horizontalSpan) {

		ErgoButton myButton = new ErgoButton(shell, SWT.PUSH);
		myButton.setErgoID(myButton.getErgoButton().hashCode());

		gridData.horizontalSpan = horizontalSpan;

		myButton.getErgoButton().setText(labelText);
		myButton.getErgoButton().setLayoutData(gridData);

		return myButton;
	}

	/**
	 * 
	 * @param shell
	 * @param gridData
	 * @param width
	 * @param horizontalSpan
	 * @return
	 */
	public ErgoCombo createCombo(Shell shell, GridData gridData, int width,  int horizontalSpan) {

		ErgoCombo myCombo = new ErgoCombo(shell, SWT.READ_ONLY);

		myCombo.setErgoID(myCombo.getErgoCombo().hashCode());

		gridData.widthHint = width;
		gridData.horizontalSpan = horizontalSpan;

		myCombo.getErgoCombo().setLayoutData(gridData);

		return myCombo;
	}

	/**
	 * 
	 * @param shell : the Shell object used
	 * @param gridData : the GridData object used
	 * @param width : the Integer width of the widget
	 *  @param height : the Integer height of the widget
	 * @param horisontalSpan : the Integer number of rows the widget occupies
	 * @return List widget
	 */
	public ErgoList createList(Shell shell, GridData gridData, int width, int height, int horisontalSpan) {
		
		ErgoList myList = new ErgoList(shell, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		
		myList.setErgoID(myList.ergoList.hashCode());

		Rectangle trim = myList.ergoList.computeTrim(0, 0, 0, height);
		gridData.heightHint = trim.height;
		gridData.horizontalSpan = horisontalSpan;
		gridData.widthHint = trim.width;
		myList.ergoList.setLayoutData(gridData);
		return myList;
	}

	/**
	 * 
	 * @param shell : the Shell object used
	 * @param gridData : the GridData object used
	 * @param width : the Integer width of the widget
	 * @param height : the Integer height of the widget
	 * @param verticalSpan : the Integer number of rows the widget occupies
	 * @return Canvas widget
	 */
	public Canvas createCanvas(Shell shell, GridData gridData, int width, int height, int verticalSpan) {

		Canvas myCanvas = new Canvas(shell, SWT.BORDER);

		gridData.widthHint = width;
		gridData.heightHint = height;
		gridData.verticalSpan = verticalSpan;
		myCanvas.setLayoutData(gridData);

		return myCanvas;
	}

	/**
	 * 
	 * @param shell : the Shell object used
	 * @param intMin : the Integer minimum value of the scale bar
	 * @param intMax : the Integer maximum value of the scale bar
	 * @param intIncrements : the Integer number of increments
	 * @param intPageIncrement : the Integer step of the scale bar
	 * @param intSetSelection : the Integer initial value of the slider
	 * @return Scale widget
	 */
	public Scale createScale(Shell shell, int intMin, int intMax, int intIncrements, int intPageIncrement, int intSetSelection) {

		Scale myScale = new Scale(shell, SWT.HORIZONTAL);

		myScale.setMinimum(intMin);
		myScale.setMaximum(intMax);
		myScale.setIncrement(intIncrements);
		myScale.setPageIncrement(intPageIncrement);
		myScale.setSelection(intSetSelection);

		return myScale;

	}
}
