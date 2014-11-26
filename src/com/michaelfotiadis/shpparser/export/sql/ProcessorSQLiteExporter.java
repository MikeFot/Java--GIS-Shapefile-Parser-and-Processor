/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.michaelfotiadis.shpparser.export.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jooq.exception.DataAccessException;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.containers.interfaces.ExporterInterface;
import com.michaelfotiadis.shpparser.datastore.Singleton;
import com.michaelfotiadis.shpparser.util.system.Log;

/**
 *
 * @author Michael Fotiadis
 */
public class ProcessorSQLiteExporter implements ExporterInterface, Runnable {
	
	private Collection<ErgoPolyline> collection;
	private String fileName;

	/**
	 * Handles database entry for a Collection of ErgoPolyline
	 * @param collection ErgoPolyline Collection
	 * @param fileName Location of the database
	 */
	public ProcessorSQLiteExporter(Collection<ErgoPolyline> collection, String fileName) {
		this.collection = collection;
		this.fileName = fileName;
	}
	
	@Override
	public void run() {
		// sanitise the filename
		String mFileName = fileName.replaceAll(".shp", "");

		// initialise the query builders
		StringBuilder queryStringBuilder = new StringBuilder();
		StringBuilder schemaStringBuilder = new StringBuilder();
		StringBuilder valueStringBulder = new StringBuilder();

		// add the first elements ot the builders
		queryStringBuilder.append("CREATE TABLE ");
		queryStringBuilder.append(mFileName);
		queryStringBuilder.append(" (ID INT PRIMARY KEY NOT NULL, COORDINATES TEXT");

		schemaStringBuilder.append("(ID, COORDINATES");
		valueStringBulder.append("VALUES(?,?");

		// first line contains the Column IDs
		ErgoPolyline lineZero = collection.iterator().next();
		for (String key : lineZero.getStringKeys()) {
			Log.Out(key, 1, true);
			// try to avoid duplicate ones
			// TODO check all keys for duplicates, even user-provided ones
			if (key.equalsIgnoreCase("ID")) {
				key = "ID_1";
			}
			if (key.equalsIgnoreCase("COORDINATES")) {
				key = "COORDINATES_1";
			}

			queryStringBuilder.append(",");
			queryStringBuilder.append(key);
			queryStringBuilder.append(" TEXT");

			schemaStringBuilder.append(" ,");
			schemaStringBuilder.append(key);

			valueStringBulder.append(", ?");
		}
		
		// TODO Add Type
		queryStringBuilder.append(")");
		schemaStringBuilder.append(") ");
		valueStringBulder.append(");");

		Log.Out(queryStringBuilder.toString(), 1, true);
		Log.Out("Creating Table " + mFileName, 1, true);
		
		boolean didICreateTable = makeTable(queryStringBuilder.toString());

		if (didICreateTable) {
			Log.Out("Table did not exist so it had to be created", 0, false);
		}

		// allow both creation & updating of data
		String builderPrefix = "INSERT OR IGNORE INTO " + mFileName + " ";

		// finalise the Insertion Schema
		String insertSchema = builderPrefix + schemaStringBuilder.toString() + valueStringBulder.toString();

		Log.Out("Insert Schema is " + insertSchema, 1, true);

		try {
			// open connection through a Singleton to ensure that only one is open at any time
			Singleton.openConnection();
			Connection connection = Singleton.getConnection();
			
			// auto-commit results
			connection.setAutoCommit(true);
			// write access
			connection.setReadOnly(false);

			// increment the ID field
			int id = 0;
			for (ErgoPolyline line : collection) {
				id++;
				// call the method to insert data to the table
				insertDataToTable(id, line, insertSchema);
			}
		} catch (SQLException ex) {
			Logger.getLogger(ProcessorSQLiteExporter.class.getName()).log(Level.SEVERE, null, ex);
			Singleton.closeConnection();
		} finally {
			// ensure that the SQL connection is closed
			Singleton.closeConnection();
			Log.Out("Finished exporting to SQL", 1, true);
		}
	}

	/**
	 * Handles SQLite data insertion for an ErgoPolyline object
	 * @param ID integer id of the object
	 * @param line Object to be inserted
	 * @param schema Schema to be used
	 */
	private static void insertDataToTable(int ID, ErgoPolyline line, String schema) {
		PreparedStatement prepStmt = null;

		try {
			// use a prepared statement to avoid injections
			prepStmt = Singleton.getConnection().prepareStatement(schema);

			// initialise the column index
			int countColumn = 1;

			// set ? variables by index order
			prepStmt.setInt(countColumn, ID);

			// add coordinates
			countColumn ++;
			prepStmt.setString(countColumn, line.getVertexListAsString());

			for (String key : line.getStringKeys()) {
				countColumn++;
				prepStmt.setString(countColumn, String.valueOf(line.getString(key)));
			}

			// get 0 or 1, depending on success
			int result = prepStmt.executeUpdate();
			Log.Out("Processed object " + ID + " (" + result + ")", 2, true);
		} catch (SQLException e) {
			Log.Exception(e, 1);
		} finally {
			// close statement
			if (prepStmt != null)
				try {
					prepStmt.close();
				} catch (SQLException ex) {
					Log.Exception(ex, 1);
				}
		}

	}

	/**
	 * Creates a database table
	 * @param text Name of the table to be created
	 * @return True if successful
	 */
	private static boolean makeTable(String text) {
		try {
			// initialise connection
			Singleton.getInstance();
			Singleton.openConnection();

			Connection connection = Singleton.getConnection();
			if (connection == null)
				return false;

			connection.setAutoCommit(true);
			connection.setReadOnly(false);

			Statement statement = connection.createStatement();
			int creationResult = statement.executeUpdate(text);
			Log.Out("Created Table with Result " + creationResult, 1, true);

			statement.close();

			return true;
		} catch (SQLException | DataAccessException e) {
			Log.Exception(e, 0);
			return false;
		} finally {
			Singleton.closeConnection();
		}
	}

}
