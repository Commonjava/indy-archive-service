/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.indy.service.archive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.commonjava.indy.service.archive.config.PreSeedConfig;
import org.commonjava.indy.service.archive.model.dto.HistoricalContentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ApplicationScoped
public class ArchiveController {

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    PreSeedConfig preSeedConfig;

    @Inject
    ObjectMapper objectMapper;

    private ExecutorService executorService;

    private CloseableHttpClient client;

    private String contentDir;

    private String archiveDir;

    private final String CONTENT_DIR = "/content";

    private final String ARCHIVE_DIR = "/archive";

    private final String ARCHIVE_SUFFIX = ".tar.gz";

    private final String PART_SUFFIX = ".part";

    private final String PART_ARCHIVE_SUFFIX = PART_SUFFIX + ARCHIVE_SUFFIX;

    @PostConstruct
    public void init()
    {
        int threads = 4 * Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool( threads, ( final Runnable r ) -> {
            final Thread t = new Thread( r );
            t.setDaemon( true );
            return t;
        } );

        final PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager();
        ccm.setMaxTotal( 500 );

        RequestConfig rc = RequestConfig.custom().build();
        final HttpClientBuilder builder = HttpClients.custom().setConnectionManager( ccm ).setDefaultRequestConfig( rc );
        client = builder.build();

        String storeDir = preSeedConfig.storageDir.orElse( "data" );
        contentDir = String.format( "%s%s", storeDir, CONTENT_DIR );
        archiveDir = String.format( "%s%s", storeDir, ARCHIVE_DIR );
    }

    @PreDestroy
    public void destroy()
    {
        IOUtils.closeQuietly( client, null );
    }

    public void downloadArtifacts( final Map<String, String> downloadPaths, final HistoricalContentDTO content )
    {
        BasicCookieStore cookieStore = new BasicCookieStore();
        ExecutorCompletionService executor = new ExecutorCompletionService<>( executorService );

        String contentBuildDir = String.format( "%s/%s", contentDir, content.getBuildConfigId() );
        File dir = new File( contentBuildDir );
        dir.delete();

        fileTrackedContent( contentBuildDir, content );

        for ( String path : downloadPaths.keySet() )
        {
            String filePath = downloadPaths.get( path );
            executor.submit( download( contentBuildDir, path, filePath, cookieStore ) );
        }
    }

    public File generateArchiveZip( final HistoricalContentDTO content )
            throws IOException
    {
        String contentBuildDir = String.format( "%s/%s", contentDir, content.getBuildConfigId() );
        File dir = new File( contentBuildDir );
        if ( !dir.exists() )
        {
            return null;
        }

        String archiveBuildDir = String.format( "%s/%s", archiveDir, content.getBuildConfigId() );
        final File part = new File( archiveBuildDir, content.getTrackId() + PART_ARCHIVE_SUFFIX );
        part.getParentFile().mkdirs();

        logger.info( "Writing archive zip to: '{}'", part.getAbsolutePath() );
        ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( part ) );
        List<File> artifacts = Files.walk( Paths.get( contentBuildDir ) )
                .filter( Files::isRegularFile )
                .map( Path::toFile )
                .collect( Collectors.toList() );

        byte[] buffer = new byte[1024];
        for ( File artifact : artifacts )
        {
            logger.trace( "Adding {} to zip{} in folder {}", artifact.getName(), content.getTrackId(), archiveBuildDir );
            FileInputStream fis = new FileInputStream( artifact );
            String entryPath = artifact.getPath().split( contentBuildDir )[1];

            zip.putNextEntry( new ZipEntry( entryPath ) );

            int length;
            while ((length = fis.read(buffer)) > 0)
            {
                zip.write(buffer, 0, length);
            }
            zip.closeEntry();
            fis.close();
        }
        zip.close();

        //clean obsolete build contents
        for ( File artifact : artifacts )
        {
            Files.delete( artifact.toPath() );
        }
        dir.delete();
        return part;
    }

    public void renderArchive( File part, final String buildConfigId, final String trackId )
    {
        if ( part != null && part.exists() )
        {
            String archiveBuildDir = String.format( "%s/%s", archiveDir, buildConfigId );
            final File target = new File( archiveBuildDir, trackId + ARCHIVE_SUFFIX );
            target.delete();
            target.getParentFile().mkdirs();
            part.renameTo( target );
        }
    }

    private void fileTrackedContent( String contentBuildDir, final HistoricalContentDTO content )
    {
        File tracked = new File( contentBuildDir, content.getTrackId() );
        tracked.getParentFile().mkdirs();

        try
        {
            String json = objectMapper.writeValueAsString( content );
            FileOutputStream out = new FileOutputStream( tracked );
            IOUtils.copy( new ByteArrayInputStream( json.getBytes() ), out );
        }
        catch ( IOException e )
        {
            final String message = "Failed to file tracked content.";
            logger.error( message, e );
        }
    }

    private Callable<Boolean> download( String contentBuildDir, final String path,
                                        final String filePath, final CookieStore cookieStore )
    {
        return () -> {
            Thread.currentThread().setName( "download--" + path );

            final File target = new File( contentBuildDir, filePath );
            final File dir = target.getParentFile();
            dir.mkdirs();
            final File part = new File( dir, target.getName() + PART_SUFFIX );

            final HttpClientContext context = new HttpClientContext();
            context.setCookieStore( cookieStore );
            final HttpGet request = new HttpGet( path );
            try
            {
                CloseableHttpResponse response = client.execute( request, context );
                int statusCode = response.getStatusLine().getStatusCode();
                if ( statusCode == 200 )
                {
                    try ( FileOutputStream out = new FileOutputStream( part ) )
                    {
                        IOUtils.copy( response.getEntity().getContent(), out );
                    }
                    part.renameTo( target );
                    return true;
                }
                else if ( statusCode == 404 )
                {
                    logger.trace( "<<<Not Found path: " + path );
                    return false;
                }
                else
                {
                    logger.trace( "<<<Error path: " + path );
                    return false;
                }
            }
            catch ( final Exception e )
            {
                e.printStackTrace();
                logger.trace( "Download failed for path: " + path );
            }
            finally
            {
                request.releaseConnection();
                request.reset();
            }
            return false;
        };
    }
}