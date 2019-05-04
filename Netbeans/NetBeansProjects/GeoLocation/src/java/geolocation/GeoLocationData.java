/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolocation;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeoLocationData {


    
    private DecimalDegrees getCoordinates(int latitude, int latitude_arcminute, double latitude_arcsecond, int longitude, int longitude_arcminute, double longitude_arcsecond) {

        DecimalDegrees dd = new DecimalDegrees();
        dd.setLatitude(latitude + latitude_arcminute / 60.0 + latitude_arcsecond / 3600.0);
        dd.setLongitude(longitude + longitude_arcminute / 60.0 + longitude_arcsecond / 3600.0);
        return (dd);
    }

    static class DecimalDegrees {

        // Decimal degrees (DD) express latitude and longitude geographic coordinates as decimal fractions
        final int precision = 4;
        private double latitude = 0;
        private double longitude = 0;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = round(latitude, precision, BigDecimal.ROUND_HALF_UP);
        }

        public void setLongitude(double longitude) {
            this.longitude = round(longitude, precision, BigDecimal.ROUND_HALF_UP);
        }

        private static Double toRad(Double value) {
            return value * Math.PI / 180;
        }

        private double getDistance(DecimalDegrees waypoint) {
            /**
             * This is the implementation Haversine Distance Algorithm between
             * two places
             *
             * @author ananth R = earth’s radius (mean radius = 6,371km) Δlat =
             * lat2− lat1 Δlong = long2− long1 a = sin²(Δlat/2) +
             * cos(lat1).cos(lat2).sin²(Δlong/2) c = 2.atan2(√a, √(1−a)) d = R.c
             *
             */
            final int R = 6371; // Radious of the earth
            Double latDistance = toRad(waypoint.getLatitude() - getLatitude());
            Double lonDistance = toRad(waypoint.getLongitude() - getLongitude());
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(toRad(getLatitude())) * Math.cos(toRad(waypoint.getLatitude()))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            Double distance = R * c;
            return (round(distance, precision, BigDecimal.ROUND_HALF_UP));
            //            return (round(((Math.acos(Math.sin(getLatitude() * Math.PI / 180) * Math.sin(waypoint.getLatitude() * Math.PI / 180) + Math.cos(getLatitude() * Math.PI / 180) * Math.cos(waypoint.getLatitude() * Math.PI / 180) * Math.cos(getLongitude() - waypoint.getLongitude()) * Math.PI / 180)) * 180 / Math.PI * 60 * 1.1515), precision, BigDecimal.ROUND_HALF_UP));
        }

        private static double round(double unrounded, int precision, int roundingMode) {
            BigDecimal bd = new BigDecimal(unrounded);
            BigDecimal rounded = bd.setScale(precision,
                    roundingMode);
            return rounded.doubleValue();
        }
    }
}
