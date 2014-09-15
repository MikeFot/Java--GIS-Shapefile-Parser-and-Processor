/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package com.michaelfotiadis.shpparser.datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.michaelfotiadis.shpparser.util.system.Log;

public class Singleton {
    private static volatile Singleton instance = null;
    private static String database;
    private static Connection connection;
    
    public static String getDatabase() {
        return database;
    }
    
    
    public static Connection getConnection() {
        return connection;
    }
    
    
    private Singleton() {
        database = "jdbc:sqlite:./output/Database_SQLite.db";
    }
    
    public static void openConnection() {
        
        // set the database connection
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(database);
            System.out.println("Opened Connection " + connection.hashCode());
        } catch (    SQLException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            Log.Exception(e, 3);
        }
        
    }
    
    public static void closeConnection() {
        try {
            // close the connection
            if (connection == null || connection.isClosed()) {
                return;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Singleton.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            connection.close();
            System.out.println("Connection " + connection.hashCode()
                    + " closed");
        } catch (SQLException logOrIgnore) {
        }
        
    }
    
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
    
    public static void setInstance(Singleton instance) {
        Singleton.instance = instance;
    }
}
