/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.progettosad.mediaplayergruppo11.db;

/**
 *
 * @author gcucc
 */
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    
    private static final Properties properties = new Properties();

    // Blocco statico: viene eseguito una sola volta quando la classe viene caricata in memoria
    static {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Impossibile trovare il file database.properties");
            }
            // Carica le proprietà dal file
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Errore durante la lettura del file di configurazione del DB", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        // Recupera le credenziali dalla mappa delle properties
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        
        return DriverManager.getConnection(url, user, password);
    }
    
    
}
