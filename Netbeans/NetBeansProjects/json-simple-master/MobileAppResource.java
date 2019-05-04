/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package geolocation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Christopher Paolini
 */
@Stateless
@Path("/app")
public class MobileAppResource {
    @EJB
    private LocationStorageBean locationStorage;   

    @GET
    @Produces("application/json")
    public String getLocation() {
        return locationStorage.getLocation();
    }
}
