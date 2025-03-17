package com.example.JsonGenerator.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JsonController {

    private static final String JSON_FOLDER = "JSON"; // Folder where files will be stored

    @PostMapping("/generate")
    public ResponseEntity<?> generateJsonFile(@RequestBody Map<String, Object> jsonData) {
        try {
            // Ensure JSON directory exists
            File directory = new File(JSON_FOLDER);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Extract referenceIdentifier for naming the file
            Map<String, Object> jobInfo = (Map<String, Object>) jsonData.get("jobInfo");
            String referenceIdentifier = (String) jobInfo.get("referenceIdentifier");

            // Generate timestamp for uniqueness
            long timestamp = System.currentTimeMillis();
            String fileName = referenceIdentifier + "_" + timestamp + ".json";

            // Create the JSON file
            File jsonFile = new File(directory, fileName);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(jsonFile, jsonData);

            return ResponseEntity.ok().body(Map.of("message", "JSON file created successfully", "fileName", jsonFile.getAbsolutePath()));

        } catch (IOException | NullPointerException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate JSON file", "details", e.getMessage()));
        }
    }
}
