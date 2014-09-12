package userinterface.layouts;

import helpers.ProcessingOperations;

import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import parsers.shapefile.ParseShapefile;
import userinterface.viewer.MapDisplay;
import userinterface.widgets.CreateWidgets;
import util.file.FileOperations;
import util.system.Log;

import com.google.common.collect.Iterables;

import containers.ergo.geometry.ErgoPolyline;
import containers.ergo.geometry.ErgoReferenceSet;
import containers.ergo.geometry.ErgoReferenceSystem;
import containers.ergo.widgets.ErgoButton;
import containers.ergo.widgets.ErgoCombo;
import containers.ergo.widgets.ErgoList;
import containers.file.FileContainer;
import containers.file.ShapefileContainer;
import export.kml.ExportKML;

/**
 * Class containing user interface SWT components and listeners
 * @author Michael Fotiadis
 *
 */
public class MainParserLayout implements DisposeListener, SelectionListener {
	
	private ShapefileContainer shapefileContainer;
	
	private Label labelFileSelection;
	private Label labelCSR;
	private Label labelStatus;
	private Label labelInfo;

	private ErgoButton buttonBrowse;
	private ErgoButton buttonMap;
	private ErgoButton buttonKML;
	private ErgoButton buttonCSV;
	private ErgoButton buttonRawCSV;
	private ErgoButton buttonSearchCRS;
	private ErgoButton buttonViewCRS;
	private ErgoButton buttonDetectCRS;

	private ErgoCombo comboSource;
	private ErgoCombo comboTarget;
	private ErgoCombo comboEPSG;

	private ErgoList listID;
	private ErgoList listAttributes;
	private ErgoList listCoordinates;

	private Text textSearch;
	private ErgoButton buttonSearch1;
	private ErgoButton buttonSearch2;
	private ErgoButton buttonSearch3;
	private ErgoList listSearch;

	private static ErgoList STATUS_LIST;

	private String fileName = "";
	private Shell mainShell;
	private Shell searchShell;
	private Shell infoShell;

	private boolean searchShellCreated = false;
	private Display display;

	private static ErgoReferenceSet REFERENCE_SETS = new ErgoReferenceSet();

	/**
	 * Initialise the Display
	 */
	public void initUI() {
		display = new Display();

		final Shell mainShell = new MainParserLayout().addMainShell(display);

		mainShell.open();
		Log.Out("Initialising UI...", 0, true);
		while (!mainShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private final FileContainer filePath = new FileContainer();

	/**
	 * The method creates a Shell in a Display defined by input
	 * @param firstDisplay : The Display (JAVA.SWT) on which the Shell will be created
	 * @return
	 */
	private Shell addMainShell(final Display firstDisplay) {
		mainShell = new Shell(firstDisplay, SWT.MAX | SWT.RESIZE | SWT.MIN);
		mainShell.setText("Shapefile Parser");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		mainShell.setLayout(gridLayout);
		//		Log.Out("Successfully Created Main Shell.", 0);

		mainShell =createMenuItems(mainShell); // Create Menu Items, should expand

		addWidgets(mainShell); // Create all the Widgets

		mainShell.addDisposeListener(this);
		mainShell.pack();

		return mainShell;
	}


	/**
	 * 
	 * @param inputShell
	 */
	public Shell createMenuItems(final Shell inputShell) {
		Shell myShell = inputShell;
		// Menu item section
		Menu menuBar = new Menu(myShell, SWT.BAR);
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");
		Menu fileMenu = new Menu(myShell, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText("&Exit"); // exit button
		myShell.setMenuBar(menuBar);

		exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputShell.getDisplay().dispose();
				System.exit(0);
			}
		});// end menu item section
		return inputShell;
	}

