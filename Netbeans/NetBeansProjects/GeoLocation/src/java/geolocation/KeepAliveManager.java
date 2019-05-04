/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package geolocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christopher Paolini
 */
public class KeepAliveManager implements Runnable {

    KeepAliveManager() {
    }

    @Override
    public void run() {
        String query = "SELECT * FROM keep_alive";  // SQL statement that is innocuous and quick
        ResultSet rs;
        while (true) {
            DatabaseManager databaseManager = DatabaseManager.getInstance();
            rs = databaseManager.query(query);
            if (rs == null) {
                System.err.println("KeepAliveManager query failed (" + Thread.currentThread().getName());
            } else {
                System.err.println("KeepAliveManager query succeeded (" + Thread.currentThread().getName());                       
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(KeepAliveManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
            try {
                Thread.sleep(1000 * 60 * 60);   // sleep for 1 hr
            } catch (InterruptedException ex) {
                System.err.println(KeepAliveManager.class.getName() + ": " + ex.getMessage());
            }
        }
    }
}
