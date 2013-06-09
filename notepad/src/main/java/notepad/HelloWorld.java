package notepad;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.json.JSONWithPadding;

@Path("/pad/{username}/{padname}")
public class HelloWorld {

    @GET
    @Path("textonly")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTextOnly(//
            @PathParam("username") final String username, //
            @PathParam("padname") final String padname) {
        return "Cheese strings!";
    }

    @GET
    @Produces("application/x-javascript")
    public JSONWithPadding getFullPad(//
            @PathParam("username") final String username, //
            @PathParam("padname") final String padname, //
            @QueryParam("callback") final String callback) {
        final FullPad result = new FullPad(username, padname, "Cheese strings!");
        return new JSONWithPadding(result, callback);
    }

    @GET
    @Path("metaonly")
    @Produces("application/x-javascript")
    public JSONWithPadding getMetadataOnly(//
            @PathParam("username") final String username, //
            @PathParam("padname") final String padname, //
            @QueryParam("callback") final String callback) {
        final FullPad result = new FullPad(username, padname, null);
        return new JSONWithPadding(result, callback);
    }

}