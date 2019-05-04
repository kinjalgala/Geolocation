/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Christopher Paolini
 */
@Singleton
public class LocationStorageBean {

    private int venue = 1;
  
    /**
     * Web service operation
     *
     * @return
     */
    public String getLocation() {
        
        // WGS84
        //
        final double a = 6378137.0;                     // m, semi-major axis
        final double b = 6356752.314245;                // m, semi-minor axis b
        final double sdsuLatitude‎ = 32.774799;          // San Diego State University
        final double lat_r = sdsuLatitude‎ * Math.PI/180;   // radians
        final double metersPerDregreeLatitude = Math.PI/180 * ( a*a * b*b / Math.pow( (a*a + b*b)/2, 3.0/2.0) ) / Math.pow(1 + ((a*a - b*b) / (a*a + b*b)) * Math.cos(2 * lat_r), 3.0/2.0); 
        final double metersPerDregreeLongitude = Math.PI/180 * ( a*a / Math.pow( (a*a + b*b)/2, 1.0/2.0 ) ) * Math.cos(lat_r) / Math.pow(1 + ((a*a - b*b)/(a*a + b*b)) * Math.cos( 2 * lat_r), 1.0/2.0) ;
        String[] listNames;
        int i = 0;
        JSONArray jsonArray = new JSONArray();
        JSONObject oJSONObject = new JSONObject();
        ResultSet rs = null;
       
        {
        if (venue <= 0) {
            try {
                oJSONObject.put("error", "venue is invalid");
            } catch (JSONException ex) {
                Logger.getLogger(GeoLocationData.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
               
                geolocation.DatabaseManager databaseManager = geolocation.DatabaseManager.getInstance();
                String query = ("SELECT DISTINCT x,y,z,timestamp,latitude,longitude,device FROM location, venues WHERE location.venue = venues.id ORDER BY timestamp DESC LIMIT 10");
                rs = databaseManager.query(query);
                
               
                while (rs != null && rs.next()) {
                    
                    oJSONObject.put("timestamp", rs.getTimestamp("timestamp"));
                    oJSONObject.put("lat", rs.getDouble("latitude") + rs.getDouble("y")/metersPerDregreeLatitude );
                    oJSONObject.put("long", rs.getDouble("longitude") + rs.getDouble("x")/metersPerDregreeLongitude );
                    oJSONObject.put("alt", rs.getDouble("z"));
                    oJSONObject.put("ID", rs.getString("device"));
                        
                   jsonArray.put(oJSONObject);
                }
            
               
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            } catch (JSONException ex) {
                System.err.println(ex.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlex) {
                        System.err.println(sqlex.getMessage());
                    }
                }
            }
        }
        
       
  
        }
        return jsonArray.toString();
    }
    
}
    
