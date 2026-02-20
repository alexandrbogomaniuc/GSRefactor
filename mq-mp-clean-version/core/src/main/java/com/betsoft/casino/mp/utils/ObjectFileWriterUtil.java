package com.betsoft.casino.mp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ObjectFileWriterUtil {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Logger LOG = LogManager.getLogger(ObjectFileWriterUtil.class);

  /**
   * Saves an object to a file in JSON format.
   *
   * @param object   the object to save
   * @param filePath the path of the file to save the object to
   */
  public  void saveObjectAsJsonToFile(Object object, String filePath) {
    Path dirPath = extractDirectoryPath(filePath);
    try {
      createDirectoryIfMissing(dirPath);
    } catch (IOException e) {
      LOG.error("saveObjectAsJsonToFile: directory {} cannot be created!", dirPath.toString());

    }

    try {
      objectMapper.writeValue(new File(filePath), object);
    } catch (IOException e) {
      LOG.error("saveObjectAsJsonToFile: data are not stored in file: {}", object.toString());
    }

    System.out.println("Object saved to file as JSON: " + filePath);
  }

  private static void createDirectoryIfMissing(Path directoryPath) throws IOException {
    if (!Files.exists(directoryPath)) {
      Files.createDirectories(directoryPath);
      System.out.println("Directory created: " + directoryPath);
    }
  }

  /**
   * Extracts and returns the directory path from a full file path.
   *
   * @param filePath The full path to the file.
   * @return The directory path as a String.
   */
  private static Path extractDirectoryPath(String filePath) {
    Path path = Paths.get(filePath);
    return path.getParent();
  }
}
