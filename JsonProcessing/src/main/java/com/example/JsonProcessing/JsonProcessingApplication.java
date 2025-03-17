package com.example.JsonProcessing;

import com.example.JsonProcessing.service.JsonProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JsonProcessingApplication implements CommandLineRunner {

	@Autowired
	private JsonProcessingService jsonProcessingService;

	public static void main(String[] args) {
		SpringApplication.run(JsonProcessingApplication.class, args);
	}

	@Override
	public void run(String... args) {
		// Start file watcher to monitor JSON folder and archive processed files
		String jsonFolder = "JSON";  // Folder where JSON files arrive
		String archiveFolder = "ARCHIVED_JSON";  // Folder to store processed JSON files

		// Start watching JSON folder
		jsonProcessingService.initializeAndWatch(jsonFolder, archiveFolder);
	}
}
