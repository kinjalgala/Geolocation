/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
 * Singleton session bean used to store the name parameter for "/GeoLocation"
 * resource
 *
 * @author mkuchtiak
 */
@Singleton
public class NameStorageBean {

    // name field
    private String name = "World Again";
    private int venue = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Web service operation
     *
     * @param json (example: {"Y": 8, "X": 5, "Z": 3, "ID": "USER0", "Site": 1})
     */
    public void setLocation(String json) {
        System.err.println("NameStorageBean: POST: " + json);
        double x = 0, y = 0, z = 0;
        String id = "";
        int site = 1;       // default is engineering building

        try {
            JSONObject oJSONObject = new JSONObject(json);
            if (oJSONObject.has("Location")) {
                JSONObject oLocation = oJSONObject.getJSONObject("Location");
                if (oLocation.has("X")) {
                    x = oLocation.getDouble("X");
                }
                if (oLocation.has("Y")) {
                    y = oLocation.getDouble("Y");
                }
                if (oLocation.has("Z")) {
                    z = oLocation.getDouble("Z");
                }
            }
            if (oJSONObject.has("ID")) {
                id = oJSONObject.getString("ID");
            }
            if (oJSONObject.has("Site")) {
                site = oJSONObject.getInt("Site");
            }

            String query = "INSERT INTO location (x,y,z,device,timestamp,venue) VALUES ("
                    + "'" + x + "',"
                    + "'" + y + "',"
                    + "'" + z + "',"
                    + "'" + id + "',"
                    + "CURRENT_TIMESTAMP,"
                    + "'" + site + "')";

            // System.err.println("NameStorageBean: POST query: " + query);

            DatabaseManager databaseManager = DatabaseManager.getInstance();
            databaseManager.execute(query);

            query = "DELETE FROM location WHERE device = '" + id + "' AND timestamp < NOW() - INTERVAL '30 MINUTE'";
            databaseManager.execute(query);

        } catch (JSONException ex) {
            System.err.println("NameStorageBean: POST: " + ex.getMessage());
        }
    }

    /**
     * Web service operation
     *
     * @return
     */
    public String getAccessPoints() {
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
                String query = "SELECT id,x,y,z,room,mac,calibration FROM ap";
                rs = databaseManager.query(query);
                while (rs != null && rs.next()) {
                    JSONObject tuple = new JSONObject();
                    tuple.put("id", rs.getString("id"));
                    tuple.put("room", rs.getString("room"));
                    tuple.put("mac", rs.getString("mac"));
                    tuple.put("x", rs.getDouble("x"));
                    tuple.put("y", rs.getDouble("y"));
                    tuple.put("z", rs.getDouble("z"));
                    tuple.put("cal", rs.getDouble("calibration"));
                    tuple.put("ssid", "eduroam");
                    oJSONArray.put(tuple);
                }
                oJSONObject.put("ap", oJSONArray);
            } catch (Exception ex) {
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
        return oJSONObject.toString();
    }
}
