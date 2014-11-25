package com.michaelfotiadis.shpparser.userinterface.layouts;

import java.io.File;
import java.net.MalformedURLException;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Iterables;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoReferenceSet;
import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoReferenceSystem;
import com.michaelfotiadis.shpparser.containers.ergo.widgets.ErgoButton;
import com.michaelfotiadis.shpparser.containers.ergo.widgets.ErgoCombo;
import com.michaelfotiadis.shpparser.containers.ergo.widgets.ErgoList;
import com.michaelfotiadis.shpparser.containers.file.FileContainer;
import com.michaelfotiadis.shpparser.containers.file.ShapefileContainer;
import com.michaelfotiadis.shpparser.containers.interfaces.ParserInterface;
import com.michaelfotiadis.shpparser.export.csv.ProcessorCSVExporter;
import com.michaelfotiadis.shpparser.export.kml.ProcessorKMLExporter;
import com.michaelfotiadis.shpparser.export.sql.ProcessorSQLiteExporter;
import com.michaelfotiadis.shpparser.helpers.ProcessingOperations;
import com.michaelfotiadis.shpparser.parsers.shapefile.ProcessorShpParser;
import com.michaelfotiadis.shpparser.userinterface.viewer.MapDisplay;
import com.michaelfotiadis.shpparser.userinterface.widgets.WidgetFactory;
import com.michaelfotiadis.shpparser.util.file.FileOperations;
import com.michaelfotiadis.shpparser.util.system.Log;

/**
 * Class containing user interface SWT components and listeners
 * @author Michael Fotiadis
 *
 */
public class MainParserLayout implements DisposeListener, SelectionListener {
	// object container
	private ShapefileContainer shapefileContainer;
	// label fields
	private Label labelFileSelection;
	private Label labelCSR;
	private Label labelStatus;
	private Label labelInfo;
	// button fields
	private ErgoButton buttonBrowse;
	private ErgoButton buttonMap;
	private ErgoButton buttonKML;
	private ErgoButton buttonExportTransformedCSV;
	private ErgoButton buttonExportRawCSV;
	private ErgoButton buttonSearchCRS;
	private ErgoButton buttonViewCRS;
	private ErgoButton buttonDetectCRS;
	private ErgoButton buttonExportSQL;
	// combo fields
	private ErgoCombo comboSource;
	private ErgoCombo comboTarget;
	private ErgoCombo comboEPSG;
	// list fields
	private ErgoList listID;
	private ErgoList listAttributes;
	private ErgoList listCoordinates;
	// search layout fields
	private Text textSearch;
	private ErgoButton buttonSearch1;
	private ErgoButton buttonSearch2;
	private ErgoButton buttonSearch3;
	private ErgoList listSearch;

	// list for displaying information
	private static ErgoList STATUS_LIST;

	private String fileName = "";
	private Shell mainShell;
	private Shell searchShell;
	private Shell infoShell;

	private final FileContainer filePath = new FileContainer();

	private boolean searchShellCreated = false;

	private Display display;

	private ErgoReferenceSet REFERENCE_SETS = new ErgoReferenceSet();

	private URL shpLocation;

	@SuppressWarnings("deprecation")
	private void actionBrowseAndParseShapefile() {
		Log.Out("Browsing for file...", 0, true);
		mainShell.setCapture(false); // disable mouse clicks on main shell

		changeStateOfWidgets(false);

		// look for an input file and return it
		FileDialog dialog = new FileDialog(mainShell);
		dialog.setFilterExtensions(new String[] {"*.shp"});

		String userSelectedPath =  dialog.open();

		if (userSelectedPath == null || userSelectedPath.length() == 0) {
			Log.Err("No file selected.", 0, true);
			if (shapefileContainer != null) {
				changeStateOfWidgets(true);
			}
			return;
		}

		try {
			shpLocation = new File(userSelectedPath).toURL();
		} catch (MalformedURLException e) {
			Log.Exception(e, 0);
			Log.Err("Error opening file.", 0, true);
			if (shapefileContainer != null) {
				changeStateOfWidgets(true);
			}
		}

		// restore Shell functionality
		mainShell.setCapture(true);
		mainShell.setFocus();
		// show the file path in the label widget
		filePath.setStringPath(shpLocation.toString());
		labelFileSelection.setText(filePath.getStringPath());

		// clear all lists
		listID.ergoList.removeAll();
		listAttributes.ergoList.removeAll();
		listCoordinates.ergoList.removeAll();
		String[] clearCache = new String[1];
		clearCache[0] = "";
		listID.ergoList.setItems(clearCache);


		Log.Out("Parsing File", 0, true);
		// start a new thread to parse the file
		ProcessorShpParser shpProcessor = new ProcessorShpParser(shpLocation);

		executeTask(shpProcessor);
	}