	/**
	 * 
	 * @param shell
	 * @return
	 */
	private Shell addSearchShell(Shell shell) {

		Log.Out("Creating Search Shell..." , 0, true);
		searchShell = new Shell(shell);
		searchShell.setText("Search");
		searchShell.setSize(200,200);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		searchShell.setLayout(gridLayout);

		// Row 1
		textSearch = new Text(searchShell, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		textSearch.setLayoutData(gridData);
		buttonSearch1 = new CreateWidgets().createButton(searchShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Clear", 1); // create draw button

		// Row 2
		listSearch = new CreateWidgets().createList(searchShell, new GridData(GridData.FILL, GridData.FILL, true, true), 400, 400, 2); // create list populated with IDs

		// Row 3
		buttonSearch2 = new CreateWidgets().createButton(searchShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Set Source", 1); // create draw button
		labelStatus = new CreateWidgets().createLabel(searchShell, new GridData(GridData.FILL), comboSource.ergoCombo.getText(), 500, 1); // create info label

		// Row 4
		buttonSearch3 = new CreateWidgets().createButton(searchShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Set Target", 1); // create draw button
		labelInfo = new CreateWidgets().createLabel(searchShell, new GridData(GridData.FILL), comboTarget.ergoCombo.getText(), 400, 1); // create info label

		listSearch.ergoList.setItems(comboSource.ergoCombo.getItems());

		buttonSearch1.ergoButton.addSelectionListener(this);
		buttonSearch2.ergoButton.addSelectionListener(this);
		buttonSearch3.ergoButton.addSelectionListener(this);

		textSearch.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent modifyEvent) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						performSearch();
					}
				});
			}
		});

		searchShellCreated = true;
		return searchShell;
	}

	/**
	 * 
	 * @param shell
	 * @return
	 */
	private Shell addInfoShell(Shell shell) {

		infoShell = new Shell(shell);
		infoShell.setText("CRS Details");
		infoShell.setSize(200,200);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		infoShell.setLayout(gridLayout);

		String crsDescription = shapefileContainer.getVerboseCRS();

		labelInfo= new Label(infoShell, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL);
		gridData.widthHint = 400;
		gridData.heightHint = 600;
		gridData.horizontalSpan = 1;

		labelInfo.setText(crsDescription.toString());
		labelInfo.setLayoutData(gridData);

		return infoShell;
	}


	/**
	 * 
	 * @param shell
	 */
	private void addWidgets(Shell shell) {
		Shell myShell = shell;
		String dataDir = "externaldata/";

		// start creating widgets - order is important
		// Row 1
		new CreateWidgets().createLabel(myShell,new GridData(GridData.BEGINNING), "Browse for a ShapeFile: ", 200, 1); // create info label
		buttonBrowse = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Browse", 2); // create draw button

		// Row 2
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Using ShapeFile: ", 200, 1); // create info label
		labelFileSelection = new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "No File Selected. Please Select a Shapefile.", 600, 2); // create info label

		// Row 3
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Shape Identifier", 200, 1); // create info label for the List beneath it
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Attribute List", 600, 1); // create info label for the List beneath it
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Coordinate List", 200, 1); // create info label for the List beneath it

		// Row 4
		listID = new CreateWidgets().createList(myShell, new GridData(GridData.FILL, GridData.FILL, true, true), 200, 400, 1); // create list populated with IDs
		listAttributes = new CreateWidgets().createList(myShell, new GridData(GridData.FILL, GridData.FILL, true, true), 500, 400, 1); // create list populated with metadata
		listCoordinates = new CreateWidgets().createList(myShell, new GridData(GridData.FILL, GridData.FILL, true, true), 200, 400, 1); // create list populated with coordinates

		// Row 5
		labelCSR = new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "CRS : Not Defined", 200, 1); // create info label
		buttonViewCRS = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Details", 2); // create button

		// Row 6
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Press to display map: ", 200, 1); // create info label
		buttonMap = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Open Map", 2); // create draw button

		// Row 7
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "EPSG File :", 200, 1); // create info label
		comboEPSG = new CreateWidgets().createCombo(myShell, new GridData(GridData.BEGINNING), 400, 1); 
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 8
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Source Coordinate System :", 200, 1); // create info label
		comboSource = new CreateWidgets().createCombo(myShell, new GridData(GridData.FILL), 400, 1);
		buttonDetectCRS = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Auto-Detect CRS", 1); // create draw button

		// Row 9
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Target Coordinate System :", 200, 1); // create info label
		comboTarget = new CreateWidgets().createCombo(myShell, new GridData(GridData.FILL), 400, 1);
		buttonSearchCRS = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Search and Set CRS", 1); // create draw button

		// Row 10
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Export to CSV :", 200, 1); // create info label
		buttonRawCSV = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Export to CSV", 1); // create draw button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 11
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Transform and Export to CSV :", 200, 1); // create info label
		buttonCSV = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Transform to CSV", 1); // create draw button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 12
		new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Export to KML (to WGS 1984) :", 200, 1); // create info label
		buttonKML = new CreateWidgets().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Export to KML", 1); // create draw button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 13
		labelStatus = new CreateWidgets().createLabel(myShell, new GridData(GridData.FILL), "Status : ", 200, 1); // create status label
		STATUS_LIST = new CreateWidgets().createList(myShell, new GridData(GridData.FILL, GridData.FILL_HORIZONTAL, false, false), 100, 50, 1); // create list populated with IDs 

		// Add listeners
		buttonDetectCRS.ergoButton.addSelectionListener(this);
		buttonBrowse.getErgoButton().addSelectionListener(this);
		buttonMap.getErgoButton().addSelectionListener(this);
		buttonKML.getErgoButton().addSelectionListener(this);
		comboSource.ergoCombo.addSelectionListener(this);
		comboTarget.ergoCombo.addSelectionListener(this);
		comboEPSG.ergoCombo.addSelectionListener(this);
		listID.ergoList.addSelectionListener(this);
		buttonSearchCRS.ergoButton.addSelectionListener(this);
		buttonViewCRS.ergoButton.addSelectionListener(this);

		// populate the EPSG Combo Widget
		boolean isDirectoryEmpty = FileOperations.isDirectoryEmpty(dataDir);
		if (!isDirectoryEmpty) {
			String[] listFiles = FileOperations.listFilesInDir(dataDir);
			if (listFiles != null) {
				comboEPSG.ergoCombo.setItems(listFiles);
				comboEPSG.ergoCombo.select(0);
			}
		} else {Log.Err("Missing EPSG Files", 0, true);}

		// populate the CRS selection Combo Widgets after the EPSG Combo Widget has been populated
		fileName = comboEPSG.ergoCombo.getItem(comboEPSG.ergoCombo.getSelectionIndex());
		if (!fileName.isEmpty() || !fileName.equals("") || fileName != null) {
			Log.Out("Using EPSG file : " + fileName, 0, true);
			String[] listCRS = new ProcessingOperations().createComboString(fileName);
			comboSource.ergoCombo.setItems(listCRS);
			comboTarget.ergoCombo.setItems(listCRS);
		} else { Log.Err("Empty file selected.", 0, true);
		}
		changeStateOfWidgets(false);
	}

	/**
	 * Activates or Deactivates a group of predetermined SWT Button widgets.
	 * @param state : Boolean TRUE or FALSE
	 */
	private void changeStateOfWidgets(boolean state) {

		buttonKML.ergoButton.setEnabled(state);
		buttonCSV.ergoButton.setEnabled(state);
		buttonRawCSV.ergoButton.setEnabled(state);
		buttonViewCRS.ergoButton.setEnabled(state);
		buttonMap.ergoButton.setEnabled(state);
		comboEPSG.ergoCombo.setEnabled(state);
		comboSource.ergoCombo.setEnabled(state);
		comboTarget.ergoCombo.setEnabled(state);
		buttonSearchCRS.ergoButton.setEnabled(state);
		buttonDetectCRS.ergoButton.setEnabled(state);

	}

	/**
	 * Accesses the geometry collection and retrieves the ShapeFile Coordinate Reference System.
	 * @return The Coordinate Reference System as String
	 */
	private String getShapefileSystem() {
		ErgoPolyline currentPolyline = Iterables.get(shapefileContainer.getGeometryCollection(), 0);

		String referenceSystem = currentPolyline.getReferenceSystem();
		String referenceLabel = referenceSystem.replace('_', ' ');

		if (referenceLabel.isEmpty()) {
			Log.Err("Missing CRS", 0, true);
			return null;
		} else {
			return referenceLabel;
		}
	}

	private void performSearch() {
		
		
		
		String text = textSearch.getText().intern();

		if (text != null && text != "") {
			String[] table = comboSource.ergoCombo.getItems();
			int index = 0;

			ArrayList<Integer> keptIndexes = new ArrayList<Integer>();

			for (String item : table) {

				if (StringUtils.containsIgnoreCase(item, text.intern())) {
					listSearch.ergoList.select(index);
					keptIndexes.add(index);
				}
				index++;
			} // end for

			String[] newPopulation = new String[keptIndexes.size()];

			int count = 0;
			for (int token : keptIndexes) {

				newPopulation[count] = table[token];
				count ++;

			}
			listSearch.ergoList.setItems(newPopulation);
		} else {
			listSearch.ergoList.setItems(comboSource.ergoCombo.getItems());
		}
	}

	/**
	 * Searches the contents of a combo widget for a match with the current coordinate system and sets the selection index to it.
	 * @param inputCombo : Target SWT Combo widget
	 */
	private void setComboIndex(ErgoCombo inputCombo) {
		int EPSG_CODE = shapefileContainer.getEpsgCode();

		if (EPSG_CODE != -1) {
			String text = String.valueOf(EPSG_CODE);
			text = text.intern();

			if (text != null && text != "") {
				String[] table = comboSource.ergoCombo.getItems();
				int indexCount = 0;

				for (String item : table) {

					if (StringUtils.containsIgnoreCase(item, text)) {
						comboSource.ergoCombo.select(indexCount);
						setSystem();
						return;
					} 
					indexCount++;
				} // end for
			}
		}
	}

	@Override
	public void widgetDisposed(DisposeEvent disposeEvent) {

		int callerHashCode = disposeEvent.getSource().hashCode();

		if (callerHashCode == mainShell.hashCode()) {
			Log.Out("Exiting...", 0, true);
			mainShell.dispose();
			System.exit(0);
		} 

	}

	@Override
	public void widgetDefaultSelected(SelectionEvent defaultEvent) {
		int callerHashCode = defaultEvent.getSource().hashCode();
		if (callerHashCode == comboEPSG.getErgoID()) {
			comboEPSG.ergoCombo.select(0);
		} else if (callerHashCode == listID.getErgoID()) {
			listID.ergoList.deselectAll();
		}
	}

	URL shpLocation;

	@Override
	public void widgetSelected(SelectionEvent selectionEvent) {

		int callerHashCode = selectionEvent.getSource().hashCode();

		if (callerHashCode == buttonBrowse.getErgoID()) {
			Log.Out("Detected click on Browse Button " + selectionEvent.getSource().hashCode(), 1, false);
			Log.Out("Browsing for file...", 0, true);
			mainShell.setCapture(false); // disable mouse clicks on main shell

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					shpLocation = new FileOperations().browseAndReturnURL(); // user input for file
				}
			});

			if(shpLocation == null) {
				mainShell.setCapture(true);
				mainShell.setFocus();
				Log.Err("No file selected.", 0, false);
				return;
			}
			mainShell.setCapture(true);
			mainShell.setFocus();
			mainShell.setVisible(true);

			filePath.setStringPath(shpLocation.toString());
			labelFileSelection.setText(filePath.getStringPath());

			listID.ergoList.removeAll();
			listAttributes.ergoList.removeAll();
			listCoordinates.ergoList.removeAll();
			String[] clearCache = new String[1];
			clearCache[0] = "";
			listID.ergoList.setItems(clearCache);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					shapefileContainer = new ParseShapefile().parseURLshapefile(shpLocation);
				}
			});

			if (shapefileContainer.getGeometryCollection() == null) {
				changeStateOfWidgets(false);
				return;
			}
			changeStateOfWidgets(true);

			String shpCRS = getShapefileSystem();
			Log.Out("Reference System is : " + shpCRS , 1 , true);
			labelCSR.setText("CRS: " + shpCRS); // change the label displaying the reference system

			// iterate through the collection and create a String [] to store IDs
			String[] plineIDList = new String[shapefileContainer.getGeometryCollection().size()];
			plineIDList = null;
			plineIDList = new String[shapefileContainer.getGeometryCollection().size()];
			int iter_PolylineCount = 0;

			for (ErgoPolyline singlePolyline : shapefileContainer.getGeometryCollection()) {

				String plineID = singlePolyline.getID();
				int dot = plineID.indexOf(".");
				plineIDList[iter_PolylineCount] = plineID.substring(dot + 1);
				iter_PolylineCount ++;

			} // end iteration

			setComboIndex(comboSource);
			listID.ergoList.setItems(plineIDList); // populate the list

		} else if (callerHashCode == buttonMap.getErgoID()) {

			new MapDisplay().shapefileDisplay(filePath.getPathAsFile());
			Log.Out("Displaying contents of file." , 1, true);

		} else if (callerHashCode == buttonKML.getErgoID()) {

			if (shapefileContainer.getGeometryCollection() != null && REFERENCE_SETS.getSourceSystem() != null) {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						new ExportKML().createKML(shapefileContainer, REFERENCE_SETS.getSourceSystem());
					}
				});
			} else {
				Log.Err("KML creation failed to start.", 0, true);
			}

		} else if (callerHashCode == comboEPSG.getErgoID()) {

			fileName = comboEPSG.ergoCombo.getItem(comboEPSG.ergoCombo.getSelectionIndex());
			System.out.println("Selected " + fileName);

			if (!fileName.isEmpty() || !fileName.equals("") || fileName != null) {
				String[] listCRS = new ProcessingOperations().createComboString(fileName);
				comboSource.ergoCombo.setItems(listCRS);
				comboTarget.ergoCombo.setItems(listCRS);
			}

		} else if (callerHashCode == comboSource.getErgoID()) {
			Log.Out("Detected action on Combo Source " + selectionEvent.getSource().hashCode(), 1, false);
			setSystem();

		}  else if (callerHashCode == comboTarget.getErgoID()) {
			Log.Out("Detected action on Combo Target " + selectionEvent.getSource().hashCode(), 1, false);
			String selectedItem = comboTarget.ergoCombo.getItem(comboTarget.ergoCombo.getSelectionIndex());

			ErgoReferenceSystem selectedCRS = new ProcessingOperations().splitComboString(selectedItem);
			Log.Out("Selected " + selectedItem, 1, true);
			REFERENCE_SETS.setTargetSystem(selectedCRS);


		}  else if (callerHashCode == buttonDetectCRS.getErgoID()) {
			Log.Out("Detected click on Detect CRS Button " + selectionEvent.getSource().hashCode(), 1, false);
			Log.Out("Detecting Coordinate Reference System. Please wait...", 0, true);
			setComboIndex(comboSource);

		} else if (callerHashCode == listID.getErgoID()) {
			Log.Out("Detected click on ID List " + selectionEvent.getSource().hashCode(), 1, false);
			listAttributes.ergoList.removeAll();
			listCoordinates.ergoList.removeAll();
			String[] selectedItems = listID.ergoList.getSelection();

			String outString = "";
			for (int loopIndex = 0; loopIndex < selectedItems.length; loopIndex++)
				outString += selectedItems[loopIndex] + " ";
			Log.Out("Selected Items: " + outString, 1, true);

			if (!outString.equalsIgnoreCase("") && !outString.equalsIgnoreCase(null)) { // very important, keep it here or else everything breaks
				int selectionIndex = Integer.valueOf(outString.trim()) - 1;

				if (selectionIndex <= shapefileContainer.getGeometryCollection().size() && selectionIndex >= 0) { // sanity check for selection

					ErgoPolyline currentPolyline = Iterables.get(shapefileContainer.getGeometryCollection(), Integer.valueOf(outString.trim())-1);

					Log.Out("HashMapSize : " + currentPolyline.getHashMapSize(), 1, false);

					String[] stringKeysPline = currentPolyline.getStringKeys();

					String[] coordinateTable = new ProcessingOperations().buildCoordinateList(currentPolyline); // call method to create list items
					String[] attributeTable = new ProcessingOperations().buildAttributeList(stringKeysPline, currentPolyline);

					listAttributes.ergoList.setItems(attributeTable);
					listAttributes.ergoList.redraw();

					listCoordinates.ergoList.setItems(coordinateTable);
					listCoordinates.ergoList.redraw();
				}
			}

		}  else if (callerHashCode == buttonSearchCRS.getErgoID()) {
			Log.Out("Detected click on Search Button " + selectionEvent.getSource().hashCode(), 1, false);

			searchShell = addSearchShell(mainShell);

			Log.Out("Opening Search Shell...", 0, true);

			searchShell.open();
			searchShell.pack();

		}  else if (searchShellCreated && callerHashCode == buttonSearch1.getErgoID()) {
			Log.Out("Detected click on Clear Search Button " + selectionEvent.getSource().hashCode(), 1, false);

			textSearch.setText("");

		} else if (searchShellCreated && callerHashCode == buttonSearch2.getErgoID()) {
			Log.Out("Detected click on Set Source Button " + selectionEvent.getSource().hashCode(), 1, false);

			int countSelected = listSearch.ergoList.getSelectionCount();
			if (countSelected == 1) {

				String[] contents = listSearch.ergoList.getSelection();
				String selection = contents[0];

				String[] comboItems = comboSource.ergoCombo.getItems();

				int count = 0;
				for (String item : comboItems) {

					if (item.equalsIgnoreCase(selection)) {
						comboSource.ergoCombo.select(count);
						setSystem();
						labelStatus.setText(comboSource.ergoCombo.getText());
						break;
					}
					count ++;
				}

			}

		} else if (searchShellCreated && callerHashCode == buttonSearch3.getErgoID()) {
			Log.Out("Detected click on Set Target Button " + selectionEvent.getSource().hashCode(), 1, false);
			int countSelected = listSearch.ergoList.getSelectionCount();
			if (countSelected == 1) {

				String[] contents = listSearch.ergoList.getSelection();
				String selection = contents[0];

				String[] comboItems = comboTarget.ergoCombo.getItems();

				int count = 0;
				for (String item : comboItems) {

					if (item.equalsIgnoreCase(selection)) {
						comboTarget.ergoCombo.select(count);
						labelInfo.setText(comboTarget.ergoCombo.getText());
						break;
					}
					count ++;
				}

			}

		} else if (callerHashCode == buttonViewCRS.getErgoID()) {
			Log.Out("Detected click on View CRS Button " + selectionEvent.getSource().hashCode(), 1, false);
			infoShell = addInfoShell(mainShell);

			Log.Out("Opening Info Shell...", 0, true);

			infoShell.open();
			infoShell.pack();

		} else {
			Log.Err("Unidentified Command Intercepted.", 0 , false);
			return;
		}

	}

	private void setSystem() {

		String selectedItem = comboSource.ergoCombo.getItem(comboSource.ergoCombo.getSelectionIndex());

		Log.Out("Selected " + selectedItem, 1, true);

		ErgoReferenceSystem selectedCRS = new ProcessingOperations().splitComboString(selectedItem);
		REFERENCE_SETS.setSourceSystem(selectedCRS);

	}

	public static ErgoList getSTATUS_LIST() {
		if (STATUS_LIST != null) {
			return STATUS_LIST;
		} else {
			return null;
		}

	}

}