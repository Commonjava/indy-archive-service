/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.service.archive.jaxrs;

import io.smallrye.mutiny.Uni;
import org.commonjava.indy.service.archive.model.dto.HistoricalContentDTO;
import org.commonjava.indy.service.archive.services.ArchiveManagerService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

@Path( "/api/archive" )
public class ArchiveManageResources
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    ArchiveManagerService archiveManagerService;

    @Operation( description = "Generate archive based on tracked content" )
    @APIResponse( responseCode = "201", description = "The archive is created successfully" )
    @RequestBody( description = "The tracked content definition JSON", name = "body", required = true,
            content = @Content( schema = @Schema( implementation = HistoricalContentDTO.class ) ) )
    @POST
    @Path( "generate" )
    @Consumes( APPLICATION_JSON )
    public Uni<Response> create(final @Context UriInfo uriInfo, final @Context HttpRequest request ) {
        return archiveManagerService.create(uriInfo,request);
    }

    @Operation( description = "Get latest historical build archive by buildConfigId" )
    @APIResponse( responseCode = "200", description = "Get the history archive successfully" )
    @APIResponse( responseCode = "204", description = "The history archive doesn't exist" )
    @Path( "{buildConfigId}" )
    @Produces ( APPLICATION_OCTET_STREAM )
    @GET
    public Uni<Response> get( final @PathParam( "buildConfigId" ) String buildConfigId, final @Context UriInfo uriInfo )
    {
        return archiveManagerService.get(buildConfigId,uriInfo);
    }

    @Operation( description = "Delete the build archive by buildConfigId" )
    @APIResponse( responseCode = "204", description = "The history archive is deleted or doesn't exist" )
    @Path( "{buildConfigId}" )
    @DELETE
    public Uni<Response> delete( final @PathParam( "buildConfigId" ) String buildConfigId, final @Context UriInfo uriInfo )
    {
        return archiveManagerService.delete(buildConfigId)
                .onItem().transform(deleted -> deleted ? Status.NO_CONTENT:Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

}
