package org.commonjava.indy.service.archive.services;

import com.google.gson.stream.JsonReader;
import org.commonjava.indy.service.archive.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.util.*;

@ApplicationScoped
public class LogService {

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public TrackedContent load(String path) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        return load(bufferedReader);
    }

    //line by line reader, for large json file map to object, with uploads skipped
    public TrackedContent load(BufferedReader input){
        TrackedContent trackedContent = new TrackedContent();
        JsonReader reader = new JsonReader(input);
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String jsonKey = reader.nextName();

                switch (jsonKey) {
                    case "key":
                        reader.beginObject();
                        if (reader.nextName().equals("id")){
                            trackedContent.setKey(new TrackingKey(reader.nextString()));
                        }
                        reader.endObject();
                        break;
                    case "downloads":
                        trackedContent.setDownloads(loadTrackedContentEntrySet(reader));
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackedContent;
    }

    public Set<TrackedContentEntry> loadTrackedContentEntrySet(JsonReader reader){
        Set<TrackedContentEntry> set = new HashSet<>();
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                set.add(loadTrackedContentEntry(reader));
            }
            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    public TrackedContentEntry loadTrackedContentEntry(JsonReader reader){
        TrackedContentEntry entry = new TrackedContentEntry();
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String jsonKey = reader.nextName();

                switch (jsonKey) {
                    case "accessChannel":
                        entry.setAccessChannel(reader.nextString());
                        break;
                    case "path":
                        entry.setPath(reader.nextString());
                        break;
                    case "localUrl":
                        entry.setOriginUrl(reader.nextString());
                        break;
                    case "md5":
                        entry.setMd5(reader.nextString());
                        break;
                    case "sha256":
                        entry.setSha256(reader.nextString());
                        break;
                    case "sha1":
                        entry.setSha1(reader.nextString());
                        break;
                    case "size":
                        entry.setSize(reader.nextLong());
                        break;
                    case "storeKey":
                        String storeKey = reader.nextString();
                        entry.setStoreKey(new StoreKey(storeKey.split(":")[0],
                                                     StoreType.valueOf(storeKey.split(":")[1]),
                                                     storeKey.split(":")[2]));
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entry;
    }


}
