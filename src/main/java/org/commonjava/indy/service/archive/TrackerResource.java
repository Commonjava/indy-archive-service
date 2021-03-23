package org.commonjava.indy.service.archive;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.commonjava.indy.service.archive.config.PreSeedConfig;
import org.commonjava.indy.service.archive.model.TrackedContent;
import org.commonjava.indy.service.archive.services.LogService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("/folo")
public class TrackerResource {
    private final Logger logger = LoggerFactory.getLogger( TrackerResource.class );

    @Inject
    PreSeedConfig pConfig;

    @Inject
    LogService logService;

    @POST
    @Path("/admin/{id}/record")
    @Consumes("multipart/form-data")
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadTrackReport(MultipartFormDataInput input, @PathParam String id) throws IOException {

        //the place of file to be stored
        String fileName = pConfig.storageDir + "/" + id + ".tracklog";

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");

        for (InputPart inputPart : inputParts) {

            try {
                //convert the uploaded file to inputstream
                InputStream inputStream = inputPart.getBody(InputStream.class,null);

                byte [] bytes = IOUtils.toByteArray(inputStream);

                writeFile(bytes,fileName);

                logger.trace("file hase been uploaded to {}", fileName);

                return Response.status(200)
                        .entity("uploadFile is called, Uploaded file name : " + fileName).build();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                TrackedContent key = logService.load(fileName);
                // TODO: key can be accessed here and we can do something awesome
            }

        }

        return Response.status(400).entity("Upload failed").build();

    }

    //save to somewhere
    private void writeFile(byte[] content, String filename) throws IOException {

        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(file);

        fop.write(content);
        fop.flush();
        fop.close();

    }
}
