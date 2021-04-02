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
package org.commonjava.indy.service.archive.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.commonjava.indy.service.archive.controller.ArchiveController;
import org.commonjava.indy.service.archive.jaxrs.ResponseHelper;
import org.commonjava.indy.service.archive.model.dto.HistoricalContentDTO;
import org.commonjava.indy.service.archive.util.HistoricalContentListReader;
import org.commonjava.indy.service.archive.util.TransferStreamingOutput;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class ArchiveManagerService {

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    ResponseHelper responseHelper;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    HistoricalContentListReader reader;

    @Inject
    ArchiveController controller;

    public Uni<Response> create(final UriInfo uriInfo,final HttpRequest request) {

        HistoricalContentDTO content;

        try {
            String json = IOUtils.toString( request.getInputStream(), Charset.defaultCharset() );
            content = objectMapper.readValue( json, HistoricalContentDTO.class );
            if ( content == null ) {
                return sendError(500,"Failed to read historical content which is empty.");
            }
        } catch ( final IOException e ) {
            return sendError(500,"Failed to read historical content file from request body.");
        }

        try {
            Map<String, String> downloadPaths = reader.readPaths( content );
            controller.downloadArtifacts( downloadPaths, content );
            Optional<File> archive = controller.generateArchive( content );
            if ( archive.isEmpty() )
            {
                return sendError(500,"Failed to get downloaded contents for archive.");
            }
            controller.renderArchive( archive.get(), content.getBuildConfigId() );
        } catch ( InterruptedException e ) {
            return sendError(500,"Artifacts downloading is interrupted.");
        } catch ( final ExecutionException e ) {
            return sendError(500,"Artifacts download execution manager failed.");
        } catch ( final IOException e ) {

            return sendError(500,"Failed to generate historical archive from content.");
        }

        return Uni.createFrom().item(uriInfo.getRequestUri())
                .onItem().transform(uri -> Response.created(uri).build());
    }

    public Uni<Boolean> delete(final String buildConfigId){

        try {
            controller.deleteArchive( buildConfigId );
        } catch ( final IOException e ) {
            return Uni.createFrom().item(false);
        }

        return Uni.createFrom().item(true);

    }

    public Uni<Response> get( String buildConfigId, final UriInfo uriInfo ){
        InputStream inputStream = null;

        try {
            Optional<File> file = controller.getArchiveInputStream( buildConfigId );
            if(file.isPresent()){
                inputStream = FileUtils.openInputStream( file.get() );
            }
        } catch ( final IOException e ) {
            return sendError(500,"Failed to generate historical archive from content.");
        }

        return buildWithHeader(Response.ok(new TransferStreamingOutput( inputStream )),buildConfigId);
    }

    private Uni<Response> buildWithHeader(Response.ResponseBuilder builder, final String buildConfigId ) {
        return Uni.createFrom().item(new StringBuilder())
                .onItem().transform(header -> header.append("attachment;").append( "filename=" ).append( buildConfigId ).append( ".zip" ))
                .onItem().transform(re -> builder.header("Content-Disposition", re.toString()).build());
    }

    private Uni<Response> sendError(int status, String message){
        return responseHelper.fromResponseReactive(message)
                .onItem().transform(re -> Response.status(status,re))
                .onItem().transform(Response.ResponseBuilder::build);
    }
}