	private void setUIData() {
		if (shapefileContainer == null || shapefileContainer.getGeometryCollection() == null) {
			Log.Out("Null ShapefileData", 0, true);
			return;
		}
		final String shpCRS = getShapefileSystem();
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
	}


	private void actionExportToCSV(boolean doTransform) {
		changeStateOfWidgets(false);

		// do an initial check for null geometry and / or reference system
		if (shapefileContainer != null && shapefileContainer.getGeometryCollection() != null) {
			final SafeSaveDialog saveDialog = new SafeSaveDialog(mainShell);
			saveDialog.setFilterExtensions(new String[]{"*.csv"});

			final String destination = saveDialog.open();

			if (destination != null) {
				// set up the processor
				ProcessorCSVExporter processorThread;
				if (doTransform) {
					processorThread = 	new ProcessorCSVExporter(new File(destination), shapefileContainer, 
							REFERENCE_SETS.getSourceSystem(), REFERENCE_SETS.getTargetSystem());
				} else {
					processorThread = 	new ProcessorCSVExporter(new File(destination), shapefileContainer, 
							REFERENCE_SETS.getSourceSystem(), null);
				}
				executeTask(processorThread);
			} else {
				Log.Err("No file selected", 1, true);
				changeStateOfWidgets(true);
			}
		} else {
			Log.Err("KML creation failed to start.", 0, true);
			changeStateOfWidgets(true);
		}
	}

	/**
	 * Action for exporting to KML (opens a file chooser)
	 */
	private void actionExportToKML() {
		changeStateOfWidgets(false);

		// do an initial check for null geometry and / or reference system
		if (shapefileContainer != null && shapefileContainer.getGeometryCollection() != null && REFERENCE_SETS.getSourceSystem() != null) {
			final SafeSaveDialog saveDialog = new SafeSaveDialog(mainShell);
			saveDialog.setFilterExtensions(new String[]{"*.kml"});

			final String destination = saveDialog.open();

			if (destination != null) {
				// set and start the processor
				ProcessorKMLExporter processorRunnable = new ProcessorKMLExporter(
						new File(destination), shapefileContainer,
						REFERENCE_SETS.getSourceSystem());
				executeTask(processorRunnable);
			} else {
				Log.Err("No file selected", 1, true);
				changeStateOfWidgets(true);
			}
		} else {
			Log.Err("KML creation failed to start.", 0, true);
			changeStateOfWidgets(true);
		}
	}

	/**
	 * Method for handling exporting to SQLite
	 */
	private void actionExportToSQLite() {
		if (shapefileContainer != null && shapefileContainer.getGeometryCollection() != null) {

			Log.Out("Starting SQL Export", 1, true);
			File shpFile = new File(shpLocation.toString());

			// initialise the runnable
			Runnable sqlProcessor = new ProcessorSQLiteExporter(shapefileContainer.getGeometryCollection(), shpFile.getName());

			// start the processor
			executeTask(sqlProcessor);
		}
	}

