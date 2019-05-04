/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package geolocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton DatabaseManager class
 *
 * @author Christopher Paolini
 */
final public class DatabaseManager {

    private static DatabaseManager instance = null;
    private static Connection conn = null;
    private static InitialContext ctx = null;
    private static Thread keepAliveThread = null;

    static void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlex) {
                sqlex.printStackTrace();
            }
        }
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException namingException) {
                namingException.printStackTrace();
            }
            ctx = null;
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private DatabaseManager() {
        try {
            /*
             * Create a JNDI Initial context to be able to lookup the DataSource
             *
             * In production-level code, this should be cached as an instance or
             * static variable, as it can be quite expensive to create a JNDI
             * context.
             *
             * Note: This code only works when you are using servlets or EJBs in
             * a J2EE application server. If you are using connection pooling in
             * standalone Java code, you will have to create/configure
             * datasources using whatever mechanisms your particular connection
             * pooling library provides.
             */

            ctx = new InitialContext();

            /*
             * Lookup the DataSource, which will be backed by a pool that the
             * application server provides. DataSource instances are also a good
             * candidate for caching as an instance variable, as JNDI lookups
             * can be expensive as well.
             */
            DataSource ds = (DataSource) ctx.lookup("jdbc/Postgres");

            /*
             * The following code is what would actually be in your Servlet, JSP
             * or EJB 'service' method...where you need to work with a JDBC
             * connection.
             */
            conn = ds.getConnection();

            keepAliveThread = new Thread(new KeepAliveManager());
            keepAliveThread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException namingException) {
                    namingException.printStackTrace();
                }
                ctx = null;
            }
        }
    }

    public Connection getConnection() {
        return conn;
    }

    ResultSet query(final String sqlStatement) {
        Statement stmt;
        ResultSet rs = null;
        try {
            if (conn == null) {
                Integer hashCode = this.hashCode();
                System.err.println("[DatabaseManager] error: can not create a database connection (object hash code: " + hashCode + ")");
            } else {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sqlStatement);
                if (rs == null) {
                    System.err.println("[DatabaseManager] error: query '" + sqlStatement + "' returned a NULL result set");
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return rs;
    }

    int execute(String sqlStatement) {
        PreparedStatement stmt = null;
        int returnValue = -2;
        try {
            if (conn == null) {
                Logger.getLogger(GeoLocationData.class.getName()).log(Level.SEVERE, null, "[DatabaseManager] error: can not create a database connection");
            } else {
                stmt = conn.prepareStatement(sqlStatement);
                if (stmt.execute() == false) {
                    returnValue = -1;
                } else {
                    returnValue = stmt.getUpdateCount();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return returnValue;
    }
}
