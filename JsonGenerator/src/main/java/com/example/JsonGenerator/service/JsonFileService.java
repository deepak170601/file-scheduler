package com.example.JsonGenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
public class JsonFileService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DIRECTORY = "JSON";

    public String saveJsonToFile(Map<String, Object> jsonData) {
        try {
            // Create the JSON folder if it doesn't exist
            File directory = new File(DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Extract referenceIdentifier from jobInfo for naming the file
            Map<String, Object> jobInfo = (Map<String, Object>) jsonData.get("jobInfo");
            String referenceIdentifier = (String) jobInfo.get("referenceIdentifier");
            long timestamp = System.currentTimeMillis();
            String fileName = referenceIdentifier + "_" + timestamp + ".json";

            // Create the JSON file
            File jsonFile = new File(directory, fileName);

            // Write JSON with pretty printing
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, jsonData);

            return jsonFile.getAbsolutePath();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Error saving JSON file", e);
        }
    }
}