	/**
	 * Displays an information shell
	 * @param shell 
	 * @return
	 */
	private Shell addInfoShell() {

		infoShell = new Shell();
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
	 * The method creates a Shell in a Display defined by input
	 * @param firstDisplay : The Display (JAVA.SWT) on which the Shell will be created
	 * @return
	 */
	private Shell addMainShell(final Display firstDisplay) {
		mainShell = new Shell(firstDisplay, SWT.MAX | SWT.RESIZE | SWT.MIN);
		mainShell.setText("Shapefile Parser and Exporter");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		mainShell.setLayout(gridLayout);
		Log.Out("Successfully Created Main Shell.", 0, false);

		mainShell = createMenuItems(mainShell); // Create Menu Items, should expand

		addWidgets(mainShell); // Create all the Widgets

		mainShell.addDisposeListener(this);
		mainShell.pack();

		return mainShell;
	}


	/**
	 * 
	 * @param shell
	 */
	private void addWidgets(Shell shell) {
		Shell myShell = shell;
		String dataDir = "externaldata/";

		WidgetFactory widgetFactory = new WidgetFactory();

		// start creating widgets - order is important
		// Row 1
		widgetFactory.createLabel(myShell,new GridData(GridData.BEGINNING), "Browse for a ShapeFile: ", 200, 1); // create info label
		buttonBrowse = new WidgetFactory().createButton(myShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Browse", 2); // create draw button

		// Row 2
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Using ShapeFile: ", 200, 1); // create info label
		labelFileSelection = new WidgetFactory().createLabel(myShell, new GridData(GridData.FILL), "No File Selected. Please Select a Shapefile.", 600, 2); // create info label

		// Row 3
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Shape Identifier", 200, 1); // create info label for the List beneath it
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Attribute List", 600, 1); // create info label for the List beneath it
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Coordinate List", 200, 1); // create info label for the List beneath it

		// Row 4
		listID = widgetFactory.createList(myShell, new GridData(GridData.FILL, GridData.FILL, true, true), 200, 400, 1); // create list populated with IDs
		listAttributes = widgetFactory.createList(myShell, new GridData(GridData.FILL, GridData.FILL, true, true), 500, 400, 1); // create list populated with metadata
		listCoordinates = widgetFactory.createList(myShell, new GridData(GridData.FILL, GridData.FILL, true, true), 200, 400, 1); // create list populated with coordinates

		// Row 5
		labelCSR = widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "CRS : Not Defined", 200, 1); // create info label
		buttonViewCRS = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Details", 2); // create button

		// Row 6
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Press to display map: ", 200, 1); // create info label
		buttonMap = new WidgetFactory().createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Open Map", 2); // create draw button

		// Row 7
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "EPSG File :", 200, 1); // create info label
		comboEPSG = widgetFactory.createCombo(myShell, new GridData(GridData.FILL), 400, 1); 
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 8
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Source Coordinate System :", 200, 1); // create info label
		comboSource = widgetFactory.createCombo(myShell, new GridData(GridData.FILL), 400, 1);
		buttonDetectCRS = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Auto-Detect CRS", 1); // create draw button

		// Row 9
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Target Coordinate System :", 200, 1); // create info label
		comboTarget = widgetFactory.createCombo(myShell, new GridData(GridData.FILL), 400, 1);
		buttonSearchCRS = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Search and Set CRS", 1); // create draw button

		// Row 10
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Export to CSV :", 200, 1); // create info label
		buttonExportRawCSV = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Export to CSV", 1); // create draw button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 11
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Transform and Export to CSV :", 200, 1); // create info label
		buttonExportTransformedCSV = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Transform and Export to CSV", 1); // create draw button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 12
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Export to KML (to WGS 1984) :", 200, 1); // create info label
		buttonKML = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Export to KML File", 1); // create draw button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 13
		widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Export to SQL :", 200, 1); // create info label
		buttonExportSQL = widgetFactory.createButton(myShell, new GridData(GridData.FILL, GridData.CENTER, false, false), "Export to SQL Database", 1); // create export button
		new Label(myShell, SWT.NONE); // empty label, used to arrange widgets

		// Row 14
		labelStatus = widgetFactory.createLabel(myShell, new GridData(GridData.FILL), "Status : ", 200, 1); // create status label
		STATUS_LIST = widgetFactory.createList(myShell, new GridData(GridData.FILL, GridData.FILL_HORIZONTAL, false, false), 100, 50, 1); // create list populated with IDs 

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
		buttonExportSQL.ergoButton.addSelectionListener(this);
		buttonExportRawCSV.ergoButton.addSelectionListener(this);
		buttonExportTransformedCSV.ergoButton.addSelectionListener(this);

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
		} else { 
			Log.Err("Empty file selected.", 0, true);
		}

