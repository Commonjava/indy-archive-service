package org.commonjava.indy.service.archive.services;
import org.commonjava.indy.service.archive.config.PreSeedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ArchiveFileService {
    private final Logger logger = LoggerFactory.getLogger( ArchiveFileService.class );
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    @Inject
    PreSeedConfig pConfig;

    public void zipGenerator(String configID, List<String> files){
        //subject to change, when we decide how to store our data and zip file
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(pConfig.dir, configID + ".zip")))) {

            File directory = new File(pConfig.dir);

            byte[] bytes = new byte[2048];

            for (String fileName : files) {
                String filePath = directory.getPath() + FILE_SEPARATOR + fileName;
                FileInputStream fis = new FileInputStream(filePath);

                zos.putNextEntry(new ZipEntry(fileName));

                int bytesRead;
                while ((bytesRead = fis.read(bytes)) != -1) {
                    zos.write(bytes, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
