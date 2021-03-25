package org.commonjava.indy.service.archive;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.commonjava.indy.service.archive.config.PreSeedConfig;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/rest")
public class ContentResource {

    @Inject
    PreSeedConfig pConfig;

    @GET
    @Path("/{path: (.*)}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<Response> getFile(@PathParam String path) {
        File nf = new File(pConfig.dir, path);
        Response.ResponseBuilder response = Response.ok(nf);
        response.header("Content-Disposition", "attachment;filename=" + nf.getName());
        Uni<Response> re = Uni.createFrom().item(response.build());
        return re;
    }
}
