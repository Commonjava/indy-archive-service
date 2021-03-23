package org.commonjava.indy.service.archive.services;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.commonjava.indy.service.archive.model.dto.HistoricalContentDTO;
import org.commonjava.indy.service.archive.model.dto.HistoricalContentEntryDTO;

import javax.inject.Inject;
import java.io.*;
import java.util.*;

public class LogService {
    @Inject
    HistoricalContentDTO historicalContentDTO;

    public HistoricalContentDTO load(String TraceLogJson) {
        Gson gson = new Gson();
        return gson.fromJson(TraceLogJson, HistoricalContentDTO.class);
    }

    public HistoricalContentDTO load(InputStreamReader input){
        HistoricalContentDTO log = new HistoricalContentDTO();
        try {
            JsonReader reader = new JsonReader(input);
            reader.beginObject();
            while (reader.hasNext()) {
                String jsonKey = reader.nextName();

                switch (jsonKey) {
                    case "key":
                        reader.beginObject();
                        while (reader.hasNext()){
                            String name = reader.nextName();
                            switch (name){
                                //subject to change, if we changed the name of the field
                                case "buildConfigId":
                                    log.setBuildConfigId(reader.nextString());
                                    break;
                                case "trackId":
                                    log.setTrackId(reader.nextString());
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                        break;
                    case "downloads":
                        log.setDownloads(readTraceLogItemsArray(reader));
                        break;
                    default:
                        reader.skipValue(); //avoid some unhandled events
                        break;
                }
            }
            reader.endObject();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log;
    }

    private Set<HistoricalContentEntryDTO> readTraceLogItemsArray(JsonReader reader) throws IOException {
        HashSet<HistoricalContentEntryDTO> items = new HashSet<>();

        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                items.add(readTraceLogItems(reader));
            }
            reader.endObject();
        }
        reader.endArray();

        return items;
    }

    private HistoricalContentEntryDTO readTraceLogItems(JsonReader reader) throws IOException {
        HistoricalContentEntryDTO item = new HistoricalContentEntryDTO();

        while (reader.hasNext()){
            String jsonKey = reader.nextName();
            switch (jsonKey) {
                case "path":
                    item.setPath(reader.nextString());
                    break;
                case "localUrl":
                    item.setLocalUrl(reader.nextString());
                    break;
                case "md5":
                    item.setMd5(reader.nextString());
                    break;
                case "sha256":
                    item.setSha256(reader.nextString());
                    break;
                case "sha1":
                    item.setSha1(reader.nextString());
                    break;
                case "size":
                    item.setSize(reader.nextLong());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }

        return item;
    }


}
