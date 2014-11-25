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

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.michaelfotiadis.shpparser.containers.ergo.geometry.ErgoPolyline;
import com.michaelfotiadis.shpparser.datastore.Singleton;
import com.michaelfotiadis.shpparser.util.system.Log;

/**
 *
 * @author Michael Fotiadis
 */
public class ExportSQL {
	/**
	 * Handles SQLite data insertion for an ErgoPolyline object
	 * @param ID integer id of the object
	 * @param line Object to be inserted
	 * @param schema Schema to be used
	 */
	public void insertDataToTable(int ID, ErgoPolyline line, String schema) {
		PreparedStatement prepStmt = null;

		try {
			// use a prepared statement to avoid injections

			Log.Out("Processing object " + ID, 2, true);

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
			Log.Out("Update result is " + result, 2, false);
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
	public boolean makeTable(String text) {
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

	/**
	 * Handles database entry for a Collection of ErgoPolyline
	 * @param collection ErgoPolyline Collection
	 * @param fileName Location of tha database
	 */
	public void processCollection(Collection<ErgoPolyline> collection, String fileName) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub


				String mFileName = fileName.replaceAll(".shp", "");

				StringBuilder queryStringBuilder = new StringBuilder();
				StringBuilder schemaStringBuilder = new StringBuilder();
				StringBuilder valueStringBulder = new StringBuilder();

				queryStringBuilder.append("CREATE TABLE ");
				queryStringBuilder.append(mFileName);
				queryStringBuilder.append(" (ID INT PRIMARY KEY NOT NULL, COORDINATES TEXT");

				schemaStringBuilder.append("(ID, COORDINATES");
				valueStringBulder.append("VALUES(?,?");

				ErgoPolyline lineZero = collection.iterator().next();
				for (String key : lineZero.getStringKeys()) {
					Log.Out(key, 1, true);
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

				String builderPrefix = "INSERT OR IGNORE INTO " + mFileName + " ";

				String insertSchema = builderPrefix + schemaStringBuilder.toString() + valueStringBulder.toString();

				Log.Out("Insert Schema is " + insertSchema, 1, true);

				try {
					Singleton.openConnection();
					Connection connection = Singleton.getConnection();
					connection.setAutoCommit(true);
					connection.setReadOnly(false);

					int id = 0;
					for (ErgoPolyline line : collection) {
						id++;
						insertDataToTable(id, line, insertSchema);
					}
				} catch (SQLException ex) {
					Logger.getLogger(ExportSQL.class.getName()).log(Level.SEVERE, null, ex);
					Singleton.closeConnection();
				} finally {
					Singleton.closeConnection();
					Log.Out("Finished exporting to SQL", 1, true);
				}
			}
		});
		thread.start();
		
	}

	public Result<Record> selectQuery(String dbTable, String fieldColumn, String fieldValue) {

		try {
			// Open the connection - requires try/catch
			Singleton.getConnection().setAutoCommit(false);
			Singleton.getConnection().setReadOnly(true);

			// Make a new context
			DSLContext create = DSL.using(Singleton.getConnection(), SQLDialect.SQLITE);

			Table<?> mTable = DSL.table(dbTable);
			Field<Object> mColumn = DSL.field(fieldColumn);

			SelectQuery<Record> query;

			if(fieldColumn == null || fieldValue == null) {
				query = create.selectQuery();
				query.addFrom(mTable);
			} else {
				Field<Object> mValue = DSL.field(fieldValue);
				query = create.selectQuery();
				query.addFrom(mTable);
				query.addConditions(mColumn.equal(mValue));
			}

			Result<Record> result = query.fetch();
			Singleton.closeConnection();
			return result;

		} catch (SQLException | DataAccessException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			Singleton.closeConnection();
			System.exit(0);
		}
		return null;
	}

}
