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
    
    
    
    public boolean makeTable(String text) {
        
        Connection connection;
        Statement statement;
        
        try {
            
            Singleton.getInstance();
            Singleton.openConnection();
            
            connection = Singleton.getConnection();
            connection.setAutoCommit(true);
            connection.setReadOnly(false);
            
            statement = connection.createStatement();
            int creationResult = statement.executeUpdate(text);
            Log.Out("Created Table with Result " + creationResult, 1, true);
            
            statement.close();
            Singleton.closeConnection();
            return true;
            
        } catch (SQLException | DataAccessException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            Singleton.closeConnection();
            return false;
        }
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
    
    public void insertDataToTable(int ID, ErgoPolyline line, String schema) {
        PreparedStatement prepStmt = null;
        
        try {
            // use a prepared statement to avoid injections
            
            Log.Out("Received " + ID + " for object " + line, 2, true);
            
            
            prepStmt = Singleton.getConnection().prepareStatement(schema);
            
            int countColumn = 1;
            
            // set ? variables by index order
            prepStmt.setInt(countColumn, ID);
            
            for (String key : line.getStringKeys()) {
                countColumn++;
                prepStmt.setString(countColumn, String.valueOf(line.getString(key)));
                
            }
            
            // get 0 or 1, depending on success
            int result = prepStmt.executeUpdate();
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            Log.Exception(e, 1);
            
        } finally {
            // close statement
            if (prepStmt != null)
                try {
                    prepStmt.close();
                    
                } catch (SQLException logOrIgnore) {
                }
        }
        
    }
    
    
    
    
    public void processCollection(Collection<ErgoPolyline> collection, String fileName) {
        
        String mFileName = fileName.replaceAll(".shp", "");
        
        StringBuilder queryStringBuilder = new StringBuilder();
        StringBuilder schemaStringBuilder = new StringBuilder();
        StringBuilder valueStringBulder = new StringBuilder();
        
        queryStringBuilder.append("CREATE TABLE ");
        queryStringBuilder.append(mFileName);
        queryStringBuilder.append(" (ID INT PRIMARY KEY NOT NULL");
        
        schemaStringBuilder.append("(ID");
        valueStringBulder.append("VALUES(?");
        
        boolean firstPass = true;
        
        ErgoPolyline lineZero = collection.iterator().next();
        for (String key : lineZero.getStringKeys()) {
            Log.Out(key, 1, true);
            queryStringBuilder.append(",");
            queryStringBuilder.append(key);
            queryStringBuilder.append(" TEXT");
            
            schemaStringBuilder.append(" ,");
            schemaStringBuilder.append(key);
            
            valueStringBulder.append(", ?");

        }
         // TODO Add Type and Vertex List
        queryStringBuilder.append(")");
        schemaStringBuilder.append(") ");
        valueStringBulder.append(");");
        
        Log.Out(queryStringBuilder.toString(), 1, true);
        Log.Out("Creating Table " + mFileName, 1, true);
        boolean didICreateTable = makeTable(queryStringBuilder.toString());
        
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
        }
        
    }
    
}
