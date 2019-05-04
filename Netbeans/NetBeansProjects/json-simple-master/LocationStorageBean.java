/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
        int venue = 1;                                  // TODO: this should be an IN argument
        
        JSONObject oJSONObject = new JSONObject();
        JSONArray oJSONArray = new JSONArray();
        ResultSet rs = null;
        if (venue <= 0) {
            try {
                oJSONObject.put("error", "venue is invalid");
            } catch (JSONException ex) {
                Logger.getLogger(GeoLocationData.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                geolocation.DatabaseManager databaseManager = geolocation.DatabaseManager.getInstance();
                // String query = "SELECT x,y,z,timestamp,latitude,longitude FROM location, venues WHERE location.venue = venues.id ORDER BY timestamp DESC LIMIT 1";
                String query = "SELECT current_location(" + venue + ");";
                rs = databaseManager.query(query);
                while (rs != null && rs.next()) {
                    String current_location = rs.getString("current_location").replaceAll("[()]", "");
                    List<String> items = Arrays.asList(current_location.split("\\s*,\\s*"));                 
                    
                    // 0                   1                   2                   3                            4               5                          6                          7                           8       
                    // x double precision, y double precision, z double precision, ts timestamp with time zone, venue1 integer, device1 character varying, latitude double precision, longitude double precision, venue2 integer
                    
                    JSONObject tuple = new JSONObject();                    
                    tuple.put("timestamp", items.get(3));
                    tuple.put("lat", Double.parseDouble(items.get(6)) + Double.parseDouble(items.get(1))/metersPerDregreeLatitude );
                    tuple.put("long", Double.parseDouble(items.get(7)) + Double.parseDouble(items.get(0))/metersPerDregreeLongitude );
                    tuple.put("alt", Double.parseDouble(items.get(2)));
                    tuple.put("device", items.get(5));
                    oJSONArray.put(tuple);
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
        return oJSONArray.toString();
    }
}