		changeStateOfWidgets(false);
	}

	/**
	 * 
	 */
	private void buildSearchShell() {

		Log.Out("Creating Search Shell..." , 0, true);
		searchShell = new Shell();
		searchShell.setText("Search");
		searchShell.setSize(200,200);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;

		searchShell.setLayout(gridLayout);

		// create a factory for the creation of widgets
		WidgetFactory widgetFactory = new WidgetFactory();

		// Row 1
		textSearch = new Text(searchShell, SWT.SINGLE | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		textSearch.setLayoutData(gridData);
		buttonSearch1 = widgetFactory.createButton(searchShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Clear", 1); // create draw button

		// Row 2
		listSearch = widgetFactory.createList(searchShell, new GridData(GridData.FILL, GridData.FILL, true, true), 400, 400, 2); // create list populated with IDs

		// Row 3
		buttonSearch2 = widgetFactory.createButton(searchShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Set Source", 1); // create draw button
		labelStatus = widgetFactory.createLabel(searchShell, new GridData(GridData.FILL), comboSource.ergoCombo.getText(), 500, 1); // create info label

		// Row 4
		buttonSearch3 = widgetFactory.createButton(searchShell, new GridData(GridData.BEGINNING, GridData.CENTER, false, false), "Set Target", 1); // create draw button
		labelInfo = widgetFactory.createLabel(searchShell, new GridData(GridData.FILL), comboTarget.ergoCombo.getText(), 400, 1); // create info label

		listSearch.ergoList.setItems(comboSource.ergoCombo.getItems());

		buttonSearch1.ergoButton.addSelectionListener(this);
		buttonSearch2.ergoButton.addSelectionListener(this);
		buttonSearch3.ergoButton.addSelectionListener(this);

		textSearch.addModifyListener(new ModifyListener() {
			// initialise the search
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

		Log.Out("Opening Search Shell...", 0, true);

		searchShell.open();
		searchShell.pack();
	}

	/**
	 * Activates or Deactivates a group of predetermined SWT Button widgets.
	 * @param state : Boolean TRUE or FALSE
	 */
	private void changeStateOfWidgets(boolean state) {
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				buttonKML.ergoButton.setEnabled(state);
				buttonExportTransformedCSV.ergoButton.setEnabled(state);
				buttonExportRawCSV.ergoButton.setEnabled(state);
				buttonViewCRS.ergoButton.setEnabled(state);
				buttonMap.ergoButton.setEnabled(state);
				comboEPSG.ergoCombo.setEnabled(state);
				comboSource.ergoCombo.setEnabled(state);
				comboTarget.ergoCombo.setEnabled(state);
				buttonSearchCRS.ergoButton.setEnabled(state);
				buttonDetectCRS.ergoButton.setEnabled(state);
				buttonExportSQL.ergoButton.setEnabled(state);
				
				if (shapefileContainer != null) {
					buttonBrowse.ergoButton.setEnabled(state);
				}
				mainShell.update();
			}
		});
	}

	/**
	 * Method for starting a non-UI Thread and monitoring its execution. Several UI widgets are disabled while the Threads are alive.
	 * @param processorRunnable Runnable which will do all the work
	 */
	private void executeTask(final Runnable processorRunnable) {
		// disable UI widgets before starting
		changeStateOfWidgets(false);

		// create a thread to handle the runnable
		final Thread processorThread = new Thread(processorRunnable);

		// create a thread that will monitor the processor
		final Thread monitorThread = new Thread(new Runnable() {
			@Override
			public void run() {
				processorThread.start();

				// wait for the processor thread to finish
				synchronized (processorThread) {
					try {
						processorThread.wait();
					} catch (InterruptedException e) {
						Log.Exception(e, 1);
					}

					// if it's a parser, retrieve the parsed data once finished
					if (processorRunnable instanceof ParserInterface) {
						try {
							shapefileContainer = (ShapefileContainer) ((ParserInterface) processorRunnable).getData();
							// update the UI through a SWT thread
							Display.getDefault().syncExec( new Runnable() {
								public void run() {
									setUIData();
								}
							});
						} catch (Exception e) {
							Log.Exception(e, 1);
						}
					} 

					// start a SWT UI thread to update the UI
					Display.getDefault().syncExec( new Runnable() {
						public void run() {
							// change widget state
							changeStateOfWidgets(true);
						}
					});
				}
			}
		});
		// start the monitor thread
		monitorThread.start();
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

	/**
	 * Initialise the Display
	 */
	public void initUI() {
		display = new Display();
		final Shell mainShell = new MainParserLayout().addMainShell(display);
		mainShell.open();

		Log.Out("UI Initialised successfully. Please browse for a Shapefile.", 1, true);
		while (!mainShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
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

	private void setSystem() {

		String selectedItem = comboSource.ergoCombo.getItem(comboSource.ergoCombo.getSelectionIndex());

		Log.Out("Selected " + selectedItem, 1, true);

		ErgoReferenceSystem selectedCRS = new ProcessingOperations().splitComboString(selectedItem);
		REFERENCE_SETS.setSourceSystem(selectedCRS);

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
	public void widgetSelected(SelectionEvent selectionEvent) {

		int callerHashCode = selectionEvent.getSource().hashCode();

		if (callerHashCode == buttonBrowse.getErgoID()) {
			actionBrowseAndParseShapefile();
		} else if (callerHashCode == buttonMap.getErgoID()) {
			Log.Out("Displaying Preview of File." , 1, true);
			new MapDisplay().shapefileDisplay(filePath.getPathAsFile());
		} else if (callerHashCode == buttonKML.getErgoID()) {
			actionExportToKML();

		} else if (callerHashCode == comboEPSG.getErgoID()) {

			fileName = comboEPSG.ergoCombo.getItem(comboEPSG.ergoCombo.getSelectionIndex());
			Log.Out("Selected " + fileName, 1, true);

			if (!fileName.isEmpty() || !fileName.equals("") || fileName != null) {
				String[] listCRS = new ProcessingOperations().createComboString(fileName);
				comboSource.ergoCombo.setItems(listCRS);
				comboTarget.ergoCombo.setItems(listCRS);
			}

		} else if (callerHashCode == comboSource.getErgoID()) {
			setSystem();

		}  else if (callerHashCode == comboTarget.getErgoID()) {
			String selectedItem = comboTarget.ergoCombo.getItem(comboTarget.ergoCombo.getSelectionIndex());

			ErgoReferenceSystem selectedCRS = new ProcessingOperations().splitComboString(selectedItem);
			Log.Out("Selected " + selectedItem, 1, true);
			REFERENCE_SETS.setTargetSystem(selectedCRS);


		}  else if (callerHashCode == buttonDetectCRS.getErgoID()) {
			Log.Out("Detecting Coordinate Reference System. Please wait...", 0, true);
			setComboIndex(comboSource);

		} else if (callerHashCode == listID.getErgoID()) {
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
			buildSearchShell();
		}  else if (searchShellCreated && callerHashCode == buttonSearch1.getErgoID()) {
			textSearch.setText("");
		} else if (searchShellCreated && callerHashCode == buttonSearch2.getErgoID()) {
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
			infoShell = addInfoShell();

			Log.Out("Opening Info Shell...", 0, true);

			infoShell.open();
			infoShell.pack();
		} else if (callerHashCode == buttonExportSQL.getErgoID()) {
			actionExportToSQLite();
		} else if (callerHashCode == buttonExportRawCSV.getErgoID()) {
			Log.Out("Starting CSV Export", 1, true);
			actionExportToCSV(false);
		} else if (callerHashCode == buttonExportTransformedCSV.getErgoID()) {
			Log.Out("Starting Transformed CSV Export", 1, true);
			actionExportToCSV(true);
		} else {
			Log.Err("Unidentified Command Intercepted.", 0 , false);
			return;
		}

	}


	/**
	 * Method for generating MenuItems and adding them to a Shell
	 * @param inputShell Shell to be used
	 */
	private static Shell createMenuItems(final Shell inputShell) {
		// Menu item section
		Menu menuBar = new Menu(inputShell, SWT.BAR);
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");
		Menu fileMenu = new Menu(inputShell, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);

		final MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText("&Exit"); // exit button
		inputShell.setMenuBar(menuBar);

		exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputShell.getDisplay().dispose();
				System.exit(0);
			}
		});// end menu item section
		return inputShell;
	}


	public static void updateStatusList(String text) {
		Display.getDefault().syncExec( new Runnable() {
			public void run() {
				STATUS_LIST.ergoList.add(text);
				STATUS_LIST.ergoList.setTopIndex(STATUS_LIST.ergoList.getItemCount() - 1); // ensure list always scrolls down
				STATUS_LIST.ergoList.redraw();
			}
		});
	}




}